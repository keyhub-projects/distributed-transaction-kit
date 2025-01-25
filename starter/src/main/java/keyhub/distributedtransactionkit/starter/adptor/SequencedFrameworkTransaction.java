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
import keyhub.distributedtransactionkit.core.transaction.composite.SequencedTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

public class SequencedFrameworkTransaction extends FrameworkTransaction implements SequencedTransaction {

    public SequencedFrameworkTransaction(KhTransaction transaction) {
        super(transaction);
    }

    public static SequencedFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SequencedTransaction transaction = SequencedTransaction.from(context);
        return new SequencedFrameworkTransaction(transaction);
    }

    public static SequencedFrameworkTransaction of(KhTransaction ...transactions) {
        SequencedFrameworkTransaction result = of();
        for (KhTransaction transaction : transactions) {
            result.add(transaction);
        }
        return result;
    }

    @Override
    public SequencedFrameworkTransaction add(KhTransaction transaction) {
        ((SequencedTransaction)this.innerTransaction).add(transaction);
        return this;
    }
}
