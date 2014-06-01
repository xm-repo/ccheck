
package mitm;

import iaik.asn1.structures.AlgorithmID;
import iaik.x509.X509Certificate;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/*
 * Utility methods for creating a new signed certificate.
 */

public class SignCert {

	/*
	 * Forge certificate which is identical to the given base certificate,
	 * except is signed by the "CA" certificate in caKeyStorw, and has the associated IssuerDN.
	 * 
	 * The new cert will be signed by a the CA whose public/private keys are
	 * contained in the caKeyStore (under the alias caAlias).
	 * 
	 */

	public static X509Certificate forgeCert(KeyStore caKeyStore, char[] caKeyStorePW, String caAlias, String commonName, 
			iaik.x509.X509Certificate baseCertificate) throws Exception {
		
		java.security.Security.addProvider(new iaik.security.provider.IAIK());

		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "IAIK");

		PrivateKey pk = (PrivateKey) caKeyStore.getKey(caAlias, caKeyStorePW);

		if (pk == null) {
			
			System.out.println("no private key!");
		} else {
			
			if(MITMProxyServer.debugFlag) {
				System.out.println("pk format = " + pk.getFormat());
			}
		}
		
		Certificate tempCertificate = caKeyStore.getCertificate(caAlias);
		
		X509Certificate caCertificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(tempCertificate.getEncoded()));

		Principal issuer = caCertificate.getSubjectDN();

		//AlgorithmID alg = AlgorithmID.sha256WithRSAEncryption;
		AlgorithmID alg = AlgorithmID.dsaWithSHA;

		PublicKey subjectPubKey = caCertificate.getPublicKey();

		X509Certificate x509 = X509CertificateGenerator.generateCertificate(subjectPubKey, issuer, pk, alg, baseCertificate);

		if(MITMProxyServer.debugFlag) {
			
			System.out.println("Newly forged cert: ");
			System.out.println(x509.toString(true));
		}

		return x509;
	}

}
