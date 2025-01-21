package keyhub.distributedtransactionkit.starter.adptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.remote.RemoteTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class RemoteFrameworkTransaction extends FrameworkTransaction implements RemoteTransaction {

    protected RemoteFrameworkTransaction(KhTransaction khTransaction) {
        super(khTransaction);
    }

    public static RemoteFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SingleTransaction transaction = RemoteTransaction.of(context);
        return new RemoteFrameworkTransaction(transaction);
    }

    public static RemoteFrameworkTransaction of(ObjectMapper objectMapper) {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SingleTransaction transaction = RemoteTransaction.of(context, objectMapper);
        return new RemoteFrameworkTransaction(transaction);
    }

    @Override
    public RemoteTransaction get(String url) {
        return ((RemoteTransaction)this.khTransaction).get(url);
    }

    @Override
    public RemoteTransaction get(String url, Map<String, Object> params) {
        return ((RemoteTransaction)this.khTransaction).get(url, params);
    }

    @Override
    public RemoteTransaction post(String url) {
        return ((RemoteTransaction)this.khTransaction).post(url);
    }

    @Override
    public RemoteTransaction post(String url, Object param) {
        return ((RemoteTransaction)this.khTransaction).post(url, param);
    }

    @Override
    public RemoteTransaction put(String url) {
        return ((RemoteTransaction)this.khTransaction).put(url);
    }

    @Override
    public RemoteTransaction put(String url, Object param) {
        return ((RemoteTransaction)this.khTransaction).put(url, param);
    }

    @Override
    public RemoteTransaction delete(String url) {
        return ((RemoteTransaction)this.khTransaction).delete(url);
    }

    @Override
    public RemoteTransaction delete(String url, Map<String, Object> params) {
        return ((RemoteTransaction)this.khTransaction).delete(url, params);
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url) {
        return ((RemoteTransaction)this.khTransaction).request(method, url);
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters) {
        return ((RemoteTransaction)this.khTransaction).request(method, url, parameters);
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url, Object body) {
        return ((RemoteTransaction)this.khTransaction).request(method, url, body);
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters, Object body) {
        return ((RemoteTransaction)this.khTransaction).request(method, url, parameters, body);
    }
}
