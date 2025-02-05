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

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.*;
import java.util.function.Supplier;

public class SimpleSequencedTransaction extends AbstractCompositeTransaction implements SequencedTransaction {
    protected final List<TransactionId> subTransactionIds;

    protected SimpleSequencedTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.subTransactionIds = new ArrayList<>();
    }

    @Override
    public SimpleSequencedTransaction add(KhTransaction transaction) {
        subTransactionIds.add(transaction.getTransactionId());
        subTransactionMap.put(transaction.getTransactionId(), transaction);
        return this;
    }

    @Override
    public Result resolve() throws KhTransactionException {
        for(TransactionId transactionId : subTransactionIds) {
            KhTransaction transaction = subTransactionMap.get(transactionId);
            if (transaction == null) {
                continue;
            }
            var result = transaction.resolve();
            subTransactionResultMap.put(transactionId, result);
        }
        try{
            return new Result(this);
        }finally {
            subTransactionIds.clear();
            subTransactionResultMap.clear();
        }
    }

    public record Result(
            SequencedMap<TransactionId, KhTransaction.Result<?>> results
    ) implements SequencedTransaction.Result {

        Result(SimpleSequencedTransaction transaction) {
            this(new LinkedHashMap<>());
            for(TransactionId id: transaction.subTransactionIds){
                results.put(id, transaction.subTransactionResultMap.get(id));
            }
        }

        @Override
        public KhTransaction.Result<?> get(TransactionId transactionId) {
            return results.get(transactionId);
        }

        @Override
        public SequencedMap<TransactionId, KhTransaction.Result<?>> get() {
            return results;
        }
    }


    @Override
    public SimpleSequencedTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        KhTransaction transaction = compensationSupplier.get();
        return setCompensation(transaction);
    }

    @Override
    public SimpleSequencedTransaction setCompensation(KhTransaction compensation) {
        this.compensation = compensation;
        return this;
    }

    @Override
    public SimpleSequencedTransaction setCallback(Supplier<KhTransaction> callbackSupplier) {
        KhTransaction transaction = callbackSupplier.get();
        return this.setCallback(transaction);
    }

    @Override
    public SimpleSequencedTransaction setCallback(KhTransaction callback) {
        this.callback = callback;
        return this;
    }
}
