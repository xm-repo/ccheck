package ccheck;

import org.ccheck.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class RemoveAsyncTask extends AsyncTask<Void, String, Void> {
	
    final View rootView;   
	
	public RemoveAsyncTask(View view) {
		this.rootView = view;		
    }

	@Override
	protected Void doInBackground(Void... values) {		
		
        final Context context = rootView.getContext().getApplicationContext();
        final ListView listView = (ListView) rootView.findViewById(R.id.CHECK_LV);
        
		FileRW filerw = new FileRW(context);
		
		SparseBooleanArray chosenItems = listView.getCheckedItemPositions();
	    
		for(int i = 0; i < chosenItems.size(); i++) {

			if(chosenItems.valueAt(i)) {
				
				String url = (String) listView.getItemAtPosition(chosenItems.keyAt(i));
							
				int index = url.indexOf(' ');
				filerw.removePin(url.substring(0, (index == -1) ? url.length(): index));
				publishProgress(url);
			}
		}
				
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
    	
    	final ListView listView = (ListView) rootView.findViewById(R.id.CHECK_LV);
    	final ArrayAdapter<String> lvAdapter = (ArrayAdapter<String>) listView.getAdapter();    	 
    	
    	for(String p : progress) {
    		lvAdapter.remove(p);	
    	}
    	    	
        lvAdapter.notifyDataSetChanged();    	
    }
    
    @SuppressWarnings("unchecked")
	@Override 
    protected void onPostExecute(Void result) {    	
    	
    	final Button checkButton = (Button) rootView.findViewById(R.id.button_checkall);
	    final Button removeButton = (Button) rootView.findViewById(R.id.button_remove);
	    
	    checkButton.setEnabled(true);
	    removeButton.setEnabled(true);	    
	    
	    final ListView listView = (ListView) rootView.findViewById(R.id.CHECK_LV);
	    final ArrayAdapter<String> lvAdapter = (ArrayAdapter<String>) listView.getAdapter(); 
	    
	    listView.clearChoices();
	    lvAdapter.notifyDataSetChanged(); 
    }

}
