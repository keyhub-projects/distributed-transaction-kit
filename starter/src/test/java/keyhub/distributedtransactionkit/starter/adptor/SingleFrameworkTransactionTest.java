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

import keyhub.distributedtransactionkit.core.etc.ApplicationContextProvider;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import keyhub.distributedtransactionkit.starter.component.AfterTransactionEventHandler;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class SingleFrameworkTransactionTest {

    @Test
    void 스레드_스코프() throws InterruptedException {
        Runnable task = () -> {
            FrameworkTransactionContext context = ApplicationContextProvider.getApplicationContext().getBean(FrameworkTransactionContext.class);
            log.info("Context instance: " + context);
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    void 정상_트랜잭션_동작() throws KhTransactionException {
        String sample = "Hello World!";
        SingleTransaction<String> utd = SingleFrameworkTransaction.of(()->{
            log.info(sample);
            return sample;
        });

        var result = utd.resolve();
        assertNotNull(result);
        log.info(result.toString());
        var result2 = result.get(String.class);
        assertNotNull(result2);
        assertEquals(sample, result2);
        log.info(result2);
    }

    @Test
    @Transactional
    void 어노테이션_Transactional과_정상_트랜잭션_동작() throws KhTransactionException {
        String sample = "Hello World!";
        KhTransaction utd = SingleFrameworkTransaction.of(()-> {
            log.info("toData:{}", sample);
            return sample;
        });

        var result = utd.resolve();
        assertNotNull(result);
        log.info(result.toString());
        var result2 = result.get();
        assertNotNull(result2);
        assertEquals(sample, result2);
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
            public void compensateSample(KhTransaction utd) throws KhTransactionException {
                utd.resolve();
                throw new RuntimeException("throw Exception");
            }
        }

        @Test
        void 어노테이션_Transactional내부_실패에_의한_보상트랜잭션_동작() {
            String utdMessage = "It will compensate";
            String compensateMessage = "It's compensate!";

            FrameworkTransaction utd = SingleFrameworkTransaction
                    .of(() -> {
                        return utdMessage;
                    })
                    .setCompensation(SingleFrameworkTransaction.of(() -> {
                        log.info(compensateMessage);
                        return compensateMessage;
                    }));

            assertThrows(RuntimeException.class, () -> compensationService.compensateSample(utd));

            verify(frameworkTransactionContext, times(1)).compensate();
            verify(afterTransactionEventHandler, times(1)).handleResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    @Nested
    class Callback트랜잭션 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;

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
        }

        @Test
        void callback트랜잭션_동작() throws KhTransactionException {
            String sample = "Hello World!";
            String callbackMessage = "It's callback!";

            SingleTransaction<String> utd = SingleFrameworkTransaction.of(()->{
                        log.info(sample);
                        return sample;
                    })
                    .setCallback(SingleFrameworkTransaction.of(() -> {
                        log.info(callbackMessage);
                        return callbackMessage;
                    }));

            var result = utd.resolve();
            assertNotNull(result);
            var result2 = result.get();
            assertNotNull(result2);
            assertEquals(sample, result2);

            verify(frameworkTransactionContext, times(1)).callback();
            verify(afterTransactionEventHandler, times(1)).handleResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    @Nested
    class Callback트랜잭션2 {
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private CallbackService callbackService;

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
            public CallbackService callbackService() {
                return new CallbackService();
            }
        }

        public static class CallbackService {
            @Transactional
            public <T> KhTransaction.Result<T> invokeCallbackSample(SingleTransaction<T> utd) throws KhTransactionException {
                return utd.resolve();
            }
        }

        @Test
        void 어노테이션_Transactional과_callback트랜잭션_동작() throws KhTransactionException {
            String sample = "Hello World!";
            String callbackMessage = "It's callback!";

            SingleTransaction<String> utd = SingleFrameworkTransaction.of(()->{
                        log.info(sample);
                        return sample;
                    })
                    .setCallback(SingleFrameworkTransaction.of(() -> {
                        log.info(callbackMessage);
                        return callbackMessage;
                    }));

            var result = callbackService.invokeCallbackSample(utd);
            assertNotNull(result);
            var result2 = result.get();
            assertNotNull(result2);
            assertEquals(sample, result2);

            verify(frameworkTransactionContext, times(1)).callback();
            verify(afterTransactionEventHandler, times(1)).handleResolveEvent(any(AfterTransactionEvent.class));
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
            public String invokeCallbackSample() throws KhTransactionException {
                SingleTransaction<String> utd = SingleFrameworkTransaction.of(()->{
                            String sample = "Hello World!";
                            log.info(sample);
                            return sample;
                        })
                        .setCompensation(SingleFrameworkTransaction.of(()->{
                            String compensationMessage = "It's compensation!";
                            log.info(compensationMessage);
                            return compensationMessage;
                        }))
                        .setCallback(SingleFrameworkTransaction.of(() -> {
                            String callbackMessage = "It's callback!";
                            log.info(callbackMessage);
                            return callbackMessage;
                        }));
                return utd.resolve()
                        .get(String.class);
            }
        }

        @Test
        void 종합Transaction_동작() throws KhTransactionException {
            var result = transactionTestService.invokeCallbackSample();
            assertNotNull(result);
            assertEquals("Hello World!", result);

            verify(afterTransactionEventHandler, times(1)).handleResolveEvent(any(AfterTransactionEvent.class));
            verify(frameworkTransactionContext, times(0)).compensate();
            verify(frameworkTransactionContext, times(1)).callback();
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
            public String invokeCallbackSample() throws KhTransactionException {
                SingleTransaction<String> utd = SingleFrameworkTransaction.of(()->{
                            String sample = "Hello World!";
                            log.info(sample);
                            return sample;
                        })
                        .setCompensation(SingleFrameworkTransaction.of(()->{
                            String compensationMessage = "It's compensation!";
                            log.info(compensationMessage);
                            return compensationMessage;
                        }))
                        .setCallback(SingleFrameworkTransaction.of(() -> {
                            String callbackMessage = "It's callback!";
                            log.info(callbackMessage);
                            return callbackMessage;
                        }));
                var result = utd.resolve()
                        .get(String.class);
                invokeException();
                return result;
            }

            private void invokeException(){
                throw new RuntimeException("I need Exception!");
            }
        }

        @Test
        void 종합Transaction_동작() {
            assertThrows(RuntimeException.class, ()->transactionTestService.invokeCallbackSample());

            verify(afterTransactionEventHandler, times(1)).handleResolveEvent(any(AfterTransactionEvent.class));
            verify(frameworkTransactionContext, times(1)).compensate();
            verify(frameworkTransactionContext, times(0)).callback();
        }
    }
}
