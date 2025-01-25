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

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public class SimpleSingleTransaction<T> extends AbstractSingleTransaction<T> {

    private final Supplier<T> transactionProcess;

    @Override
    public SimpleSingleTransaction.Result<T> resolve() throws KhTransactionException {
        try {
            var result = transactionProcess.get();
            storeCompensation();
            storeOutbox();
            return new Result<>(result);
        } catch (Exception e) {
            throw new KhTransactionException(transactionId, e);
        }
    }

    public SimpleSingleTransaction(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        super(transactionContext);
        this.transactionProcess = transactionProcess;
    }

    public static <T> SimpleSingleTransaction<T> of(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        return new SimpleSingleTransaction<>(transactionProcess, transactionContext);
    }

    public static class Result<T> implements KhTransaction.Result<T> {
        T data;

        Result(T data) {
            this.data = data;
        }

        @Override
        public T get(){
            return data;
        }

        @Override
        public <R> R get(Class<R> returnType) {
            return returnType.cast(data);
        }
    }
}
