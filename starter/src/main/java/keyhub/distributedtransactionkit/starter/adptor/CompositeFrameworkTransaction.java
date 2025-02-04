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
import keyhub.distributedtransactionkit.core.transaction.composite.CompositeTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

import java.util.function.Supplier;

public class CompositeFrameworkTransaction extends FrameworkTransaction implements CompositeTransaction {

    public CompositeFrameworkTransaction(KhTransaction transaction) {
        super(transaction);
    }

    public static CompositeFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        CompositeTransaction transaction = CompositeTransaction.from(context);
        return new CompositeFrameworkTransaction(transaction);
    }

    public static CompositeFrameworkTransaction of(KhTransaction ...transactions) {
        CompositeFrameworkTransaction result = of();
        for (KhTransaction transaction : transactions) {
            result.add(transaction);
        }
        return result;
    }

    @Override
    public CompositeFrameworkTransaction add(KhTransaction transaction) {
        ((CompositeTransaction)this.innerTransaction).add(transaction);
        return this;
    }

    @Override
    public CompositeTransaction.Result resolve() {
        var result = resolving();
        return (CompositeTransaction.Result) result;
    }

    @Override
    public CompositeFrameworkTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        innerTransaction.setCompensation(compensationSupplier);
        return this;
    }

    @Override
    public CompositeFrameworkTransaction setCompensation(KhTransaction compensation) {
        innerTransaction.setCompensation(compensation);
        return this;
    }

    @Override
    public CompositeFrameworkTransaction setOutbox(Supplier<KhTransaction> outboxSupplier) {
        innerTransaction.setOutbox(outboxSupplier);
        return this;
    }

    @Override
    public CompositeFrameworkTransaction setOutbox(KhTransaction outbox) {
        innerTransaction.setOutbox(outbox);
        return this;
    }
}
