package ccheck.ssl.pinning.test;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.test.AndroidTestCase;
import android.util.Log;
import ccheck.ssl.pinning.util.PinningHelper;

public class PinningHelperTest extends AndroidTestCase {

	public void testGoodUrlConnection() throws IOException {
		
		//String[] pins = new String[] { "40c5401d6f8cbaf08b00edefb1ee87d005b3b9cd" }; 
		String[] pins = new String[] { "c07a98688d89fbab05640c117daa7d65b8cacc4e" };
		
		HttpsURLConnection connection = PinningHelper
				.getPinnedHttpsURLConnection(getContext(), pins, new URL("https://www.google.com/"));
		connection.getInputStream();
	}

	public void testBadUrlConnection() throws IOException {
		
		//String[] pins = new String[] { "40c5401d6f8cbaf08b00edefb1ee87d005b3b9cd" };
		String[] pins = new String[] { "c07a98688d89fbab05640c117daa7d65b8cacc4e" };
		
		HttpsURLConnection connection = PinningHelper
				.getPinnedHttpsURLConnection(getContext(), pins, new URL("https://www.twitter.com/"));

		try {
			connection.getInputStream();
		} catch (IOException ioe) {
			Log.w("PinningHelperTest", ioe);
			return;
		}

		fail("Accepted bad pin!");
	}

	public void testGoodHttpClient() throws IOException {
		
		//String[] pins = new String[] { "40c5401d6f8cbaf08b00edefb1ee87d005b3b9cd" };
		String[] pins = new String[] { "c07a98688d89fbab05640c117daa7d65b8cacc4e" };
		
		HttpClient client = PinningHelper.getPinnedHttpClient(getContext(), pins);
		client.execute(new HttpGet("https://www.google.com"));
	}

	public void testBadHttpClient() {
		
		//String[] pins = new String[] { "40c5401d6f8cbaf08b00edefb1ee87d005b3b9cd" };
		String[] pins = new String[] { "c07a98688d89fbab05640c117daa7d65b8cacc4e" };
		
		HttpClient client = PinningHelper.getPinnedHttpClient(getContext(), pins);
		
		try {
			client.execute(new HttpGet("https://www.twitter.com"));
		} catch (IOException ioe) {
			Log.w("PinningHelperTest", ioe);
			return;
		}

		fail("Accepted bad pin!");
	}

}
