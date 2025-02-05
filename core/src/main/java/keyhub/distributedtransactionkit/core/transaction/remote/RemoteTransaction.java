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

package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Supplier;

public interface RemoteTransaction extends SingleTransaction<Object> {

    static RemoteTransaction of(KhTransactionContext transactionContext){
        return new SimpleRemoteTransaction(transactionContext);
    }

    static RemoteTransaction of(KhTransactionContext transactionContext, ObjectMapper objectMapper){
        return new SimpleRemoteTransaction(transactionContext, objectMapper);
    }

    interface Result extends SingleTransaction.Result<Object> {
    }

    RemoteTransaction header(String key, String value);

    RemoteTransaction get(String url);

    RemoteTransaction get(String url, Map<String, Object> params);

    RemoteTransaction post(String url);

    RemoteTransaction post(String url, Object param);

    RemoteTransaction put(String url);

    RemoteTransaction put(String url, Object param);

    RemoteTransaction delete(String url);

    RemoteTransaction delete(String url, Map<String, Object> params);

    RemoteTransaction request(HttpMethod method, String url);

    RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters);

    RemoteTransaction request(HttpMethod method, String url, Object body);

    RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters, Object body);

    @Override
    Result resolve() throws KhTransactionException;

    @Override
    RemoteTransaction setCompensation(Supplier<KhTransaction> compensationSupplier);

    @Override
    RemoteTransaction setCompensation(KhTransaction compensation);

    @Override
    RemoteTransaction setCallback(Supplier<KhTransaction> callbackSupplier);

    @Override
    RemoteTransaction setCallback(KhTransaction callback);
}
