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

package keyhub.distributedtransactionkit.core.context.callback;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleCallbackStore implements CallbackStore {

    private final Queue<TransactionId> transactionIdQueue = new ConcurrentLinkedQueue<>();
    private final Map<TransactionId, KhTransaction> callbacks;

    SimpleCallbackStore(Map<TransactionId, KhTransaction> callbacks) {
        this.callbacks = callbacks;
    }

    static SimpleCallbackStore of(){
        return new SimpleCallbackStore(
                new ConcurrentHashMap<>()
        );
    }

    @Override
    public void add(TransactionId transactionId, KhTransaction callback) {
        transactionIdQueue.add(transactionId);
        callbacks.put(transactionId, callback);
    }

    @Override
    public KhTransaction poll(TransactionId transactionId) {
        transactionIdQueue.remove(transactionId);
        return callbacks.remove(transactionId);
    }

    @Override
    public List<KhTransaction> pollAll() {
        return transactionIdQueue.stream()
                .map(this::poll)
                .toList();
    }
}
