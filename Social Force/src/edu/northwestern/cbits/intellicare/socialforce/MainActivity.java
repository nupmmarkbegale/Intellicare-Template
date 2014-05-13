package edu.northwestern.cbits.intellicare.socialforce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
	public enum UserState
	{
	    LONELY, BORED, ADVICE, HELP, HAPPY
	}

	protected UserState _state = UserState.HAPPY;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.getSupportActionBar().setSubtitle(R.string.subtitle_main);
        
        final MainActivity me = this;
        
        RadioGroup radios = (RadioGroup) this.findViewById(R.id.radio_user_states);
        
        radios.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			public void onCheckedChanged(RadioGroup arg0, int id)
			{
				if (id == R.id.button_lonely)
				{
					me._state  = UserState.LONELY;
				}
				else if (id == R.id.button_bored)
				{
					me._state = UserState.BORED;
				}
				else if (id == R.id.button_advice)
				{
					me._state = UserState.ADVICE;
				}
				else if (id == R.id.button_help)
				{
					me._state = UserState.HELP;
				}
				else
				{
					me._state = UserState.HAPPY;
				}
				
				me.refreshBubbles();
			}
        });
        
        ScheduleManager.getInstance(this);
    }        
    
    protected void onResume()
    {
    	super.onResume();
    	
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false) == false)
        {
	        Intent introIntent = new Intent(this, IntroActivity.class);
	        this.startActivity(introIntent);
        }
        else if (prefs.getBoolean(RatingActivity.CONTACTS_RATED, false) == false)
        {
	        Intent introIntent = new Intent(this, RatingActivity.class);
	        this.startActivity(introIntent);
        }
        
        this.refreshBubbles();
    }

    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void refreshBubbles() 
    {
		WebView graphView = (WebView) this.findViewById(R.id.network_visualization);
		graphView.getSettings().setJavaScriptEnabled(true);
		graphView.getSettings().setBuiltInZoomControls(true);
		graphView.getSettings().setDisplayZoomControls(false);
		graphView.getSettings().setLoadWithOverviewMode(true);
		graphView.getSettings().setUseWideViewPort(true);
		graphView.setInitialScale(1);
		
		graphView.addJavascriptInterface(this, "android");
		graphView.loadDataWithBaseURL("file:///android_asset/viz/", MainActivity.generateBubbles(this, this._state), "text/html", null, null);
	}
    
    @JavascriptInterface
    public void selectByName(String name)
    {
		List<ContactRecord> contacts = ContactCalibrationHelper.fetchContactRecords(this);
		
		final List<ContactRecord> positives = new ArrayList<ContactRecord>();
		
		for (ContactRecord record : contacts)
		{
			if (record.level >= 0 && record.level < 3)
				positives.add(record);
		}
		
		for (ContactRecord record : positives)
		{
			if (name.equals(record.name))
			{
				Intent intent = new Intent(this, ScheduleActivity.class);
				intent.putExtra(ScheduleActivity.CONTACT_KEY, record.key);
				
				this.startActivity(intent);
			}
		}
    }

	public static String generateBubbles(Context context, UserState state) 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = context.getAssets().open("viz/home_bubbles.html");

		    BufferedReader in = new BufferedReader(new InputStreamReader(html));

		    String str = null;

		    while ((str = in.readLine()) != null) 
		    {
		    	buffer.append(str);
		    	buffer.append(System.getProperty("line.separator"));
		    }

		    in.close();
		} 
		catch (IOException e) 
		{
			LogManager.getInstance(context).logException(e);
		}

		String graphString = buffer.toString();

		try 
		{
			JSONObject graphValues = MainActivity.bubbleValues(context, state);

			graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		} 
		
		return graphString;
	}

	private static JSONObject bubbleValues(Context context, UserState state) throws JSONException 
	{
		/*
		 * 
		 * var root = {
  "name": "contacts",
  "children": [
    {"name": "AgglomerativeCluster", "size": 3938},
    {"name": "CommunityStructure", "size": 3812},
    {"name": "HierarchicalCluster", "size": 3938},
    {"name": "MergeEdge", "size": 3938}
  ]
};
		 */
		
		JSONObject bubbles = new JSONObject();

		bubbles.put("name", "contacts");
		
		JSONArray children = new JSONArray();

		List<ContactRecord> contacts = ContactCalibrationHelper.fetchContactRecords(context);
		
		for (ContactRecord contact : contacts)
		{
			if (contact.level <= 2 && contact.level >= 0 && contact.name != null && contact.name.trim().length() > 0)
			{
				JSONObject bubble = new JSONObject();
				bubble.put("name", contact.name);
				bubble.put("size", contact.count);
				
				JSONArray colors = new JSONArray();
				boolean include = false;
				
				if (ContactCalibrationHelper.isAdvice(context, contact) && (state == UserState.ADVICE || state == UserState.HAPPY))
				{
					colors.put("#0099CC");
					include = true;
				}
				
				if (ContactCalibrationHelper.isCompanion(context, contact) && (state == UserState.LONELY || state == UserState.HAPPY))
				{
					colors.put("#9933CC");
					include = true;
				}

				if (ContactCalibrationHelper.isEmotional(context, contact) && (state == UserState.HELP || state == UserState.HAPPY))
				{
					colors.put("#CC0000");
					include = true;
				}
				
				if (ContactCalibrationHelper.isPractical(context, contact) && (state == UserState.BORED || state == UserState.HAPPY))
				{
					colors.put("#669900");
					include = true;
				}

				if (colors.length() == 0)
				{
					colors.put("#808080");
				}
				
				bubble.put("colors", colors);
				
				if (include)
					children.put(bubble);
			}
		}
		
		bubbles.put("children", children);
		
		return bubbles;
	}

	public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	int itemId = item.getItemId();
    	
		if (itemId == R.id.action_settings)
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		else if (itemId == R.id.action_rating)
		{
			Intent intent = new Intent(this, RatingActivity.class);
			this.startActivity(intent);
		}
		else if (itemId == R.id.action_network)
		{
			Intent intent = new Intent(this, NetworkActivity.class);
			this.startActivity(intent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));

        return super.onOptionsItemSelected(item);
    }
}
