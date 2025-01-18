package keyhub.distributedtransactionkit.core.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import keyhub.distributedtransactionkit.core.RemoteTransactionException;
import keyhub.distributedtransactionkit.core.compensation.CompensatingTransactionStore;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface RemoteTransaction {

    static RemoteTransaction of(){
        return SingleRemoteTransaction.of();
    }

    static RemoteTransaction of(CompensatingTransactionStore store){
        return SingleRemoteTransaction.of(store);
    }

    default RemoteTransaction get(String url) {
        return request(HttpMethod.GET, url);
    }

    default RemoteTransaction get(String url, Map<String, Object> params) {
        return request(HttpMethod.GET, url, params);
    }

    default RemoteTransaction post(String url) {
        return request(HttpMethod.POST, url);
    }

    default RemoteTransaction post(String url, Object param) {
        return request(HttpMethod.POST, url, param);
    }

    default RemoteTransaction put(String url) {
        return request(HttpMethod.PUT, url);
    }

    default RemoteTransaction put(String url, Object param) {
        return request(HttpMethod.PUT, url, param);
    }

    default RemoteTransaction delete(String url) {
        return request(HttpMethod.DELETE, url);
    }

    default RemoteTransaction delete(String url, Map<String, Object> params) {
        return request(HttpMethod.DELETE, url, params);
    }

    default RemoteTransaction request(HttpMethod method, String url) {
        request(method, url, null, null);
        return this;
    }

    default RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters) {
        request(method, url, parameters, null);
        return this;
    }

    default RemoteTransaction request(HttpMethod method, String url, Object body) {
        request(method, url, null, body);
        return this;
    }

    RemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters, Object body);

    interface Result {
        <R> R toData(Class<R> returnType) throws JsonProcessingException;
        <R> List<R> toList(Class<R> returnType);
    }

    default RemoteTransaction setCompensation(String url, HttpMethod httpMethod) {
        setCompensation(url, httpMethod, null, null);
        return this;
    }

    default RemoteTransaction setCompensation(String url, HttpMethod httpMethod, Map<String, Object> parameters){
        setCompensation(url, httpMethod, parameters, null);
        return this;
    }

    default RemoteTransaction setCompensation(String url, HttpMethod httpMethod, Object body) {
        setCompensation(url, httpMethod, null, body);
        return this;
    }

    RemoteTransaction setCompensation(String url, HttpMethod httpMethod, Map<String, Object> parameters, Object body);

    RemoteTransaction.Result resolve() throws RemoteTransactionException;
}
