package ccheck;

import org.ccheck.R;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoadAsyncTask extends AsyncTask<Void, String, Void> {

    final View rootView;   
	
	public LoadAsyncTask(View view) {
		this.rootView = view;		
    }
	
	@Override
	protected Void doInBackground(Void... params) {
		
		final Context context = rootView.getContext().getApplicationContext();
		
		FileRW filerw = new FileRW(context);
		
		for(String url : filerw.getPins().keySet()) {
			publishProgress(url);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(String... values) {
		
		final ListView listView = (ListView) rootView.findViewById(R.id.CHECK_LV);
    	final ArrayAdapter<String> lvAdapter = (ArrayAdapter<String>) listView.getAdapter();
    	
    	lvAdapter.addAll(values);
    	lvAdapter.notifyDataSetChanged(); 
	}

}
