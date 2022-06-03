import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.*;

import org.bouncycastle.crypto.tls.TlsClientProtocol;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Modified to use delegation.
 * <p>
 * Only hosts in {@link #TLS_1_2_HOSTS} will use this class. Other hosts will go through {@link #defaultFactory}.
 * <p>
 * We have {@link #handshakeListener} for debugging purpose. Please feel free to delete it.
 * <p>
 * When using {@link TlsDemoSingleTarget} approach, {@link #TLS_1_2_HOSTS} and {@link #shouldUseTLS1_2(String)}
 * are not needed.
 * <p>
 * 원본 코드를 delegation 을 사용하도록 수정하였습니다.
 * <p>
 * {@link #TLS_1_2_HOSTS} 에 등록된 host 들만 이 클래스를 사용하여 연결하고, 그렇지 않은 host 들은 {@link #defaultFactory} 를 통해
 * 연결합니다.
 * <p>
 * {@link #handshakeListener} 는 디버깅 목적으로만 존재합니다. 삭제하셔도 무방합니다.
 * <p>
 * {@link TlsDemoSingleTarget} 접근을 사용하시면 {@link #TLS_1_2_HOSTS} 와 {@link #shouldUseTLS1_2(String)} 는 필요하지 않습니다.
 */
public class TLSSocketFactory extends SSLSocketFactory {
    private static final SSLSocketFactory defaultFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    /**
     * Hosts to use TLS 1.2. When using {@link TlsDemoUsingDelegation} approach, should be deleted.
     * <p>
     * TLS 1.2 를 사용할 호스트입니다. {@link TlsDemoUsingDelegation} 방식으로 이 클래스를 사용하시면 삭제하셔도 무방합니다.
     */
    private static final String[] TLS_1_2_HOSTS = new String[]{"amazing.today", "vtov.studio"};
    /**
     * Listener for debugging purpose. Should be deleted in production.
     * <p>
     * 디버깅 목적으로 존재하는 리스너입니다. 라이브 서비스에서는 삭제되어야 합니다.
     */
    private static final HandshakeCompletedListener handshakeListener = new HandshakeCompletedListener() {
        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            final SSLSession session = event.getSession();
            System.out.println("Handshake completed with " + session.getPeerHost() + " using " + session.getProtocol());
        }
    };

    // default socket timeout (0:infinite)
    public static final int SOCKET_TIMEOUT = 0;

    // add bouncycastle provider
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // random number generator
    private SecureRandom secureRandom;
    private boolean selfSignPass = false;
    private int soTimeout = SOCKET_TIMEOUT;

    public TLSSocketFactory() {
        secureRandom = new SecureRandom();
    }

    public TLSSocketFactory(boolean selfSignPass) {
        this();
        this.selfSignPass = selfSignPass;
    }

    public TLSSocketFactory(boolean selfSignPass, int soTimeout) {
        this(selfSignPass);
        this.setSoTimeout(soTimeout);
    }

    public boolean isSelfSignPass() {
        return selfSignPass;
    }

    public void setSelfSignPass(boolean selfSignPass) {
        this.selfSignPass = selfSignPass;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    private static Socket attachHandshakeListener(final Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).addHandshakeCompletedListener(handshakeListener);
        }
        return socket;
    }

    /**
     * When using {@link TlsDemoUsingDelegation} approach, should be deleted.
     * <p>
     * {@link TlsDemoUsingDelegation} 방식으로 사용하시면 삭제되어야합니다.
     */
    private static boolean shouldUseTLS1_2(final String host) {
        for (final String tlsHost : TLS_1_2_HOSTS) {
            if (host.contains(tlsHost)) return true;
        }
        return false;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        if (!shouldUseTLS1_2(host)) {
            return attachHandshakeListener(defaultFactory.createSocket(socket, host, port, autoClose));
        }

        if (socket == null) {
            socket = new Socket();
        }
        socket.setSoTimeout(this.soTimeout);

        if (!socket.isConnected()) {
            socket.connect(new InetSocketAddress(host, port));
        }
        TlsClientProtocol tlsClientProtocol = new TlsClientProtocol(socket.getInputStream(),
                socket.getOutputStream(), this.secureRandom);
        return _createSSLSocket(host, port, tlsClientProtocol);
    }

    private SSLSocket _createSSLSocket(String host, int port, TlsClientProtocol tlsClientProtocol) {
        final SSLSocket socket = new TLSSocket(host, port, tlsClientProtocol, selfSignPass);
        attachHandshakeListener(socket);
        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return TLSSocket.CIPHER_SUITES;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return TLSSocket.CIPHER_SUITES;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        if (!shouldUseTLS1_2(host)) {
            return attachHandshakeListener(defaultFactory.createSocket(host, port));
        }
        return createSocket(null, host, port, false);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return attachHandshakeListener(defaultFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        if (!shouldUseTLS1_2(host)) {
            return attachHandshakeListener(defaultFactory.createSocket(host, port, localHost, localPort));
        }
        return null;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        return attachHandshakeListener(defaultFactory.createSocket(arg0, arg1, arg2, arg3));
    }

}
