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

package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleCompensationStore implements CompensationStore {

    private final Queue<TransactionId> transactionIdQueue = new ConcurrentLinkedQueue<>();
    private final Map<TransactionId, KhTransaction> compensations;

    SimpleCompensationStore(Map<TransactionId, KhTransaction> compensations) {
        this.compensations = compensations;
    }

    static SimpleCompensationStore of() {
        return new SimpleCompensationStore(
                new ConcurrentHashMap<>()
        );
    }

    @Override
    public void add(TransactionId transactionId, KhTransaction compensation) {
        transactionIdQueue.add(transactionId);
        compensations.putIfAbsent(transactionId, compensation);
    }

    @Override
    public KhTransaction pop(TransactionId transactionId) {
        transactionIdQueue.remove(transactionId);
        return compensations.remove(transactionId);
    }

    @Override
    public List<KhTransaction> popAll() {
        return transactionIdQueue.stream()
                .map(this::pop)
                .toList();
    }
}
