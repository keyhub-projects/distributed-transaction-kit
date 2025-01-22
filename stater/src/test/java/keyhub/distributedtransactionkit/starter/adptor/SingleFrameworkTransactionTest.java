package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
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
        KhTransaction utd = SingleFrameworkTransaction.of(()->{
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

    public static class CompensationService {
        @Transactional
        public void compensateSample(FrameworkTransaction utd) {
            utd.resolve();
            throw new RuntimeException("throw Exception");
        }
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
            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
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
        void outbox트랜잭션_동작() {
            String sample = "Hello World!";
            String outboxMessage = "It's outbox!";

            FrameworkTransaction utd = SingleFrameworkTransaction.of(()->{
                        log.info(sample);
                        return sample;
                    })
                    .setOutbox(SingleFrameworkTransaction.of(() -> {
                        log.info(outboxMessage);
                        return outboxMessage;
                    }));

            var result = utd.resolve();
            assertNotNull(result);
            var result2 = result.get();
            assertNotNull(result2);
            assertEquals(sample, result2);

            verify(frameworkTransactionContext, times(1)).invokeEvent();
            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    public static class OutboxService {
        @Transactional
        public KhTransaction.Result<?> invokeOutboxSample(FrameworkTransaction utd) {
            return utd.resolve();
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

        @Test
        void 어노테이션_Transactional과_outbox트랜잭션_동작() {
            String sample = "Hello World!";
            String outboxMessage = "It's outbox!";

            FrameworkTransaction utd = SingleFrameworkTransaction.of(()->{
                        log.info(sample);
                        return sample;
                    })
                    .setOutbox(SingleFrameworkTransaction.of(() -> {
                        log.info(outboxMessage);
                        return outboxMessage;
                    }));

            var result = outboxService.invokeOutboxSample(utd);
            assertNotNull(result);
            var result2 = result.get();
            assertNotNull(result2);
            assertEquals(sample, result2);

            verify(frameworkTransactionContext, times(1)).invokeEvent();
            verify(afterTransactionEventHandler, times(1)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
        }
    }
}
