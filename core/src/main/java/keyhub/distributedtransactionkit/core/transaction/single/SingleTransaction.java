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

package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.remote.RemoteTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface SingleTransaction<T> extends KhTransaction {

    static <T> SingleTransaction<T> of(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        return SimpleSingleTransaction.of(transactionProcess, transactionContext);
    }

    interface Result<T> extends KhTransaction.Result<T> {
        T get();
        Optional<T> optional();
        List<T> list();
        <R> R get(Class<R> returnType);
        <R> Optional<R> optional(Class<R> returnType);
        <R> List<R> list(Class<R> returnType);
    }

    @Override
    Result<T> resolve() throws KhTransactionException;

    @Override
    SingleTransaction<T> setCompensation(Supplier<KhTransaction> compensationSupplier);

    @Override
    SingleTransaction<T> setCompensation(KhTransaction compensation);

    @Override
    SingleTransaction<T> setOutbox(Supplier<KhTransaction> outboxSupplier);

    @Override
    SingleTransaction<T> setOutbox(KhTransaction outbox);
}
