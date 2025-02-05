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

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.remote.RemoteTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Supplier;

public class RemoteFrameworkTransaction extends SingleFrameworkTransaction<Object> implements RemoteTransaction {

    protected RemoteFrameworkTransaction(RemoteTransaction innerTransaction) {
        super(innerTransaction);
    }

    public static RemoteFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        RemoteTransaction transaction = RemoteTransaction.of(context);
        return new RemoteFrameworkTransaction(transaction);
    }

    public static RemoteFrameworkTransaction of(ObjectMapper objectMapper) {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        RemoteTransaction transaction = RemoteTransaction.of(context, objectMapper);
        return new RemoteFrameworkTransaction(transaction);
    }

    @Override
    public RemoteFrameworkTransaction header(String key, String value) {
        ((RemoteTransaction)this.innerTransaction).header(key, value);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction get(String url) {
        ((RemoteTransaction)this.innerTransaction).get(url);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction get(String url, Map<String, Object> params) {
        ((RemoteTransaction)this.innerTransaction).get(url, params);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction post(String url) {
        ((RemoteTransaction)this.innerTransaction).post(url);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction post(String url, Object param) {
        ((RemoteTransaction)this.innerTransaction).post(url, param);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction put(String url) {
        ((RemoteTransaction)this.innerTransaction).put(url);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction put(String url, Object param) {
        ((RemoteTransaction)this.innerTransaction).put(url, param);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction delete(String url) {
        ((RemoteTransaction)this.innerTransaction).delete(url);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction delete(String url, Map<String, Object> params) {
        ((RemoteTransaction)this.innerTransaction).delete(url, params);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction request(HttpMethod method, String url) {
        ((RemoteTransaction)this.innerTransaction).request(method, url);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction request(HttpMethod method, String url, Map<String, Object> parameters) {
        ((RemoteTransaction)this.innerTransaction).request(method, url, parameters);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction request(HttpMethod method, String url, Object body) {
        ((RemoteTransaction)this.innerTransaction).request(method, url, body);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction request(HttpMethod method, String url, Map<String, Object> parameters, Object body) {
        ((RemoteTransaction)this.innerTransaction).request(method, url, parameters, body);
        return this;
    }

    @Override
    public RemoteTransaction.Result resolve() {
        var result = resolving();
        return (RemoteTransaction.Result) result;
    }

    @Override
    public RemoteFrameworkTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        innerTransaction.setCompensation(compensationSupplier);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction setCompensation(KhTransaction compensation) {
        innerTransaction.setCompensation(compensation);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction setCallback(Supplier<KhTransaction> callbackSupplier) {
        innerTransaction.setCallback(callbackSupplier);
        return this;
    }

    @Override
    public RemoteFrameworkTransaction setCallback(KhTransaction callback) {
        innerTransaction.setCallback(callback);
        return this;
    }
}
