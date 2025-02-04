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

package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;
import java.util.function.Supplier;

public class SimpleCompositeTransaction extends AbstractCompositeTransaction implements CompositeTransaction {

    protected SimpleCompositeTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
    }

    @Override
    public SimpleCompositeTransaction.Result resolve() throws KhTransactionException {
        for(TransactionId transactionId : subTransactionMap.keySet()) {
            KhTransaction transaction = subTransactionMap.get(transactionId);
            var result = transaction.resolve();
            subTransactionResultMap.put(transactionId, result);
        }
        try{
            return new SimpleCompositeTransaction.Result(this);
        }finally {
            subTransactionResultMap.clear();
        }
    }

    @Override
    public SimpleCompositeTransaction add(KhTransaction transaction) {
        subTransactionMap.put(transaction.getTransactionId(), transaction);
        return this;
    }

    public record Result(
            Map<TransactionId, KhTransaction.Result<?>> results
    ) implements CompositeTransaction.Result {
        public Result(SimpleCompositeTransaction transaction) {
            this(Map.copyOf(transaction.subTransactionResultMap));
        }

        @Override
        public KhTransaction.Result<?> get(TransactionId transactionId) {
            return results.get(transactionId);
        }

        @Override
        public Map<TransactionId, KhTransaction.Result<?>> get() {
            return results;
        }
    }


    @Override
    public SimpleCompositeTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        KhTransaction transaction = compensationSupplier.get();
        return setCompensation(transaction);
    }

    @Override
    public SimpleCompositeTransaction setCompensation(KhTransaction compensation) {
        this.compensation = compensation;
        return this;
    }

    @Override
    public SimpleCompositeTransaction setOutbox(Supplier<KhTransaction> outboxSupplier) {
        KhTransaction transaction = outboxSupplier.get();
        return setOutbox(transaction);
    }

    @Override
    public SimpleCompositeTransaction setOutbox(KhTransaction outbox) {
        this.outbox = outbox;
        return this;
    }
}
