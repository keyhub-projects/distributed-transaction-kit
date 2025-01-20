package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.KhTransactionException;
import keyhub.distributedtransactionkit.core.compensation.CompensatingTransactionStore;
import keyhub.distributedtransactionkit.core.outbox.OutboxTransactionStore;
import keyhub.distributedtransactionkit.core.RemoteTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SimpleRemoteTransaction extends AbstractRemoteTransaction {
    private Request request;

    SimpleRemoteTransaction(CompensatingTransactionStore compensatingTransactionStore, OutboxTransactionStore outboxTransactionStore, ObjectMapper objectMapper) {
        super(compensatingTransactionStore, outboxTransactionStore, objectMapper);
    }

    SimpleRemoteTransaction(CompensatingTransactionStore compensatingTransactionStore, OutboxTransactionStore outboxTransactionStore) {
        super(compensatingTransactionStore, outboxTransactionStore);
    }

    SimpleRemoteTransaction() {
        super();
    }

    public static SimpleRemoteTransaction of() {
        return new SimpleRemoteTransaction();
    }

    public static SimpleRemoteTransaction of (CompensatingTransactionStore compensatingTransactionStore, OutboxTransactionStore outboxTransactionStore) {
        return new SimpleRemoteTransaction(compensatingTransactionStore, outboxTransactionStore);
    }

    public static SimpleRemoteTransaction of(CompensatingTransactionStore compensatingTransactionStore, OutboxTransactionStore outboxTransactionStore, ObjectMapper objectMapper) {
        return new SimpleRemoteTransaction(compensatingTransactionStore, outboxTransactionStore, objectMapper);
    }

    @Builder @AllArgsConstructor @NoArgsConstructor @Getter
    public static class Request {
        Map<String, String> headers;
        HttpMethod method;
        String url;
        Map<String, Object> parameters;
        Object body;
    }

    public static class Result implements KhTransaction.Result {
        SimpleRemoteTransaction distributedTransaction;

        private Result(SimpleRemoteTransaction distributedTransaction) throws KhTransactionException {
            this.distributedTransaction = distributedTransaction;
            if (distributedTransaction.exception != null) {
                throw distributedTransaction.exception;
            }
            this.distributedTransaction.storeOutbox();
        }

        private static Result from(SimpleRemoteTransaction distributedTransaction) throws KhTransactionException {
            return new Result(distributedTransaction);
        }

        @Override
        public <R> R toData(Class<R> returnType) {
            return distributedTransaction.objectMapper.convertValue(distributedTransaction.rawResult, returnType);
        }

        @Override
        public <R> List<R> toList(Class<R> returnType) {
            if (distributedTransaction.rawResult instanceof List<?> tempList) {
                return tempList.stream()
                        .map(element -> distributedTransaction.objectMapper.convertValue(element, returnType))
                        .toList();
            }
            throw new ClassCastException(distributedTransaction.rawResult + " Cannot cast");
        }
    }

    @Override
    public SimpleRemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters, Object body) {
        this.request = Request.builder()
                .method(method)
                .url(url)
                .parameters(parameters)
                .body(body)
                .build();
        return this;
    }

    @Override
    public Result resolve() throws KhTransactionException {
        try{
            String targetUrl = generateParameterQuery(this.request);
            WebClient webClient = WebClient.create();
            var requestSpec = generateRequestSpec(webClient, this.request, targetUrl);
            ResponseSpec responseSpec = send(requestSpec);
            Mono<String> mono = responseSpec.bodyToMono(String.class);
            String jsonResponse = mono.block();
            this.rawResult = objectMapper.readValue(jsonResponse, Object.class);
        } catch (Throwable exception) {
            storeCompensation();
            this.exception = new RemoteTransactionException(exception);
        }
        return Result.from(this);
    }

    private ResponseSpec send(WebClient.RequestHeadersSpec<?> requestSpec) throws JsonProcessingException {
        return requestSpec
            .retrieve()
            .onStatus(
                httpStatus -> httpStatus == HttpStatus.INTERNAL_SERVER_ERROR,
                clientResponse -> clientResponse
                    .bodyToMono(String.class)
                    .flatMap(responseBody -> Mono.error(new RemoteTransactionException()))
            );
    }

    private String generateParameterQuery(Request request) {
        String url = request.getUrl();
        Map<String, Object> parameters = request.getParameters();

        StringBuilder query = new StringBuilder(url);
        if (parameters == null || parameters.isEmpty()) {
            return query.toString();
        }
        query.append("?");
        return parameters.keySet().stream()
                .map(key -> "%s=%s".formatted(
                        URLEncoder.encode(key, StandardCharsets.UTF_8),
                        URLEncoder.encode(parameters.get(key).toString(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));
    }

    private WebClient.RequestHeadersSpec<?> generateRequestSpec(WebClient webClient, Request request, String targetUrl) {
        var methodBound = setQuery(webClient, request, targetUrl);
        var headerBound = setHeader(methodBound, request);
        return setBody(headerBound, request);
    }

    private WebClient.RequestHeadersSpec<?> setQuery(WebClient webClient, Request requestMap, String targetUrl) {
        HttpMethod method = requestMap.getMethod();
        return switch (method.name()) {
            case "GET" -> webClient.get().uri(targetUrl);
            case "POST" -> webClient.post().uri(targetUrl);
            case "PUT" -> webClient.put().uri(targetUrl);
            case "DELETE" -> webClient.delete().uri(targetUrl);
            default -> throw new IllegalArgumentException("No method");
        };
    }

    private WebClient.RequestHeadersSpec<?> setHeader(WebClient.RequestHeadersSpec<?> requestSpec, Request request) {
        return requestSpec.headers(httpHeaders ->
                request.getHeaders().forEach(httpHeaders::set)
        );
    }

    private WebClient.RequestHeadersSpec<?> setBody(WebClient.RequestHeadersSpec<?> requestSpec, Request request) {
        Object body = request.getBody();
        if (body == null) {
            return requestSpec;
        }
        WebClient.RequestBodyUriSpec casted = (WebClient.RequestBodyUriSpec) requestSpec;
        casted.bodyValue(body);
        return casted;
    }
}
