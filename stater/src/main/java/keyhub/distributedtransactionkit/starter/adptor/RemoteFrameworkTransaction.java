package keyhub.distributedtransactionkit.starter.adptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.remote.RemoteTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import org.springframework.http.HttpMethod;

import java.util.Map;

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
}
