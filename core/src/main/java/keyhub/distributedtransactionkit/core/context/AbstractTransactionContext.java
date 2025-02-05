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

package keyhub.distributedtransactionkit.core.context;

import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.callback.CallbackStore;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public abstract class AbstractTransactionContext implements KhTransactionContext {
    protected final CompensationStore compensationStore;
    protected final CallbackStore callbackStore;

    public AbstractTransactionContext() {
        this.compensationStore = CompensationStore.of();
        this.callbackStore = CallbackStore.of();
    }

    @Override
    public void storeCompensation(TransactionId transactionId, KhTransaction compensation) {
        this.compensationStore.add(transactionId, compensation);
    }

    @Override
    public void storeCallback(TransactionId transactionId, KhTransaction callback) {
        this.callbackStore.add(transactionId, callback);
    }
}
