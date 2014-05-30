package ccheck.ssl.pinning.util;

import android.content.Context;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import ccheck.ssl.pinning.DynamicTrustManager;
import ccheck.ssl.pinning.FragileTrustManager;
import ccheck.ssl.pinning.PinningSSLSocketFactory;
import ccheck.ssl.pinning.PinningTrustManager;
import ccheck.ssl.pinning.SystemKeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class PinningHelper {

	public static HttpClient getPinnedHttpClient(Context context, String[] pins) {

		try {
			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", new PinningSSLSocketFactory(context, pins), 443));

			HttpParams httpParams = new BasicHttpParams();
			ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
			
			return new DefaultHttpClient(connectionManager, httpParams);
			
		} catch(UnrecoverableKeyException e) {
			throw new AssertionError(e);
		} catch(KeyManagementException e) {
			throw new AssertionError(e);
		} catch(NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		} catch(KeyStoreException e) {
			throw new AssertionError(e);
		}
	}

	public static HttpsURLConnection getPinnedHttpsURLConnection(Context context, String[] pins, URL url) 
			throws IOException {
		
		try {
			
			if(!url.getProtocol().equals("https")) {				
				throw new IllegalArgumentException("Attempt to construct pinned non-https connection!");				
			}

			TrustManager[] trustManagers = new TrustManager[1];
			trustManagers[0] = new PinningTrustManager(SystemKeyStore.getInstance(context), pins);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			//SSLContext sslContext = SSLContext.getDefault();
			sslContext.init(null, trustManagers, null);

			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

			return urlConnection;
			
		} catch (NoSuchAlgorithmException nsae) {
			throw new AssertionError(nsae);
		} catch (KeyManagementException e) {
			throw new AssertionError(e);
		}
	}
	
    public static HttpsURLConnection getDynamicHttpsURLConnection(Context context, URL url) 
    		throws IOException {
		
        try {
			
			if(!url.getProtocol().equals("https")) {				
				throw new IllegalArgumentException("Attempt to construct dynamic non-https connection!");				
			}

			TrustManager[] trustManagers = new TrustManager[1];
			trustManagers[0] = new DynamicTrustManager(SystemKeyStore.getInstance(context));

			SSLContext sslContext = SSLContext.getInstance("TLS");
			//SSLContext sslContext = SSLContext.getDefault();
			sslContext.init(null, trustManagers, null);

			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

			return urlConnection;
			
		} catch (NoSuchAlgorithmException nsae) {
			throw new AssertionError(nsae);
		} catch (KeyManagementException e) {
			throw new AssertionError(e);
		}		
	}
    
    public static HttpsURLConnection getFragileHttpsURLConnection(Context context, URL url) 
    		throws IOException {
		
        try {
			
			if(!url.getProtocol().equals("https")) {				
				throw new IllegalArgumentException("Attempt to construct fragile non-https connection!");				
			}

			TrustManager[] trustManagers = new TrustManager[1];
			trustManagers[0] = new FragileTrustManager();

			SSLContext sslContext = SSLContext.getInstance("TLS");
			//SSLContext sslContext = SSLContext.getDefault();
			sslContext.init(null, trustManagers, null);

			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

			return urlConnection;
			
		} catch (NoSuchAlgorithmException nsae) {
			throw new AssertionError(nsae);
		} catch (KeyManagementException e) {
			throw new AssertionError(e);
		}		
	}
	
}
