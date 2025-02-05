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

package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;

public abstract class AbstractTransaction implements KhTransaction {
    protected final TransactionId transactionId;
    protected final KhTransactionContext context;
    protected KhTransaction compensation;
    protected KhTransaction callback;

    protected AbstractTransaction(KhTransactionContext transactionContext) {
        this.transactionId = TransactionId.ofUuid();
        this.context = transactionContext;
    }

    protected void storeCompensation() {
        if(compensation != null) {
            this.context.storeCompensation(this.transactionId, this.compensation);
        }
    }

    protected void storeOutbox() {
        if(callback != null) {
            this.context.storeCallback(this.transactionId, this.callback);
        }
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public KhTransactionContext getContext() {
        return context;
    }
}
