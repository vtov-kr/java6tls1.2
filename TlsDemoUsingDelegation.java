import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.URL;

/**
 * TLS 1.2 communication with only selected hosts demo.
 * <p>
 * This file shows how it's done using {@link HttpsURLConnection#setDefaultSSLSocketFactory(SSLSocketFactory)}.
 * <p>
 * We have our own implementation of {@link SSLSocketFactory} - {@link TLSSocketFactory}.
 * <p>
 * In {@link TLSSocketFactory}, hosts in {@link TLSSocketFactory#TLS_1_2_HOSTS} will be connected using TLS1.2.
 * <p>
 * Other hosts, like {@link #NO_TLS_1_2_ENDPOINT} will not use TLS 1.2.
 * <p>
 * {@link TlsDemoUsingDelegation}:
 * 1. No need to call {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)} on every connection.
 * 2. But all connections will go through {@link TLSSocketFactory}.
 * 3. We have to add hosts to allow in {@link TLSSocketFactory#TLS_1_2_HOSTS}.
 * <p>
 * {@link TlsDemoSingleTarget}
 * 1. Need to call {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)} on every connection.
 * 2. But only selected connections will go through {@link TLSSocketFactory}.
 * 3. We don't need to add hosts to allow in {@link TLSSocketFactory#TLS_1_2_HOSTS}.
 * <p>
 * TLS 1.2 를 일부 host 에만 사용하는 데모입니다.
 * <p>
 * 이 파일은 {@link HttpsURLConnection#setDefaultSSLSocketFactory(SSLSocketFactory)} 을 이용하는 방법을 소개합니다.
 * <p>
 * TLS 1.2 연결을 위해 {@link SSLSocketFactory} 의 구현체인 {@link TLSSocketFactory} 를 사용합니다.
 * <p>
 * {@link TLSSocketFactory} 는 {@link TLSSocketFactory#TLS_1_2_HOSTS} 에 정의된 host 들에만 TLS 1.2를 사용하여 연결합니다.
 * <p>
 * 다른 호스트들, 예를 들어 {@link #NO_TLS_1_2_ENDPOINT} 는 TLS 1.2 를 사용하지 않습니다.
 * <p>
 * {@link TlsDemoUsingDelegation}은:
 * 1. 모든 연결에서 {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)}를 부를 필요가 없습니다.
 * 2. 하지만 모든 연결이 {@link TLSSocketFactory} 를 통하게 됩니다.
 * 3. {@link TLSSocketFactory} 가 스스로 TLS 1.2 를 쓸지를 판단하기 때문에 이를 위해 원하는 host 를
 * {@link TLSSocketFactory#TLS_1_2_HOSTS} 에 추가해줘야 합니다.
 * <p>
 * {@link TlsDemoSingleTarget}은:
 * 1. 모든 TLS 1.2 를 쓸 연결에서 {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)} 를 불러야합니다.
 * 2. 하지만 TLS 1.2 를 쓸 연결만 {@link TLSSocketFactory} 를 통하게 됩니다.
 * 3. 따라서 {@link TLSSocketFactory} 가 스스로 판단할 필요도 없으며, {@link TLSSocketFactory#TLS_1_2_HOSTS} 를 설정할 필요도 없습니다.
 */
public class TlsDemoUsingDelegation {
    private static final String NO_TLS_1_2_ENDPOINT = "https://www.google.com/";
    private static final String TLS_1_2_ENDPOINT = "https://amazing.today/";

    /**
     * Since {@link #NO_TLS_1_2_ENDPOINT} is not in {@link TLSSocketFactory#TLS_1_2_HOSTS}, this request will
     * not use TLS 1.2.
     * <p>
     * {@link #NO_TLS_1_2_ENDPOINT} 가 {@link TLSSocketFactory#TLS_1_2_HOSTS} 에 포함되어있지 않기 때문에 이 요청은 TLS 1.2 를
     * 사용하지 않습니다.
     */
    private static void noTLSrequest() throws Exception {
        System.out.println("Fire GET request to " + NO_TLS_1_2_ENDPOINT);
        connect(NO_TLS_1_2_ENDPOINT);
    }

    /**
     * Since {@link #TLS_1_2_ENDPOINT} is in {@link TLSSocketFactory#TLS_1_2_HOSTS}, this request will
     * use TLS 1.2.
     * <p>
     * {@link #NO_TLS_1_2_ENDPOINT} 가 {@link TLSSocketFactory#TLS_1_2_HOSTS} 에 포함되어있기 때문에 이 요청은 TLS 1.2 를
     * 사용합니다.
     */
    private static void TLSrequest() throws Exception {
        System.out.println("Fire GET request to " + TLS_1_2_ENDPOINT);
        connect(TLS_1_2_ENDPOINT);
    }

    private static void connect(final String target) throws Exception {
        final URL url = new URL(target);
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.getInputStream();
    }

    /**
     * Using {@link HttpsURLConnection#setDefaultSSLSocketFactory(SSLSocketFactory)}, all connection will go through
     * our {@link TLSSocketFactory}. However, connections to hosts that are not in
     * {@link TLSSocketFactory#TLS_1_2_HOSTS} will not use TLS 1.2.
     * <p>
     * {@link HttpsURLConnection#setDefaultSSLSocketFactory(SSLSocketFactory)} 를 쓰기 때문에 모든 연결은
     * {@link TLSSocketFactory} 를 거쳐갑니다. 하지만 {@link TLSSocketFactory#TLS_1_2_HOSTS} 에 없는 host 들은 기존처럼
     * TLS 1.2 를 쓰지 않습니다.
     */
    public static void main(String[] args) throws Exception {
        HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
        noTLSrequest();
        TLSrequest();
    }
}
