import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.URL;

/**
 * TLS 1.2 communication with only selected hosts demo.
 * <p>
 * This file shows how it's done using {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)}.
 * <p>
 * We have our own implementation of {@link SSLSocketFactory} - {@link TLSSocketFactory}.
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
 * 이 파일은 {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)} 을 이용하는 방법을 소개합니다.
 * <p>
 * TLS 1.2 연결을 위해 {@link SSLSocketFactory} 의 구현체인 {@link TLSSocketFactory} 를 사용합니다.
 * <p>
 * {@link #connect(String)} 를 통해 생성된 연결은 Java 6 의 기존 연결을 그대로 사용하여 TLS 1.2 를 사용하지 않습니다.
 * {@link #connectTls1_2(String)} 를 통해 생성된 연결은 {@link TLSSocketFactory} 를 사용하며, TLS 1.2 를 사용합니다.
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
public class TlsDemoSingleTarget {
    private static final String NO_TLS_1_2_ENDPOINT = "https://www.google.com/";
    private static final String TLS_1_2_ENDPOINT = "https://amazing.today/";

    /**
     * Singleton factory container using enum pattern.
     * <p>
     * 열거형 패턴을 활용한 팩토리의 싱글턴 컨테이너입니다.
     */
    private enum FactoryContainer {
        INSTANCE;
        private static final TLSSocketFactory factory = new TLSSocketFactory();

        static TLSSocketFactory getFactory() {
            return factory;
        }
    }

    /**
     * We are using {@link #connect(String)} - no TLS 1.2.
     * <p>
     * {@link #connect(String)} 를 사용하기 때문에 TLS 1.2 를 사용하지 않습니다.
     */
    private static void noTLSrequest() throws Exception {
        System.out.println("Fire GET request to " + NO_TLS_1_2_ENDPOINT);
        connect(NO_TLS_1_2_ENDPOINT);
    }

    /**
     * We are using {@link #connectTls1_2(String)} - yes TLS 1.2.
     * <p>
     * {@link #connectTls1_2(String)} 를 사용하기 때문에 TLS 1.2 를 사용합니다.
     */
    private static void TLSrequest() throws Exception {
        System.out.println("Fire GET request to " + TLS_1_2_ENDPOINT);
        connectTls1_2(TLS_1_2_ENDPOINT);
    }

    /**
     * Nothing special here. No TLS 1.2.
     * <p>
     * 아무런 특별 행동을 하지 않습니다. TLS 1.2 를 사용하지 않습니다.
     */
    private static void connect(final String target) throws Exception {
        final URL url = new URL(target);
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.getInputStream();
    }

    /**
     * We are setting {@link SSLSocketFactory} using {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)}.
     * We use {@link FactoryContainer#getFactory()} which is a container of the singleton instance
     * {@link TLSSocketFactory}.
     * <p>
     * 연결에 {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)} 를 통해  {@link SSLSocketFactory} 를
     * 설정합니다. 열거형 싱글턴 컨테이너인 {@link FactoryContainer#getFactory()} 를 사용하여 {@link TLSSocketFactory} 를 가져옵니다.
     * TLS 1.2 연결이 지원됩니다.
     */
    private static void connectTls1_2(final String target) throws Exception {
        final URL url = new URL(target);
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(FactoryContainer.getFactory());
        con.setRequestMethod("GET");
        con.getInputStream();
    }

    public static void main(String[] args) throws Exception {
        noTLSrequest();
        TLSrequest();
    }
}
