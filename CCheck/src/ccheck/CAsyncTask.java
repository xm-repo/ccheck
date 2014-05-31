package ccheck;

import org.ccheck.R;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CAsyncTask extends AsyncTask<String, String, Void> {

    final View rootView;   
	
	public CAsyncTask(View view) {
		this.rootView = view;		
    }
	
	@Override
	protected Void doInBackground(String... arg) {
		
		/*final Context context = rootView.getContext().getApplicationContext();
		FileRW filerw = new FileRW(context);
		//filerw.read();	
		
		String host = arg[0];
		//ArrayList<String> tmppins = filerw.getPins();		
		//String [] pins = tmppins.toArray(new String[tmppins.size()]);	
		String progress;
		
		try {

			URL url = new URL(host);
			HttpsURLConnection connection = PinningHelper.getPinnedHttpsURLConnection(context, pins, url);

			InputStream is = connection.getInputStream();
			
			X509Certificate [] certs = (X509Certificate[]) connection.getServerCertificates();
			if(certs == null) {
				return null;
			}
			progress = certs[certs.length - 1].toString();
			
			is.close();
			connection.disconnect();

		} catch (Exception e) {
			progress = " ";
		}

		publishProgress(progress);*/

		return null;
	}
	
	@Override
	protected void onPreExecute() {
		
		final Button checkButton = (Button)rootView.findViewById(R.id.button_ckeck);
		final Button removeButton = (Button)rootView.findViewById(R.id.button_remove);
		
		checkButton.setEnabled(false);
		removeButton.setEnabled(false);
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		
		final TextView textView = (TextView) rootView.findViewById(R.id.text_cert);
		textView.setText(values[0]);		
	}
	
	@Override
	protected void onPostExecute(Void result) {
		
		final Button checkButton = (Button)rootView.findViewById(R.id.button_ckeck);
		final Button removeButton = (Button)rootView.findViewById(R.id.button_remove);
		
		checkButton.setEnabled(true);
		removeButton.setEnabled(true);
	}

}
