package com.exploreca.tourfinder;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.exploreca.tourfinder.db.ToursDataSource;
import com.exploreca.tourfinder.model.Tour;
import com.exploreca.tourfinder.xml.ToursPullParser;

public class MainActivity extends ListActivity {
	
	public static final String LOGTAG="EXPLORECA";
	public static final String USERNAME="pref_username";
	public static final String VIEWIMAGE="pref_viewimages";
	
	private SharedPreferences settings;
	private OnSharedPreferenceChangeListener listener;
	private List<Tour> tours;
	
	ToursDataSource datasource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		listener = new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				MainActivity.this.refreshDisplay();
			}
		};
		settings.registerOnSharedPreferenceChangeListener(listener);
		
		datasource = new ToursDataSource(this);
		datasource.open();
		
		tours = datasource.findAll();
		if (tours.size() == 0) {
			createData();
			tours = datasource.findAll();
		}
		
//		ArrayAdapter<Tour> adapter = new ArrayAdapter<Tour>(this,
//				android.R.layout.simple_list_item_1, tours);
//		setListAdapter(adapter);

        refreshDisplay();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

        case R.id.menu_all:
            tours = datasource.findAll();
            refreshDisplay();
            break;
		case R.id.menu_cheap:
			tours = datasource.findFiltered("price <= 300", "price ASC");
            refreshDisplay();
			break;

		case R.id.menu_fancy:
            tours = datasource.findFiltered("price >= 1000", "price DESC");
            refreshDisplay();
			break;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setPreference(View v) {
		Log.i(LOGTAG, "Clicked set");
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void refreshDisplay() {
        ArrayAdapter<Tour> adapter = new ArrayAdapter<Tour>(this,
                android.R.layout.simple_list_item_1, tours);
        setListAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		datasource.open();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		datasource.close();
	}
	
	private void createData() {
		ToursPullParser parser = new ToursPullParser();
		List<Tour> tours = parser.parseXML(this);
		
		for (Tour tour : tours) {
			datasource.create(tour);
		}
		
	}
	
}
