
package mitm;

import iaik.asn1.structures.AlgorithmID;
import iaik.x509.X509Certificate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

/*
 * An utility class that provides a method for generating a signed
 * X.509 certificate from a given base certificate.  All fields of the
 * base certificate are preserved, except for the IssuerDN, the
 * public key, and the signature.
 */
public class X509CertificateGenerator {

	public static X509Certificate generateCertificate(PublicKey subjectPublicKey, Principal issuerName,
			PrivateKey issuerPrivateKey, AlgorithmID algorithm, X509Certificate baseCert) {
		
		X509Certificate certificate = null;

		try {
			
			certificate = new X509Certificate(baseCert.getEncoded());
			certificate.setPublicKey(subjectPublicKey);
			certificate.setIssuerDN(issuerName);
			certificate.sign(algorithm, issuerPrivateKey);
			
		} catch (InvalidKeyException e) {
			
			System.err.println("X509 Certificate Generation Error: Invalid Key");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			
			System.err.println("X509 Certificate Generation Error: No Such Algorithm");
			e.printStackTrace();
		} catch (CertificateException e) {
			
			System.err.println("X509 Certificate Generation Error: Certificate Exception");
			e.printStackTrace();
		}
		
		return certificate;
	}

}
