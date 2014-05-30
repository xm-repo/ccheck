package ccheck;

import org.ccheck.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class SettingsSectionFragment extends Fragment {
 
		public static final String ARG_SECTION_NUMBER = "section_number";

		public String url;
		
		public SettingsSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			final View rootView = inflater.inflate(R.layout.fragment_main_settings, container, false);
			
			final Button checkButton = (Button)rootView.findViewById(R.id.button_ckeck);
			final TextView textView2 = (TextView) rootView.findViewById(R.id.text_url);
			final TextView textView = (TextView) rootView.findViewById(R.id.text_cert);
			
			textView.setMovementMethod(new ScrollingMovementMethod());
			
			checkButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					CheckAsyncTask checkAsyncTask = new CheckAsyncTask(rootView);
					//ch.execute("https://www.facebook.com");
					checkAsyncTask.execute(textView2.getText().toString());
				}
			});
			
			return rootView;
		}
	}
