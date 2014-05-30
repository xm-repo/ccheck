package ccheck;

import java.util.ArrayList;
import java.util.List;

import org.ccheck.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ccheck.utils.WiFiTester;

public class CheckSectionFragment extends Fragment {
	
	public interface onItemClickEventListener {
	    public void itemClick(String item);
	}
	
	onItemClickEventListener itemClickListener;
	
	@Override
	public void onAttach(Activity activity) {
	    
		super.onAttach(activity);
	    
	    try {
	        itemClickListener = (onItemClickEventListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString() + " must implement onItemClickEventListener");
	    }
	    
	}
	
	public static final String ARG_SECTION_NUMBER = "section_number";	

	public CheckSectionFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		final View rootView = inflater.inflate(R.layout.fragment_main_check, container, false);
		final ListView listView = (ListView) rootView.findViewById(R.id.CHECK_LV);			
		final EditText editText = (EditText) rootView.findViewById(R.id.edit_newurl);			
		final Button checkButton = (Button) rootView.findViewById(R.id.button_checkall);
		final Button removeButton = (Button) rootView.findViewById(R.id.button_remove);
		final Button fragileCheckButton = (Button) rootView.findViewById(R.id.button_fragilecheck);
		
		final List<String> urls = new ArrayList<String>();
		
		final ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this.getActivity(), 
    			android.R.layout.simple_list_item_multiple_choice, urls);
		
		listView.setAdapter(lvAdapter);	
		lvAdapter.notifyDataSetChanged();
		
		LoadAsyncTask loadAsyncTask = new LoadAsyncTask(rootView);
		loadAsyncTask.execute(); 
		
		removeButton.setOnClickListener(new View.OnClickListener() {
		    @Override
	  	    public void onClick(View v) {
			
			    RemoveAsyncTask removeAsyncTask = new RemoveAsyncTask(rootView); 			    
			    removeAsyncTask.execute();
		    }
	    });
    	
    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
    			
    			itemClickListener.itemClick(((TextView) itemClicked).getText().toString());   		    
    			
    			Toast.makeText(rootView.getContext(), ((TextView) itemClicked).getText(), Toast.LENGTH_SHORT).show();	    			
    		}
    	});  	    	
    	
    	editText.setOnKeyListener(new View.OnKeyListener() {	    		
    		@Override
    		public boolean onKey(View v, int keyCode, KeyEvent event) { 
    			
    			if(event.getAction() == KeyEvent.ACTION_DOWN) {
    				if(keyCode == KeyEvent.KEYCODE_ENTER) {
    					
    					//InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    					//imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);	    							
    						    					    					
    					String url = editText.getText().toString();
    					
    					AddAsyncTask addAsyncTask = new AddAsyncTask(rootView);
    					addAsyncTask.execute(url); 
    					
    					editText.setText("https://");   					
    					
    					return true;
    				}
    			}	    			
    			return true;
    		}
    	});
    	
        checkButton.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View v) {	
											    
			    if(!WiFiTester.isWiFiUp(rootView.getContext())) {
			    	Toast.makeText(rootView.getContext(), "We need WiFi!", Toast.LENGTH_SHORT).show();
			    	return;
			    }
				
				CheckAllAsyncTask checkAllAsyncTask = new CheckAllAsyncTask(rootView);
				checkAllAsyncTask.execute();									
			}
		});
        
        fragileCheckButton.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View v) {	
				
				if(!WiFiTester.isWiFiUp(rootView.getContext())) {
			    	Toast.makeText(rootView.getContext(), "We need WiFi!", Toast.LENGTH_SHORT).show();
			    	return;
			    }
				
				CheckAllAsyncTask checkAllAsyncTask = new CheckAllAsyncTask(rootView);
				checkAllAsyncTask.execute();									
			}
		});
		
		return rootView;
	}
}