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

import java.util.SequencedMap;
import java.util.function.Supplier;

public interface SequencedTransaction extends KhTransaction {

    static SequencedTransaction from(KhTransactionContext transactionContext) {
        return new SimpleSequencedTransaction(transactionContext);
    }

    interface Result extends KhTransaction.Result<SequencedMap<TransactionId, KhTransaction.Result<?>>>{
        KhTransaction.Result<?> get(TransactionId transactionId);
    }

    SequencedTransaction add(KhTransaction transaction);

    @Override
    Result resolve() throws KhTransactionException;

    @Override
    SequencedTransaction setCompensation(Supplier<KhTransaction> compensationSupplier);

    @Override
    SequencedTransaction setCompensation(KhTransaction compensation);

    @Override
    SequencedTransaction setCallback(Supplier<KhTransaction> callbackSupplier);

    @Override
    SequencedTransaction setCallback(KhTransaction callback);
}
