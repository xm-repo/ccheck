package ccheck;

import java.util.Locale;

import org.ccheck.R;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity 
        implements ActionBar.TabListener, CheckSectionFragment.onItemClickEventListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		for(int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {			
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		
		switch(item.getItemId()) {
		case R.id.action_settings:
			mViewPager.setCurrentItem(1);
			break;
		case R.id.action_check:
			mViewPager.setCurrentItem(0);
			break;
		}
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			Fragment fragment = null;
			
			switch(position) {
			case 0:
				fragment = new CheckSectionFragment();			
				break;
			case 1:
				fragment = new SettingsSectionFragment();
				break;			
			default:
				fragment = new SettingsSectionFragment();
				break;
			}
			
			Bundle args = new Bundle();
			args.putInt(SettingsSectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			
			return fragment;
		}

		@Override
		public int getCount() {			
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			Locale locale = Locale.getDefault();
			
			switch (position) {
			case 0:
				return getString(R.string.title_section_check).toUpperCase(locale);
			case 1:
				return getString(R.string.title_section_settings).toUpperCase(locale);
			}
			
			return null;
		}
	}

	@Override
	public void itemClick(String text) {	
		
		//mViewPager.setCurrentItem(1);
		View view = mViewPager.getChildAt(1);
		
		text = text.trim();
		int f = text.indexOf(" "); 
		f = (f == -1)? 0:f; 
		
		TextView tv = (TextView) view.findViewById(R.id.text_url);
		tv.setText(text.substring(0, f));
	}   

}
