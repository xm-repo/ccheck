package ccheck.ssl.pinning;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

import org.ccheck.R;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class SystemKeyStore {
	
	private static final int CACERTS_FILE_SIZE = 1024 * 140;

	private static SystemKeyStore instance;
	private Context context;

	public static synchronized SystemKeyStore getInstance(Context context) {
		
		if(instance == null) {
			instance = new SystemKeyStore(context);
		}
		
		
		return instance;
	}

	private final HashMap<Principal, X509Certificate> trustRoots;
	
	final KeyStore trustStore;

	private SystemKeyStore(Context context) {
		
		final KeyStore trustStore = getTrustStore(context);
		this.trustRoots = initializeTrustedRoots(trustStore);
		this.trustStore = trustStore;	
		this.context = context;
	}
	
	public void addcert(X509Certificate certificate) {
		
		X500Principal alias = certificate.getSubjectX500Principal();
		
		try {
			trustStore.setCertificateEntry(alias.toString(), certificate);
			trustRoots.put(alias, certificate);
			 	
	    	context.deleteFile("cacerts");
	    	
	    	OutputStream outputStream = null;
	    	try {
	    		outputStream = context.openFileOutput("cacerts", Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {			
				//e.printStackTrace();
				return;
			}

	    	BufferedOutputStream bos = new BufferedOutputStream(outputStream);
	    	trustStore.store(bos, ("changeit").toCharArray());
			
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean isTrustRoot(X509Certificate certificate) {
		
		final X509Certificate trustRoot = trustRoots.get(certificate.getSubjectX500Principal());
		return (trustRoot != null) && (trustRoot.getPublicKey().equals(certificate.getPublicKey()));
	}

	public X509Certificate getTrustRootFor(X509Certificate certificate) {
		
		final X509Certificate trustRoot = trustRoots.get(certificate.getIssuerX500Principal());

		if(trustRoot == null) {
			return null;
		}

		if(trustRoot.getSubjectX500Principal().equals(certificate.getSubjectX500Principal())) {
			return null;
		}

		try {
			certificate.verify(trustRoot.getPublicKey());
		} catch (GeneralSecurityException gsException) {
			return null;
		}

		return trustRoot;		
	}

	private HashMap<Principal, X509Certificate> initializeTrustedRoots(KeyStore trustStore) {
		
		try {
			
			final HashMap<Principal, X509Certificate> trusted = new HashMap<Principal, X509Certificate>();

			for (Enumeration<String> aliases = trustStore.aliases(); aliases.hasMoreElements(); ) {
				
				final String alias = aliases.nextElement();
				final X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);

				if (certificate != null) {
					trusted.put(certificate.getSubjectX500Principal(), certificate);
				}
				
			}

			return trusted;
			
		} catch (KeyStoreException e) {
			throw new AssertionError(e);
		}
	}

	private KeyStore getTrustStore(Context context) {
		
		try {
			
			InputStream inputStream = null;
			
			File file = new File(context.getFilesDir(), "cacerts");
	    	
	    	if(file.exists()) {
	    		try {
	    			inputStream = context.openFileInput("cacerts");
	    		} catch (FileNotFoundException e) {			
	    			
	    		} 
	    	} else {
	    		inputStream = context.getResources().openRawResource(R.raw.cacerts);
	    	}
			
			final KeyStore trustStore = KeyStore.getInstance("BKS");
			final BufferedInputStream bin = 
					new BufferedInputStream(inputStream, CACERTS_FILE_SIZE);

			try {				
				
				trustStore.load(bin, "changeit".toCharArray());				
			} finally {				
				try {
					bin.close();
				} catch (IOException ioe) {
					
				}				
			}

			return trustStore;
			
		} catch(KeyStoreException kse) {
			throw new AssertionError(kse);
		} catch(NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		} catch(CertificateException e) {
			throw new AssertionError(e);
		} catch(NotFoundException e) {
			throw new AssertionError(e);
		} catch(IOException e) {
			throw new AssertionError(e);
		}
	}
}
