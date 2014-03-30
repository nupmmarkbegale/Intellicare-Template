package edu.northwestern.cbits.intellicare.dailyfeats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ScheduleManager
{
	public static final String REMINDER_HOUR = "preferred_hour";
	public static final String REMINDER_MINUTE = "preferred_minutes";
	public static final int DEFAULT_HOUR = 18;
	public static final int DEFAULT_MINUTE = 0;
	private static final String LAST_NOTIFICATION = "last_notification";

	private static ScheduleManager _instance = null;

	private Context _context = null;

	public ScheduleManager(Context context) 
	{
		this._context  = context.getApplicationContext();
		
		AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);
		
		Intent broadcast = new Intent(this._context, ScheduleHelper.class);
		PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000, pi);
	}

	public static ScheduleManager getInstance(Context context)
	{
		if (ScheduleManager._instance == null)
		{
			ScheduleManager._instance = new ScheduleManager(context.getApplicationContext());
			ScheduleManager._instance.updateSchedule();
		}
		
		return ScheduleManager._instance;
	}
	
	public void updateSchedule()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		long lastNotification = prefs.getLong(ScheduleManager.LAST_NOTIFICATION, 0);
		long now = System.currentTimeMillis();
		
    	int hour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
        int minutes = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(now);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		long scheduled = c.getTimeInMillis();
		
		if (lastNotification < scheduled && now > scheduled)
		{
			String title = this._context.getString(R.string.note_title);
			String message = this._context.getString(R.string.note_message);
			
			Intent intent = new Intent(this._context, CalendarActivity.class);

			PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

			int oldLevel = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);
			int newLevel = FeatsProvider.checkLevel(this._context, oldLevel);

			Editor e = prefs.edit();
			e.putInt(FeatsProvider.DEPRESSION_LEVEL, newLevel);
			e.commit();

			if (oldLevel > newLevel)
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("user_level", newLevel);
				payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
				
				LogManager.getInstance(this._context).log("feats_demoted", payload);

				message = this._context.getString(R.string.note_title_demoted, newLevel);
			}
			else if (newLevel > oldLevel)
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("user_level", newLevel);
				payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
				
				LogManager.getInstance(this._context).log("feats_promoted", payload);
				
				message = this._context.getString(R.string.note_title_promoted, newLevel);
			}
			
			if (oldLevel != newLevel)
			{
				ContentValues values = new ContentValues();
				values.put("enabled", true);
			
				String where = "feat_level = ?";
				String[] args = { "" + newLevel };
						
				this._context.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);
			}
			
			LogManager.getInstance(this._context).log("feats_checkin_notified", null);

			StatusNotificationManager.getInstance(this._context).notifyBigText(97531, R.drawable.ic_notification_feats, title, message, pi, StartupActivity.URI);

			e = prefs.edit();
			e.putLong(ScheduleManager.LAST_NOTIFICATION, now);
			e.commit();
		}
		
		try 
		{
			Log.e("DF", "COMMITS: " + this.todayCommits().toString(2));
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}
	
	private JSONArray todayCommits()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

        JSONArray commits = new JSONArray();

		if (prefs.getBoolean("settings_github_enabled", false))
		{
			if (prefs.contains("oauth_fitbit_token"))
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String today = sdf.format(new Date());
				
				String token = prefs.getString("oauth_fitbit_token", "");
				
				String userUri = "https://api.github.com/user?access_token=" + token;
				
				try 
				{
					URL url = new URL(userUri);
					
			        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			        String inputLine = null;
			        StringBuffer all = new StringBuffer();
			        
			        while ((inputLine = in.readLine()) != null)
			        {
			        	all.append(inputLine);
			        	all.append("\n");
			        }

			        in.close();
			        
			        JSONObject userInfo = new JSONObject(all.toString());
			        
			        String username = userInfo.getString("login");
			        
			        if (username != null)
			        {
			        	url = new URL("https://api.github.com/users/" + username + "/events?access_token=" + token);

				        in = new BufferedReader(new InputStreamReader(url.openStream()));

				        all = new StringBuffer();
				        
				        while ((inputLine = in.readLine()) != null)
				        {
				        	all.append(inputLine);
				        	all.append("\n");
				        }

				        in.close();
				        
				        JSONArray events = new JSONArray(all.toString());
				        
				        for (int i = 0; i < events.length(); i++)
				        {
				        	JSONObject event = events.getJSONObject(i);
				        	
				        	if ("PushEvent".equalsIgnoreCase(event.getString("type")))
				        	{
				        		if (event.getString("created_at").startsWith(today))
				        		{
				        			JSONArray commitObjects = event.getJSONObject("payload").getJSONArray("commits");
				        			
				        			for (int j = 0; j < commitObjects.length(); j++)
				        			{
				        				commits.put(commitObjects.getJSONObject(j));
				        			}
				        		}
				        	}
				        }
			        }
				} 
				catch (MalformedURLException e) 
				{
					LogManager.getInstance(this._context).logException(e);
				} 
				catch (IOException e) 
				{
					LogManager.getInstance(this._context).logException(e);
				} 
				catch (JSONException e) 
				{
					LogManager.getInstance(this._context).logException(e);
				}
			}
		}
		
		return commits;
	}
}
