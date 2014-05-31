
package ccheck.ssl.pinning;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

/*
 * A standard SSL Socket Factory that uses an pinning trust manager.
 */

public class PinningSSLSocketFactory extends SSLSocketFactory {

	private final javax.net.ssl.SSLSocketFactory pinningSocketFactory;

	/*
	 * Constructs a PinningSSLSocketFactory with a set of valid pins.
	 * 
	 * pins is an array of encoded pins to match a seen certificate
     * chain against. A pin is a hex-encoded hash of a X.509 certificate's
     * SubjectPublicKeyInfo.
	 */
	
	public PinningSSLSocketFactory(Context context, String[] pins) 
			throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {		
		
		super(null);

		final SystemKeyStore keyStore = SystemKeyStore.getInstance(context);
		final SSLContext pinningSslContext = SSLContext.getInstance("TLS");
		//final SSLContext pinningSslContext = SSLContext.getDefault();
		final TrustManager[] pinningTrustManagers = initializePinningTrustManagers(keyStore, pins);

		pinningSslContext.init(null, pinningTrustManagers, null);
		this.pinningSocketFactory = pinningSslContext.getSocketFactory();
		
	}

	@Override
	public Socket createSocket() throws IOException {
		return pinningSocketFactory.createSocket();
	}

	@Override
	public Socket connectSocket(final Socket socket, final String host, final int port, 
			final InetAddress localAddress, int localPort, final HttpParams httpParams) throws IOException {
		
		final SSLSocket sslSocket = (SSLSocket) ((socket != null) ? socket : createSocket());

		if((localAddress != null) || (localPort > 0)) {
			
			if(localPort < 0) {
				localPort = 0;
			}

			sslSocket.bind(new InetSocketAddress(localAddress, localPort));
		}

		final int connTimeout = HttpConnectionParams.getConnectionTimeout(httpParams);
		final int soTimeout = HttpConnectionParams.getSoTimeout(httpParams);

		final InetSocketAddress remoteAddress = new InetSocketAddress(host,	port);
		sslSocket.connect(remoteAddress, connTimeout);
		sslSocket.setSoTimeout(soTimeout);

		try {
			
			SSLSocketFactory.STRICT_HOSTNAME_VERIFIER.verify(host, sslSocket);
			
		} catch (IOException ioException) {
			
			try {				
				sslSocket.close();				
			} catch (Exception ignored) {
				
			}
			
			throw ioException;
		}

		return sslSocket;
	}

	@Override
	public Socket createSocket(final Socket socket, final String host, int port, final boolean autoClose) throws IOException {
		
		//RFC 2818 port 443
		port = (port == -1) ? 443 : port;

		final SSLSocket sslSocket = (SSLSocket) pinningSocketFactory.createSocket(socket, host, port, autoClose);
		SSLSocketFactory.STRICT_HOSTNAME_VERIFIER.verify(host, sslSocket);
		
		return sslSocket;
		
	}

	@Override
	public void setHostnameVerifier(X509HostnameVerifier hostnameVerifier) {		
		throw new IllegalArgumentException("Only strict hostname verification (default) is supported!");		
	}

	@Override
	public X509HostnameVerifier getHostnameVerifier() {
		return SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
	}

	private TrustManager[] initializePinningTrustManagers(SystemKeyStore keyStore, String[] pins) {
		
		final TrustManager[] trustManagers = new TrustManager[1];
		trustManagers[0] = new PinningTrustManager(keyStore, pins);

		return trustManagers;		
	}
	
}

