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
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.AbstractSingleTransaction;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractRemoteTransaction extends AbstractSingleTransaction<Object> implements RemoteTransaction {
    protected final ObjectMapper objectMapper;

    protected AbstractRemoteTransaction(KhTransactionContext transactionContext, ObjectMapper objectMapper) {
        super(transactionContext);
        this.objectMapper = objectMapper;
    }

    protected AbstractRemoteTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AbstractRemoteTransaction get(String url) {
        return request(HttpMethod.GET, url);
    }

    @Override
    public AbstractRemoteTransaction get(String url, Map<String, Object> params) {
        return request(HttpMethod.GET, url, params);
    }

    @Override
    public AbstractRemoteTransaction post(String url) {
        return request(HttpMethod.POST, url);
    }

    @Override
    public AbstractRemoteTransaction post(String url, Object param) {
        return request(HttpMethod.POST, url, param);
    }

    @Override
    public AbstractRemoteTransaction put(String url) {
        return request(HttpMethod.PUT, url);
    }

    @Override
    public AbstractRemoteTransaction put(String url, Object param) {
        return request(HttpMethod.PUT, url, param);
    }

    @Override
    public AbstractRemoteTransaction delete(String url) {
        return request(HttpMethod.DELETE, url);
    }

    @Override
    public AbstractRemoteTransaction delete(String url, Map<String, Object> params) {
        return request(HttpMethod.DELETE, url, params);
    }

    @Override
    public AbstractRemoteTransaction request(HttpMethod method, String url) {
        request(method, url, null, null);
        return this;
    }

    @Override
    public AbstractRemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters) {
        request(method, url, parameters, null);
        return this;
    }

    @Override
    public AbstractRemoteTransaction request(HttpMethod method, String url, Object body) {
        request(method, url, null, body);
        return this;
    }
}
