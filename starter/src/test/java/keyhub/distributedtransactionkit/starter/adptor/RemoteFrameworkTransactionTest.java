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

package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.starter.component.AfterTransactionEventHandler;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class RemoteFrameworkTransactionTest {

    private MockWebServer mockWebServer;
    private static String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        baseUrl = mockWebServer.url("/").toString();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void 테스트환경_mockWebServer_정상작동확인() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message\": \"success\"}")
                .addHeader("Content-Type", "application/json"));
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

        String response = webClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertEquals("{\"message\": \"success\"}", response);
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals("/test", recordedRequest.getPath());
    }

    @Test
    void 정상_트랜잭션_동작() throws KhTransactionException {
        String sample = "Hello World!";
        String message = "{\"message\": \"" + sample +"\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(message)
                .addHeader("Content-Type", "application/json"));
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        KhTransaction utd = SingleFrameworkTransaction.of(()-> webClient
                .get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class)
                .block()
        );

        var result = utd.resolve();

        assertNotNull(result);
        log.info(result.toString());
        var result2 = result.get(String.class);
        assertNotNull(result2);
        assertEquals(message, result2);
        log.info(result2);
    }

    @Test
    void 정상_Remote_트랜잭션_동작() throws KhTransactionException {
        String sample = "Hello World!";
        String message = "{\"message\": \"" + sample +"\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(message)
                .addHeader("Content-Type", "application/json"));
        KhTransaction utd = RemoteFrameworkTransaction.of()
                .get(baseUrl)
                .header("Content-Type", "application/json");

        var result = utd.resolve();

        assertNotNull(result);
        var result2 = result.get(Map.class);
        assertNotNull(result2);
        assertEquals(sample, result2.get("message"));
        log.info(result2.toString());
    }

    @Nested
    class 보상트랜잭션 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private CompensationService compensationService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public CompensationService compensationService() {
                return new CompensationService();
            }
        }

        public static class CompensationService {
            @Transactional
            public void compensateSample(FrameworkTransaction utd) {
                utd.resolve();
                throw new RuntimeException("throw Exception");
            }
        }

        @Test
        void 어노테이션_Transactional내부_실패에_의한_원격보상트랜잭션_동작() {
            String utdMessage = "It will compensate";
            String utdJson = "{\"message\": \"" + utdMessage +"\"}";
            String compensateMessage = "It's compensate!";
            String compensateJson = "{\"message\": \"" + compensateMessage +"\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(utdJson)
                    .addHeader("Content-Type", "application/json"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(compensateJson)
                    .addHeader("Content-Type", "application/json"));

            FrameworkTransaction utd = RemoteFrameworkTransaction.of()
                    .get(baseUrl)
                    .header("Content-Type", "application/json")
                    .setCompensation(
                        // 1회 재시도
                        RemoteFrameworkTransaction.of()
                            .get(baseUrl)
                            .header("Content-Type", "application/json")
                    );

            assertThrows(RuntimeException.class, () -> compensationService.compensateSample(utd));

            verify(frameworkTransactionContext, times(1)).compensate();
            verify(afterTransactionEventHandler, times(1))
                    .handleOutboxResolveEvent(any(AfterTransactionEvent.class));
            // compensation이나 outbox에 대한 로깅을 지원하자
            // logger 인터페이스 제공
            // error, warn, info, debug, trace 나눠 기록하기
            // 구현체가 없으면 안해줌
        }
    }

    @Nested
    class Outbox트랜잭션 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }
        }

        @Test
        void outbox트랜잭션_동작() {
            String utdMessage = "It will outbox";
            String utdJson = "{\"message\": \"" + utdMessage +"\"}";
            String outboxMessage = "It's outbox!";
            String outboxJson = "{\"message\": \"" + outboxMessage +"\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(utdJson)
                    .addHeader("Content-Type", "application/json"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(outboxJson)
                    .addHeader("Content-Type", "application/json"));

            var result = RemoteFrameworkTransaction.of()
                    .get(baseUrl)
                    .header("Content-Type", "application/json")
                    .setOutbox(RemoteFrameworkTransaction.of()
                        .get(baseUrl)
                        .header("Content-Type", "application/json")
                    )
                    .resolve();

            assertNotNull(result);
            var result2 = result.get(Map.class);
            assertNotNull(result2);
            assertEquals(utdMessage, result2.get("message"));
            verify(frameworkTransactionContext, times(1)).invokeEvent();
            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    @Nested
    class Outbox트랜잭션2 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private OutboxService outboxService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public OutboxService outboxService() {
                return new OutboxService();
            }
        }

        public static class OutboxService {
            @Transactional
            public KhTransaction.Result<?> invokeOutboxSample(FrameworkTransaction utd) {
                return utd.resolve();
            }
        }

        @Test
        void 어노테이션_Transactional과_outbox트랜잭션_동작() {
            String utdMessage = "It will outbox";
            String utdJson = "{\"message\": \"" + utdMessage +"\"}";
            String outboxMessage = "It's outbox!";
            String outboxJson = "{\"message\": \"" + outboxMessage +"\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(utdJson)
                    .addHeader("Content-Type", "application/json"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(outboxJson)
                    .addHeader("Content-Type", "application/json"));

            FrameworkTransaction utd = RemoteFrameworkTransaction.of()
                    .get(baseUrl)
                    .header("Content-Type", "application/json")
                    .setOutbox(RemoteFrameworkTransaction.of()
                            .get(baseUrl)
                            .header("Content-Type", "application/json")
                    );

            var result = outboxService.invokeOutboxSample(utd);

            assertNotNull(result);
            var result2 = result.get(Map.class);
            assertNotNull(result2);
            assertEquals(utdMessage, result2.get("message"));
            verify(frameworkTransactionContext, times(1)).invokeEvent();
            verify(afterTransactionEventHandler, times(1))
                    .handleOutboxResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    @Nested
    class 종합_작성_통과사례 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private TransactionTestService transactionTestService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public TransactionTestService transactionTestService() {
                return new TransactionTestService();
            }
        }

        public static class TransactionTestService {
            @Transactional
            public Map<String, String> invokeOutboxSample(KhTransaction utd) throws KhTransactionException {
                return utd.resolve()
                        .get(Map.class);
            }
        }

        @Test
        void 종합Transaction_동작() throws KhTransactionException {
            String utdMessage = "It will outbox";
            String utdJson = "{\"message\": \"" + utdMessage +"\"}";
            String outboxMessage = "It's outbox!";
            String outboxJson = "{\"message\": \"" + outboxMessage +"\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(utdJson)
                    .addHeader("Content-Type", "application/json"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(outboxJson)
                    .addHeader("Content-Type", "application/json"));

            FrameworkTransaction utd = RemoteFrameworkTransaction.of()
                    .get(baseUrl)
                    .header("Content-Type", "application/json")
                    .setCompensation(RemoteFrameworkTransaction.of()
                            .get(baseUrl)
                            .header("Content-Type", "application/json"))
                    .setOutbox(RemoteFrameworkTransaction.of()
                            .get(baseUrl)
                            .header("Content-Type", "application/json")
                    );
            Map<String, String> result = transactionTestService.invokeOutboxSample(utd);
            assertNotNull(result);
            assertEquals(utdMessage, result.get("message"));

            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
            verify(frameworkTransactionContext, times(0)).compensate();
            verify(frameworkTransactionContext, times(1)).invokeEvent();
        }
    }

    @Nested
    class 종합_작성_보상사례 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private TransactionTestService transactionTestService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public TransactionTestService transactionTestService() {
                return new TransactionTestService();
            }
        }

        public static class TransactionTestService {
            @Transactional
            public Map<String, String> invokeOutboxSample(KhTransaction utd) throws KhTransactionException {
                var result = utd.resolve()
                        .get(Map.class);
                invokeException();
                return result;
            }

            private void invokeException(){
                throw new RuntimeException("I need Exception!");
            }
        }

        @Test
        void 종합Transaction_동작() {
            String utdMessage = "It will compensate";
            String utdJson = "{\"message\": \"" + utdMessage +"\"}";
            String compensateMessage = "It's compensate!";
            String compensateJson = "{\"message\": \"" + compensateMessage +"\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(utdJson)
                    .addHeader("Content-Type", "application/json"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(compensateJson)
                    .addHeader("Content-Type", "application/json"));

            FrameworkTransaction utd = RemoteFrameworkTransaction.of()
                    .get(baseUrl)
                    .header("Content-Type", "application/json")
                    .setCompensation(RemoteFrameworkTransaction.of()
                            .get(baseUrl)
                            .header("Content-Type", "application/json"))
                    .setOutbox(RemoteFrameworkTransaction.of()
                            .get(baseUrl)
                            .header("Content-Type", "application/json")
                    );

            assertThrows(RuntimeException.class, ()->transactionTestService.invokeOutboxSample(utd));

            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
            verify(frameworkTransactionContext, times(1)).compensate();
            verify(frameworkTransactionContext, times(0)).invokeEvent();
        }
    }

    @Nested
    class 종합_작성_메서드를_활용한_사례 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private TransactionTestService transactionTestService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public TransactionTestService transactionTestService() {
                return new TransactionTestService();
            }
        }

        public static class TransactionTestService {
            @Transactional
            public Map<String, String> invokeOutboxSample() throws KhTransactionException {
                Map<String, String> result = utd(baseUrl)
                        .setCompensation(utd(baseUrl))
                        .setOutbox(utd(baseUrl))
                        .resolve()
                        .get(Map.class);
                log.info(result.toString());
                return result;
            }

            KhTransaction utd(String baseUrl) {
                return RemoteFrameworkTransaction.of()
                        .get(baseUrl)
                        .header("Content-Type", "application/json");
            }
        }

        @Test
        void 종합Transaction_동작() throws KhTransactionException {
            String utdMessage = "It will outbox";
            String utdJson = "{\"message\": \"" + utdMessage +"\"}";
            String outboxMessage = "It's outbox!";
            String outboxJson = "{\"message\": \"" + outboxMessage +"\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(utdJson)
                    .addHeader("Content-Type", "application/json"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(outboxJson)
                    .addHeader("Content-Type", "application/json"));

            Map<String, String> result = transactionTestService.invokeOutboxSample();
            assertNotNull(result);
            assertEquals(utdMessage, result.get("message"));

            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
            verify(frameworkTransactionContext, times(0)).compensate();
            verify(frameworkTransactionContext, times(1)).invokeEvent();
        }
    }
}