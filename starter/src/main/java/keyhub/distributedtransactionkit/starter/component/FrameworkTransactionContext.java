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

package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.context.AbstractTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@Scope("thread") // thread 가 맞을까 request 가 맞을까..
public class FrameworkTransactionContext extends AbstractTransactionContext implements TransactionSynchronization {

    private final ApplicationEventPublisher eventPublisher;
    private final Logger log;

    public FrameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
        super();
        this.eventPublisher = applicationEventPublisher;
        this.log = LoggerFactory.getLogger(FrameworkTransactionContext.class);;
    }

    public void invokeAfterTransactionEvent(KhTransaction transaction) {
        AfterTransactionEvent event = new AfterTransactionEvent(transaction);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void afterCompletion(int status) {
        log.debug("afterCompletion status: {}", status);
        try{
            if(TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.clear();
            }
            switch (status) {
                case STATUS_COMMITTED -> invokeEvent();
                case STATUS_ROLLED_BACK -> compensate();
            }
        } catch (Exception exception) {
            throw new KhTransactionRuntimeException(exception);
        }
    }

    @Override
    public void compensate(TransactionId transactionId) {
        KhTransaction compensation = compensationStore.pop(transactionId);
        outboxStore.poll(transactionId);
        if(compensation == null) {
            return;
        }
        log.debug("invoke AfterTransactionEvent - Compensation {} for {}",
                compensation.getTransactionId(), transactionId);
        invokeAfterTransactionEvent(compensation);
    }

    @Override
    public void invokeEvent(TransactionId transactionId) {
        KhTransaction outbox = outboxStore.poll(transactionId);
        compensationStore.pop(transactionId);
        if(outbox == null) {
            return;
        }
        log.debug("invoke AfterTransactionEvent - Outbox {} for {}",
                outbox.getTransactionId(), transactionId);
        invokeAfterTransactionEvent(outbox);
    }

    @Override
    public void compensate() {
        List<KhTransaction> compensations = compensationStore.popAll();
        if(compensations.isEmpty()) {
            return;
        }
        log.debug("invoke AfterTransactionEvent - Compensations {}",
                compensations.stream().map(KhTransaction::getTransactionId).toList());
        compensations.forEach(this::invokeAfterTransactionEvent);
    }

    @Override
    public void invokeEvent() {
        List<KhTransaction> outboxes = outboxStore.pollAll();
        if(outboxes.isEmpty()) {
            return;
        }
        log.debug("invoke AfterTransactionEvent - Outboxes {}",
                outboxes.stream().map(KhTransaction::getTransactionId).toList());
        outboxes.forEach(this::invokeAfterTransactionEvent);
    }
}
