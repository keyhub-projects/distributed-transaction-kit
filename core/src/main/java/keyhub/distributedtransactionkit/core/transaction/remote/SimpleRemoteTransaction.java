package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
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
    Map<String, String> headers = new HashMap<>();

    SimpleRemoteTransaction(KhTransactionContext transactionContext, ObjectMapper objectMapper) {
        super(transactionContext, objectMapper);
    }

    SimpleRemoteTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
    }

    public static SimpleRemoteTransaction of(KhTransactionContext transactionContext) {
        return new SimpleRemoteTransaction(transactionContext);
    }

    public static SimpleRemoteTransaction of(KhTransactionContext transactionContext, ObjectMapper objectMapper) {
        return new SimpleRemoteTransaction(transactionContext, objectMapper);
    }

    @Builder @AllArgsConstructor @NoArgsConstructor @Getter
    public static class Request {
        HttpMethod method;
        String url;
        Map<String, Object> parameters;
        Object body;
    }

    public static class Result implements RemoteTransaction.Result {
        private final Object rawResult;
        private final ObjectMapper objectMapper;
        private Result(SimpleRemoteTransaction transaction) throws KhTransactionException {
            this.rawResult = transaction.rawResult;
            this.objectMapper = transaction.objectMapper;

            if (transaction.exception != null) {
                throw new KhTransactionException(transaction.getTransactionId(), transaction.exception);
            }
        }

        private static Result from(SimpleRemoteTransaction transaction) throws KhTransactionException {
            return new Result(transaction);
        }

        @Override
        public Object get() {
            return rawResult;
        }

        @Override
        public <T> T get(Class<T> returnType) {
            return objectMapper.convertValue(rawResult, returnType);
        }

        @Override
        public <T> List<T> list(Class<T> returnType) {
            if (rawResult instanceof List<?> tempList) {
                return tempList.stream()
                        .map(element -> objectMapper.convertValue(element, returnType))
                        .toList();
            }
            throw new ClassCastException(rawResult + " Cannot cast");
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
    public SimpleRemoteTransaction header(String key, String value){
        this.headers.put(key, value);
        return this;
    }

    @Override
    public Result resolve() throws KhTransactionException {
        try{
            String targetUrl = generateParameterQuery();
            WebClient webClient = WebClient.create();
            var requestSpec = generateRequestSpec(webClient, targetUrl);
            ResponseSpec responseSpec = send(requestSpec);
            Mono<String> mono = responseSpec.bodyToMono(String.class);
            String jsonResponse = mono.block();
            this.rawResult = objectMapper.readValue(jsonResponse, Object.class);
            storeCompensation();
            storeOutbox();
        } catch (Throwable exception) {
            this.exception = new KhTransactionException(transactionId, exception);
        }
        return Result.from(this);
    }

    private ResponseSpec send(WebClient.RequestHeadersSpec<?> requestSpec) {
        return requestSpec
            .retrieve()
            .onStatus(
                httpStatus -> httpStatus == HttpStatus.INTERNAL_SERVER_ERROR,
                clientResponse -> clientResponse
                    .bodyToMono(String.class)
                    .flatMap(responseBody -> Mono.error(new KhTransactionException(transactionId)))
            );
    }

    private String generateParameterQuery() {
        String url = this.request.getUrl();
        Map<String, Object> parameters = this.request.getParameters();

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

    private WebClient.RequestHeadersSpec<?> generateRequestSpec(WebClient webClient, String targetUrl) {
        var queryBound = setQuery(webClient, targetUrl);
        var headerBound = setHeader(queryBound);
        return setBody(headerBound, this.request);
    }

    private WebClient.RequestHeadersSpec<?> setQuery(WebClient webClient, String targetUrl) {
        HttpMethod method = this.request.getMethod();
        return switch (method.name()) {
            case "GET" -> webClient.get().uri(targetUrl);
            case "POST" -> webClient.post().uri(targetUrl);
            case "PUT" -> webClient.put().uri(targetUrl);
            case "DELETE" -> webClient.delete().uri(targetUrl);
            default -> throw new IllegalArgumentException("No method");
        };
    }

    private WebClient.RequestHeadersSpec<?> setHeader(WebClient.RequestHeadersSpec<?> requestSpec) {
        return requestSpec.headers(httpHeaders ->
                this.headers.forEach(httpHeaders::set)
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
