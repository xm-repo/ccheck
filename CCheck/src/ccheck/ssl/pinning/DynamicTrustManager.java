package ccheck.ssl.pinning;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class DynamicTrustManager implements X509TrustManager {
	
	private final TrustManager[] systemTrustManagers;
	private final SystemKeyStore systemKeyStore;
	
    public DynamicTrustManager(SystemKeyStore keyStore) {
		
		this.systemTrustManagers = initializeSystemTrustManagers(keyStore);
		this.systemKeyStore = keyStore;		
	}
    
    private TrustManager[] initializeSystemTrustManagers(SystemKeyStore keyStore) {
		
		try {
			
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
			trustManagerFactory.init(keyStore.trustStore);
			
			return trustManagerFactory.getTrustManagers();
			
		} catch(NoSuchAlgorithmException nsaException) {			
			throw new AssertionError(nsaException);			
		} catch(KeyStoreException ksException) {			
			throw new AssertionError(ksException);
		}
		
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

		throw new CertificateException("Client certificates not supported!");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	
		try {
		
		    for(TrustManager systemTrustManager : systemTrustManagers) {
			    ((X509TrustManager)systemTrustManager).checkServerTrusted(chain, authType);
		    }
		} catch (CertificateException certException) {
			
			systemKeyStore.addcert(chain[chain.length - 1]);
			//throw new CertificateException();
		}
		
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	} 

}
