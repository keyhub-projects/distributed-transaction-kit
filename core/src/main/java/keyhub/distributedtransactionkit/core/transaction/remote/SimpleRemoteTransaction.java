/*
 * MIT License
 *
 * Copyright (c) 2025 KeyHub Projects
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    public record Request(
            HttpMethod method,
            String url,
            Map<String, Object> parameters,
            Object body
    ) {
    }

    public record Result(
        Object rawResult,
        ObjectMapper objectMapper
    ) implements RemoteTransaction.Result {

        Result(SimpleRemoteTransaction transaction) throws KhTransactionException {
            this(transaction.rawResult, transaction.objectMapper);
            if (transaction.exception != null) {
                throw new KhTransactionException(transaction.getTransactionId(), transaction.exception);
            }
        }

        @Override
        public Object get() {
            return rawResult;
        }

        @Override
        public Optional<Object> optional() {
            return Optional.ofNullable(rawResult);
        }

        @Override
        public List<Object> list() {
            if (rawResult instanceof List<?> tempList) {
                return tempList.stream()
                        .map(element -> (Object) element)
                        .toList();
            }
            throw new ClassCastException(rawResult + " Cannot cast");
        }

        @Override
        public <T> T get(Class<T> returnType) {
            return objectMapper.convertValue(rawResult, returnType);
        }

        @Override
        public <R> Optional<R> optional(Class<R> returnType) {
            if(rawResult == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.convertValue(rawResult, returnType));
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
        this.request = new Request(method, url, parameters, body);
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
        return new Result(this);
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
        String url = this.request.url();
        Map<String, Object> parameters = this.request.parameters();

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
        HttpMethod method = this.request.method();
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
        Object body = request.body();
        if (body == null) {
            return requestSpec;
        }
        WebClient.RequestBodyUriSpec casted = (WebClient.RequestBodyUriSpec) requestSpec;
        casted.bodyValue(body);
        return casted;
    }


    @Override
    public SimpleRemoteTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        KhTransaction transaction = compensationSupplier.get();
        return setCompensation(transaction);
    }

    @Override
    public SimpleRemoteTransaction setCompensation(KhTransaction compensation) {
        this.compensation = compensation;
        return this;
    }

    @Override
    public SimpleRemoteTransaction setOutbox(Supplier<KhTransaction> outboxSupplier) {
        KhTransaction transaction = outboxSupplier.get();
        return setOutbox(transaction);
    }

    @Override
    public SimpleRemoteTransaction setOutbox(KhTransaction outbox) {
        this.outbox = outbox;
        return this;
    }
}
