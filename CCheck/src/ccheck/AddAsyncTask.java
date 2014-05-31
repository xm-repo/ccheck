package ccheck;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

import org.ccheck.R;

import ccheck.ssl.pinning.util.PinningHelper;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class AddAsyncTask extends AsyncTask<String, String, Void> {

	final View rootView; 
	
	public AddAsyncTask(View view) {
		this.rootView = view;
	}	
	
	@Override
	protected Void doInBackground(String... args) {
		
		String strurl = args[0];
		
		if(strurl.isEmpty()) {
			return null;
		}

		URL url = null;
		strurl = "https://www.example.com";
		try {
			
			url = new URL(strurl);
			
			if(!url.getProtocol().equals("https")) {	
				return null;
			}
			
			//url = new URL("https", "10.0.2.2", -1, "/");
			//url = new URL("https://www.example.com");
			
		} catch (MalformedURLException e) {						
			return null;
		}
		
		
		final Context context = rootView.getContext().getApplicationContext();
		
		FileRW filerw = new FileRW(context);
		
		if(filerw.containsUrl(strurl)) {
			return null;
		}
		
		String hexpin;
		
		try {
			
			HttpsURLConnection httpsURLConnection = PinningHelper.getDynamicHttpsURLConnection(context, url);
			
			httpsURLConnection.setConnectTimeout(4000);
			httpsURLConnection.connect();
			//httpsURLConnection.disconnect();
			
			//InputStream inputStream = httpsURLConnection.getInputStream();
			//inputStream.close();
			
			X509Certificate [] certs = (X509Certificate[]) httpsURLConnection.getServerCertificates();
			if(certs == null) {
				return null;
			}
						
			//!!
			X509Certificate rootcert = certs[certs.length - 1];
			
			final MessageDigest digest = MessageDigest.getInstance("SHA1");	            
            final byte[] spki = rootcert.getPublicKey().getEncoded();		
			final byte[] pin = digest.digest(spki);
			
			final char[] hexArray = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
			
			char[] hexChars = new char[pin.length * 2];
			
			for(int j = 0; j < pin.length; j++) {
		        
		    	int v = pin[j] & 0xFF;
		    	hexChars[j * 2] = hexArray[v >>> 4];
		        hexChars[j * 2 + 1] = hexArray[v & 0x0F];;	        
		    }    
	    
		    hexpin = new String(hexChars); 
		    
		} catch (IOException e) {
			
			publishProgress(e.getMessage());
			//e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			
			publishProgress(e.getMessage());
			//e.printStackTrace();
			return null;
		}
		
		publishProgress(strurl + " (New)");	
		
		filerw.insertPin(strurl, hexpin);
		filerw.save();
		
		return null;
	}
	
	@Override	
	protected void onPreExecute() {
		
		final Button checkButton = (Button) rootView.findViewById(R.id.button_checkall);
	    final Button removeButton = (Button) rootView.findViewById(R.id.button_remove);
	    
	    checkButton.setEnabled(false);
	    removeButton.setEnabled(false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(String... progress) {
		
		final ListView listView = (ListView)rootView.findViewById(R.id.CHECK_LV);
		final ArrayAdapter<String> lvAdapter = (ArrayAdapter<String>) listView.getAdapter();
		
		lvAdapter.addAll(progress);
		lvAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onPostExecute(Void result) {
		
		final Button checkButton = (Button) rootView.findViewById(R.id.button_checkall);
	    final Button removeButton = (Button) rootView.findViewById(R.id.button_remove);
	    
	    checkButton.setEnabled(true);
	    removeButton.setEnabled(true);
	}

}
