package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import org.springframework.http.HttpMethod;

import java.util.Map;

public interface RemoteTransaction extends SingleTransaction {

    static RemoteTransaction of(){
        return new SimpleRemoteTransaction();
    }

    static RemoteTransaction of(CompensationStore store, OutboxStore outboxStore){
        return new SimpleRemoteTransaction(store, outboxStore);
    }

    static RemoteTransaction of(CompensationStore store, OutboxStore outboxStore, ObjectMapper objectMapper){
        return new SimpleRemoteTransaction(store, outboxStore, objectMapper);
    }

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
}
