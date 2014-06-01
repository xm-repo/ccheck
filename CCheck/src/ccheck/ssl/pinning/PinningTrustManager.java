
package ccheck.ssl.pinning;

import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/*
 * A TrustManager implementation that enforces Certificate "pins"
 * 
 * PinningTrustManager is layered on top of the system's default TrustManager,
 * such that the system continues to validate CA signatures for SSL connections
 * as usual. Additionally, however, PinningTrustManager will enforce certificate
 * constraints on the validated certificate chain. Specifically, it will ensure that 
 * one of an arbitrary number of specified SubjectPublicKeyInfos appears somewhere 
 * in the valid certificate chain.
 */

public class PinningTrustManager implements X509TrustManager {

	private final TrustManager[] systemTrustManagers;
	private final SystemKeyStore systemKeyStore;

	private final List<byte[]> pins = new LinkedList<byte[]>();
	private final Set<X509Certificate> cache = Collections.synchronizedSet(new HashSet<X509Certificate>());

	/*
	 * Constructs a PinningTrustManager with a set of valid pins.
	 */
	
	public PinningTrustManager(SystemKeyStore keyStore, String[] pins) {
		
		this.systemTrustManagers = initializeSystemTrustManagers(keyStore);
		this.systemKeyStore = keyStore;		

		for(String pin : pins) {
			this.pins.add(hexStringToByteArray(pin));
		}
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

	private boolean isValidPin(X509Certificate certificate)	throws CertificateException {
		
		try {
			
			final MessageDigest digest = MessageDigest.getInstance("SHA1");			
			final byte[] spki = certificate.getPublicKey().getEncoded();		
			final byte[] pin = digest.digest(spki);
			
			for(byte[] validPin : this.pins) {
				if(Arrays.equals(validPin, pin)) {
					return true;
				}
			}
			return false;
			
		} catch (NoSuchAlgorithmException nsaException) {
			throw new CertificateException(nsaException);
		}
	}

	private void checkSystemTrust(X509Certificate[] chain, String authType)	throws CertificateException {
		
		for(TrustManager systemTrustManager : systemTrustManagers) {
			((X509TrustManager) systemTrustManager).checkServerTrusted(chain, authType);
		}		
	}

	private void checkPinTrust(X509Certificate[] chain) throws CertificateException {

		final X509Certificate[] cleanChain = CertificateChainCleaner.getCleanChain(chain, systemKeyStore);

		for(X509Certificate certificate : cleanChain) {
			if(isValidPin(certificate)) {
				return;
			}
		}

		throw new CertificateException("No valid pins found in chain!");
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		
		throw new CertificateException("Client certificates not supported!");
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		
		if (cache.contains(chain[0])) {
			return;
		}
		
		// We do this so that we'll never be doing worse than the default
	    // system validation. It's duplicate work, however, and can be factored
	    // out if we make the verification below more complete.
		checkSystemTrust(chain, authType);
		checkPinTrust(chain);
		cache.add(chain[0]);		
	}

	public X509Certificate[] getAcceptedIssuers() {		
		return null;		
	}

	private byte[] hexStringToByteArray(String string) {
		
		final int len = string.length();
		final byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte)((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
		}

		return data;
	}

	public void clearCache() {
		cache.clear();
	}
}
