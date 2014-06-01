package ccheck;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.ccheck.R;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import ccheck.ssl.pinning.util.PinningHelper;

public class CheckAsyncTask extends AsyncTask<Void, String, Void> {

    final View rootView;   
	
	public CheckAsyncTask(View view) {
		this.rootView = view;		
    }
	
	@Override
	protected Void doInBackground(Void... args) {
		
		final Context context = rootView.getContext().getApplicationContext();
		
		FileRW filerw = new FileRW(context);
		
		for(String strurl : filerw.getPins().keySet()) {

			try {

				URL url = new URL(strurl);
				HttpsURLConnection httpsURLConnection = PinningHelper.getPinnedHttpsURLConnection(context, new String[] { filerw.getPins().get(strurl) }, url);
				httpsURLConnection.setConnectTimeout(4000);
				//httpsURLConnection.connect();
				
				//InputStream inputStream = 
				httpsURLConnection.getInputStream();
				//inputStream.close();
				//httpsURLConnectionection.disconnect();
				
				publishProgress(strurl + "  (Ok)");

			} catch (IOException ioException) {
				
				publishProgress(strurl + "  (Bad: " + ioException.getMessage() + ")");
			}
		}  		

		return null;
	}
    
    @SuppressWarnings("unchecked")
	@Override
    protected void onPreExecute() {
    	
    	final Button checkButton = (Button) rootView.findViewById(R.id.button_checkall);
    	final Button removeButton = (Button) rootView.findViewById(R.id.button_remove);
    	
    	checkButton.setEnabled(false);
    	removeButton.setEnabled(false);
    	
    	final ListView lv = (ListView) rootView.findViewById(R.id.CHECK_LV);
    	final ArrayAdapter<String> lvAdapter = (ArrayAdapter<String>) lv.getAdapter();
    	
    	lvAdapter.clear();
    	lvAdapter.notifyDataSetChanged(); 
    }
    
    @SuppressWarnings("unchecked")
	@Override    
    protected void onProgressUpdate(String... progress) {    	
    	
    	final ListView lv = (ListView) rootView.findViewById(R.id.CHECK_LV);
    	final ArrayAdapter<String> lvAdapter = (ArrayAdapter<String>) lv.getAdapter();    	
    	
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
