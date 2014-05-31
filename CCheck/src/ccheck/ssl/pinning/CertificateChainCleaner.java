package ccheck.ssl.pinning;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;

/*
 * Does the work of cleaning up a certificate chain by sifting out any
 * unrelated certificates and returning something that's signed from
 * EE to a trust anchor.
 */

class CertificateChainCleaner {

	private CertificateChainCleaner() {
	}

	public static X509Certificate[] getCleanChain(X509Certificate[] chain, SystemKeyStore systemKeyStore) throws CertificateException {
		
		final LinkedList<X509Certificate> cleanChain = new LinkedList<X509Certificate>();
		boolean trustedChain = false;
		int i;

		trustedChain = systemKeyStore.isTrustRoot(chain[0]);
		
		cleanChain.add(chain[0]);

		for(i = 1; i < chain.length; i++) {	
			
			trustedChain = (trustedChain || systemKeyStore.isTrustRoot(chain[i])); 
			
			if(isValidLink(chain[i], chain[i - 1])) {
				cleanChain.add(chain[i]);
			} else {
				break;
			}			
		}

		final X509Certificate trustRoot = systemKeyStore.getTrustRootFor(chain[i - 1]);

		if(trustRoot != null) {
			cleanChain.add(trustRoot);
			trustedChain = true;
		}

		if(trustedChain) {	
			return cleanChain.toArray(new X509Certificate[cleanChain.size()]);
		} else {
			throw new CertificateException("Didn't find a trust anchor in chain cleanup!");
		}
		
	}

	private static boolean isValidLink(X509Certificate parent, X509Certificate child) {
		
		if(!parent.getSubjectX500Principal().equals(child.getIssuerX500Principal())) {
			return false;
		}

		try {
			child.verify(parent.getPublicKey());
		} catch(GeneralSecurityException gse) {
			return false;
		}

		return true;
	}
}
