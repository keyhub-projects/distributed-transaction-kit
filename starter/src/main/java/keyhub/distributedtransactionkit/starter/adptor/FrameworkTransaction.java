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

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

public abstract class FrameworkTransaction implements KhTransaction {

    protected final KhTransaction innerTransaction;

    public FrameworkTransaction(KhTransaction transaction) {
        this.innerTransaction = transaction;
    }

    protected static FrameworkTransactionContext getTransactionContext() {
        return ApplicationContextProvider.getApplicationContext().getBean(FrameworkTransactionContext.class);
    }

    @Override
    public TransactionId getTransactionId() {
        return innerTransaction.getTransactionId();
    }

    @Override
    public KhTransactionContext getContext() {
        return innerTransaction.getContext();
    }

    @Override
    public FrameworkTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        innerTransaction.setCompensation(compensationSupplier);
        return this;
    }

    @Override
    public FrameworkTransaction setCompensation(KhTransaction compensation) {
        innerTransaction.setCompensation(compensation);
        return this;
    }

    @Override
    public FrameworkTransaction setOutbox(Supplier<KhTransaction> outboxSupplier) {
        innerTransaction.setOutbox(outboxSupplier);
        return this;
    }

    @Override
    public FrameworkTransaction setOutbox(KhTransaction outbox) {
        innerTransaction.setOutbox(outbox);
        return this;
    }

    @Override
    public Result<?> resolve() {
        PlatformTransactionManager transactionManager = ApplicationContextProvider.getApplicationContext().getBean(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = null;
        try {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            }
            TransactionSynchronizationManager.registerSynchronization((FrameworkTransactionContext)innerTransaction.getContext());
            Result<?> result = this.innerTransaction.resolve();

            if (transactionStatus != null) {
                transactionManager.commit(transactionStatus);
            }
            return result;
        } catch (Exception e) {
            if (transactionStatus != null) {
                transactionManager.rollback(transactionStatus);
            }
            throw new KhTransactionRuntimeException(e);
        }
    }
}
