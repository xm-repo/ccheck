
package mitm;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class MITMSSLSocketFactory implements MITMSocketFactory {
	
	final ServerSocketFactory m_serverSocketFactory;
	final SocketFactory m_clientSocketFactory;
	final SSLContext m_sslContext;

	/*
	 * This constructor will create an SSL server socket factory that is initialized with a fixed CA certificate
	 */
	
	public MITMSSLSocketFactory() throws IOException, GeneralSecurityException {
		
		m_sslContext = SSLContext.getInstance("TLS");

		final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

		final String keyStoreFile = System.getProperty(JSSEConstants.KEYSTORE_PROPERTY);
		final char[] keyStorePassword = System.getProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "").toCharArray();
		final String keyStoreType = System.getProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "JKS");

		final KeyStore keyStore;

		if(keyStoreFile != null) {
			
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
		} else {
			
			keyStore = null;
		}

		keyManagerFactory.init(keyStore, keyStorePassword);

		m_sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new TrustEveryone() }, null);

		m_clientSocketFactory = m_sslContext.getSocketFactory();
		m_serverSocketFactory = m_sslContext.getServerSocketFactory();
	}

	/*
	 * This constructor will create an SSL server socket factory that is initialized 
	 * with a forged server certificate that is issued by the proxy "CA certificate".
	 */
	
	public MITMSSLSocketFactory(String remoteCN, iaik.x509.X509Certificate remoteServerCert) throws Exception {
		
		m_sslContext = SSLContext.getInstance("TLS");

		final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

		final String keyStoreFile = System.getProperty(JSSEConstants.KEYSTORE_PROPERTY);
		final char[] keyStorePassword = System.getProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "").toCharArray();
		final String keyStoreType = System.getProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "JKS");

		final KeyStore keyStore;

		assert keyStoreFile != null;

		keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);

		PrivateKey pk = (PrivateKey) keyStore.getKey(JSSEConstants.DEFAULT_ALIAS, keyStorePassword);

		iaik.x509.X509Certificate newCertificate = SignCert.forgeCert(keyStore, keyStorePassword, 
				JSSEConstants.DEFAULT_ALIAS, remoteCN, remoteServerCert);

		KeyStore newKeyStore = KeyStore.getInstance("JKS");
		newKeyStore.load(null, null);

		newKeyStore.setKeyEntry(JSSEConstants.DEFAULT_ALIAS, pk, keyStorePassword, new Certificate[] { newCertificate });

		keyManagerFactory.init(newKeyStore, keyStorePassword);

		m_sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new TrustEveryone() }, null);

		m_clientSocketFactory = m_sslContext.getSocketFactory();
		m_serverSocketFactory = m_sslContext.getServerSocketFactory();
	}

	public final ServerSocket createServerSocket(String localHost, int localPort, int timeout) throws IOException {
		
		final SSLServerSocket socket = (SSLServerSocket) m_serverSocketFactory
				.createServerSocket(localPort, 50, InetAddress.getByName(localHost));

		socket.setSoTimeout(timeout);

		//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

		return socket;
	}

	public final Socket createClientSocket(String remoteHost, int remotePort) throws IOException {
		
		final SSLSocket socket = (SSLSocket) m_clientSocketFactory.createSocket(remoteHost, remotePort);

		//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

		socket.startHandshake();

		return socket;
	}

	private static class TrustEveryone implements X509TrustManager {
		
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
			
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) {
			
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}
    
