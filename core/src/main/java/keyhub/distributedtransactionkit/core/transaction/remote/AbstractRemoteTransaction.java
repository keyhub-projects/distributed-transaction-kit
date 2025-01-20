package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.transaction.single.AbstractSingleTransaction;
import org.springframework.http.HttpMethod;

import java.util.Map;

public abstract class AbstractRemoteTransaction extends AbstractSingleTransaction implements RemoteTransaction {
    protected final ObjectMapper objectMapper;

    protected AbstractRemoteTransaction(CompensationStore compensatingTransactionStore, OutboxStore outboxTransactionStore, ObjectMapper objectMapper) {
        super(compensatingTransactionStore, outboxTransactionStore);
        this.objectMapper = objectMapper;
    }

    protected AbstractRemoteTransaction(CompensationStore compensatingTransactionStore, OutboxStore outboxTransactionStore) {
        super(compensatingTransactionStore, outboxTransactionStore);
        this.objectMapper = new ObjectMapper();
    }

    protected AbstractRemoteTransaction() {
        super();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public RemoteTransaction get(String url) {
        return request(HttpMethod.GET, url);
    }

    @Override
    public RemoteTransaction get(String url, Map<String, Object> params) {
        return request(HttpMethod.GET, url, params);
    }

    @Override
    public RemoteTransaction post(String url) {
        return request(HttpMethod.POST, url);
    }

    @Override
    public RemoteTransaction post(String url, Object param) {
        return request(HttpMethod.POST, url, param);
    }

    @Override
    public RemoteTransaction put(String url) {
        return request(HttpMethod.PUT, url);
    }

    @Override
    public RemoteTransaction put(String url, Object param) {
        return request(HttpMethod.PUT, url, param);
    }

    @Override
    public RemoteTransaction delete(String url) {
        return request(HttpMethod.DELETE, url);
    }

    @Override
    public RemoteTransaction delete(String url, Map<String, Object> params) {
        return request(HttpMethod.DELETE, url, params);
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url) {
        request(method, url, null, null);
        return this;
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters) {
        request(method, url, parameters, null);
        return this;
    }

    @Override
    public RemoteTransaction request(HttpMethod method, String url, Object body) {
        request(method, url, null, body);
        return this;
    }
}
