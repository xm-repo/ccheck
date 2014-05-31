package ccheck.ssl.pinning.test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.test.AndroidTestCase;
import android.util.Log;
import ccheck.ssl.pinning.PinningSSLSocketFactory;

public class PinningSSLSocketFactoryTest extends AndroidTestCase {

	public void testGoodPin() throws IOException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		
		//String[] pins = new String[] { "40c5401d6f8cbaf08b00edefb1ee87d005b3b9cd" };
		String[] pins = new String[] { "c07a98688d89fbab05640c117daa7d65b8cacc4e" };
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new PinningSSLSocketFactory(getContext(), pins), 443));

		HttpParams httpParams = new BasicHttpParams();
		ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);

		@SuppressWarnings("unused")
		HttpResponse response = httpClient.execute(new HttpGet("https://www.google.com/"));
	}

	public void testBadPin() throws UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		
		//String[] pins = new String[] { "40c5401d6f8cbaf08b00edefb1ee87d005b3b9cd" };
		String[] pins = new String[] { "c07a98688d89fbab05640c117daa7d65b8cacc4e" };
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new PinningSSLSocketFactory(getContext(), pins), 443));

		HttpParams httpParams = new BasicHttpParams();
		ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);

		try {
			@SuppressWarnings("unused")
			HttpResponse response = httpClient.execute(new HttpGet("https://www.twitter.com/"));
		} catch (IOException ioe) {
			Log.w("PinningSSLSocketFactory", ioe);
			return;
		}

		fail("No errot thrown when connecting to unpinned host!");
	}

}
