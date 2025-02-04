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
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;
import keyhub.distributedtransactionkit.core.transaction.composite.CompositeTransaction;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class CompositeFrameworkTransactionTest {

    @Nested
    class Composite_정상트랜잭션{
        static KhTransaction single(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                return message;
            });
        }

        @Test
        void 정상_트랜잭션_동작() throws KhTransactionException {
            CompositeTransaction utd = CompositeFrameworkTransaction.of(
                            single("1"), single("2"), single("3")
                    );
            var result = utd.resolve();
            assertNotNull(result);
            log.info(result.toString());
            var result2 = result.get();
            assertNotNull(result2);
            log.info(result2.toString());
            assertEquals(3, result2.size());
        }
    }

    @Nested
    class Composite_outbox사례{
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
            public Map<TransactionId, KhTransaction.Result<?>> outboxSample() {
                CompositeTransaction.Result result = CompositeFrameworkTransaction.of(
                        single("1"), single("2"),
                        single("I will invoke outbox1!").setOutbox(single("outbox1")),
                        single("I will invoke outbox2!").setOutbox(single("outbox2"))
                ).resolve();
                CompositeFrameworkTransaction.of(
                        single("5"), single("6"),
                        single("I will invoke outbox3!").setOutbox(single("outbox3")),
                        single("I will invoke outbox4!").setOutbox(single("outbox4")),
                        single("7"), single("8")
                ).resolve();
                return result.get();
            }
        }

        static KhTransaction single(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                return message;
            });
        }

        @Test
        void outbox_트랜잭션_동작() {
            var result = outboxService.outboxSample();

            assertNotNull(result);
            assertEquals(4, result.size());
            log.info(result.toString());
            verify(frameworkTransactionContext, times(0)).compensate();
            verify(frameworkTransactionContext, times(1)).invokeEvent();
            verify(afterTransactionEventHandler, times(4)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    @Nested
    class Composite_보상사례{
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
            public void compensateSample() throws KhTransactionException {
                var failTransaction = failSingle("I will be fail!");
                CompositeFrameworkTransaction.of(
                        single("1"), single("2"),
                        single("I will compensate1!").setCompensation(single("compensation1")).setOutbox(single("no outbox1")),
                        single("3"),
                        single("4").setOutbox(single("no outbox2")),
                        single("I will compensate2!").setCompensation(single("compensation2"))
                ).resolve();
                single("I will compensate3!").setCompensation(single("compensation3")).resolve();
                failTransaction.resolve();
                CompositeFrameworkTransaction.of(
                        single("no1"), single("no2"),
                        single("no3").setCompensation(single("no compensation1")).setOutbox(single("no outbox1"))
                ).resolve();
            }
        }

        static KhTransaction single(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                return message;
            });
        }

        static KhTransaction failSingle(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                throw new RuntimeException("fail");
            });
        }

        @Test
        void 보상_트랜잭션_동작() {
            assertThrows(KhTransactionRuntimeException.class, ()-> compensationService.compensateSample());

            verify(frameworkTransactionContext, times(1)).compensate();
            verify(frameworkTransactionContext, times(0)).invokeEvent();
            verify(afterTransactionEventHandler, times(3)).handleOutboxResolveEvent(any(AfterTransactionEvent.class));
        }
    }
}