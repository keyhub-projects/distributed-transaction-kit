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

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

import java.util.function.Supplier;

public class SingleFrameworkTransaction<T> extends FrameworkTransaction implements SingleTransaction<T> {
    protected SingleFrameworkTransaction(SingleTransaction<T> innerTransaction) {
        super(innerTransaction);
    }

    public static <T> SingleFrameworkTransaction<T> of(Supplier<T> transactionProcess) {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SingleTransaction<T> transaction = SingleTransaction.of(transactionProcess, context);
        return new SingleFrameworkTransaction<>(transaction);
    }

    @Override
    public SingleFrameworkTransaction<T> setCompensation(Supplier<KhTransaction> compensationSupplier) {
        innerTransaction.setCompensation(compensationSupplier);
        return this;
    }

    @Override
    public SingleFrameworkTransaction<T> setCompensation(KhTransaction compensation) {
        innerTransaction.setCompensation(compensation);
        return this;
    }

    @Override
    public SingleFrameworkTransaction<T> setCallback(Supplier<KhTransaction> callbackSupplier) {
        innerTransaction.setCallback(callbackSupplier);
        return this;
    }

    @Override
    public SingleFrameworkTransaction<T> setCallback(KhTransaction callback) {
        innerTransaction.setCallback(callback);
        return this;
    }

    @Override
    public SingleTransaction.Result<T> resolve() {
        var result = resolving();
        return (SingleTransaction.Result<T>) result;
    }
}
