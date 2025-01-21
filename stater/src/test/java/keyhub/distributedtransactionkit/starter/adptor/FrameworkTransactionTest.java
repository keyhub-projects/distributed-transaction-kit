package keyhub.distributedtransactionkit.starter.adptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import keyhub.distributedtransactionkit.core.exception.KhCompensationException;
import keyhub.distributedtransactionkit.core.exception.KhOutboxException;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class FrameworkTransactionTest {

    @Test
    void 스레드_스코프() throws InterruptedException {
        Runnable task = () -> {
            FrameworkTransactionContext context = ApplicationContextProvider.getApplicationContext().getBean(FrameworkTransactionContext.class);
            System.out.println("Context instance: " + context);
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    void 정상_트랜잭션_동작() throws JsonProcessingException, KhTransactionException {
        String sample = "Hello World!";
        KhTransaction utd = SingleFrameworkTransaction.of(()->new KhTransaction.Result() {
            @Override
            public <R> R toData(Class<R> returnType) throws JsonProcessingException {
                System.out.println(sample);
                return returnType.cast(sample);
            }

            @Override
            public <R> List<R> toList(Class<R> returnType) {
                System.out.println("Hello World");
                return List.of(returnType.cast(sample+1), returnType.cast(sample+2));
            }
        });

        var result = utd.resolve();
        assertNotNull(result);
        log.info(result.toString());
        var result2 = result.toData(String.class);
        assertNotNull(result2);
        assertEquals(sample, result2);
        log.info(result2);

    }

    @Test
    @Transactional
    void 어노테이션_Transactional과_정상_트랜잭션_동작() throws JsonProcessingException, KhTransactionException {
        String sample = "Hello World!";
        KhTransaction utd = SingleFrameworkTransaction.of(()->new KhTransaction.Result() {
            @Override
            public <R> R toData(Class<R> returnType) throws JsonProcessingException {
                System.out.println(sample);
                return returnType.cast(sample);
            }

            @Override
            public <R> List<R> toList(Class<R> returnType) {
                System.out.println(sample);
                return List.of(returnType.cast(sample+1), returnType.cast(sample+2));
            }
        });

        var result = utd.resolve();
        assertNotNull(result);
        log.info(result.toString());
        var result2 = result.toData(String.class);
        assertNotNull(result2);
        assertEquals(sample, result2);
        log.info(result2);
    }

    @Nested
    class 보상트랜잭션 {
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext; // Spy된 빈이 주입됨

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext() {
                return spy(new FrameworkTransactionContext());
            }
        }

        @Test
        void 보상트랜잭션_동작() throws KhCompensationException, KhTransactionException {
            String sample = "throw Exception";
            String compensateMessage = "It's compensate!";

            FrameworkTransaction utd = SingleFrameworkTransaction
                    .of(() -> {
                        throw new RuntimeException(sample);
                    })
                    .setCompensation(SingleFrameworkTransaction.of(() -> {
                        System.out.println(compensateMessage);
                        return new KhTransaction.Result() {
                            @Override
                            public <R> R toData(Class<R> returnType) {
                                return returnType.cast(compensateMessage);
                            }
                            @Override
                            public <R> List<R> toList(Class<R> returnType) {
                                return List.of();
                            }
                        };
                    }));

            assertThrows(KhTransactionRuntimeException.class, utd::resolve); // 출력이 왜 안됨..?

            verify(frameworkTransactionContext, times(1)).compensate();
        }
    }

    @Nested
    class Outbox트랜잭션 {
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext() {
                return spy(new FrameworkTransactionContext());
            }
        }

        @Test
        void outbox트랜잭션_동작() throws KhCompensationException, KhTransactionException, JsonProcessingException, KhOutboxException {
            String sample = "Hello World!";
            String outboxMessage = "It's outbox!";

            FrameworkTransaction utd = SingleFrameworkTransaction.of(()->new KhTransaction.Result() {
                        @Override
                        public <R> R toData(Class<R> returnType) throws JsonProcessingException {
                            System.out.println(sample);
                            return returnType.cast(sample);
                        }

                        @Override
                        public <R> List<R> toList(Class<R> returnType) {
                            System.out.println("Hello World");
                            return List.of(returnType.cast(sample+1), returnType.cast(sample+2));
                        }
                    })
                    .setOutbox(SingleFrameworkTransaction.of(() -> {
                        System.out.println(outboxMessage);
                        return new KhTransaction.Result() {
                            @Override
                            public <R> R toData(Class<R> returnType) {
                                return returnType.cast(outboxMessage);
                            }
                            @Override
                            public <R> List<R> toList(Class<R> returnType) {
                                return List.of();
                            }
                        };
                    }));

            var result = utd.resolve();
            assertNotNull(result);
            log.info(result.toString());
            var result2 = result.toData(String.class);
            assertNotNull(result2);
            assertEquals(sample, result2);
            log.info(result2);

            verify(frameworkTransactionContext, times(1)).invokeEvent();
        }
    }
}
