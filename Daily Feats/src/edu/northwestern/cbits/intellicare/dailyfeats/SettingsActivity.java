package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.HashMap;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.oauth.FitbitApi;
import edu.northwestern.cbits.intellicare.oauth.GitHubApi;
import edu.northwestern.cbits.intellicare.oauth.JawboneApi;
import edu.northwestern.cbits.intellicare.oauth.OAuthActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.activity_settings);

		Preference version = this.findPreference("app_version");

		try  
		{
			version.setTitle(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} 
		catch (NameNotFoundException e) 
		{
			LogManager.getInstance(this).logException(e);
		}
	}
	
	public void onResume()
	{
		super.onResume();

		this.refreshItems();
		
		LogManager.getInstance(this).log("opened_settings", null);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@SuppressWarnings("deprecation")
	private void refreshItems() 
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.contains("oauth_fitbit_token"))
		{
			Preference fitbit = this.findPreference("settings_fitbit_login");
			
			if (fitbit != null)
			{
				fitbit.setTitle(R.string.prefs_fitbit_logout);
				fitbit.setKey("settings_fitbit_logout");
			}
		}
		else
		{
			Preference fitbit = this.findPreference("settings_fitbit_logout");
			
			if (fitbit != null)
			{
				fitbit.setTitle(R.string.prefs_fitbit_login);
				fitbit.setKey("settings_fitbit_login");
			}
		}

		if (prefs.contains("oauth_jawbone_token"))
		{
			Preference fitbit = this.findPreference("settings_jawbone_login");
			
			if (fitbit != null)
			{
				fitbit.setTitle(R.string.prefs_jawbone_logout);
				fitbit.setKey("settings_jawbone_logout");
			}
		}
		else
		{
			Preference fitbit = this.findPreference("settings_jawbone_logout");
			
			if (fitbit != null)
			{
				fitbit.setTitle(R.string.prefs_jawbone_login);
				fitbit.setKey("settings_jawbone_login");
			}
		}

		if (prefs.contains("oauth_github_token"))
		{
			Preference fitbit = this.findPreference("settings_github_login");
			
			if (fitbit != null)
			{
				fitbit.setTitle(R.string.prefs_github_logout);
				fitbit.setKey("settings_github_logout");
			}
		}
		else
		{
			Preference fitbit = this.findPreference("settings_github_logout");
			
			if (fitbit != null)
			{
				fitbit.setTitle(R.string.prefs_github_login);
				fitbit.setKey("settings_github_login");
			}
		}
	}

	/*

		<PreferenceScreen android:title="@string/prefs_github_feat">
			<CheckBoxPreference android:title="@string/prefs_enable_github" android:key="settings_github_enabled" />
			<Preference android:title="@string/prefs_github_login" android:key="settings_github_login" />
	    </PreferenceScreen>

	 */

	public void onPause()
	{
		LogManager.getInstance(this).log("closed_settings", null);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);

		super.onPause();
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String key = preference.getKey();
		
		if (key == null)
		{
			
		}
		else if (key.equals("settings_fitbit_login"))
		{
			this.fetchFitbitAuth();
		}
		else if (key.equals("settings_fitbit_logout"))
		{
			Editor e = prefs.edit();
			
			e.remove("oauth_fitbit_token");
			e.remove("oauth_fitbit_secret");
			
			e.commit();
			
			this.refreshItems();
		}
		else if (key.equals("settings_jawbone_login"))
		{
			this.fetchJawboneAuth();
		}
		else if (key.equals("settings_jawbone_logout"))
		{
			Editor e = prefs.edit();
			
			e.remove("oauth_jawbone_token");
			e.remove("oauth_jawbone_secret");
			
			e.commit();
			
			this.refreshItems();
		}
		else if (key.equals("settings_github_login"))
		{
			this.fetchGitHubAuth();
		}
		else if (key.equals("settings_github_logout"))
		{
			Editor e = prefs.edit();
			
			e.remove("oauth_github_token");
			e.remove("oauth_github_secret");
			
			e.commit();
			
			this.refreshItems();
		}
		else if (key.equals("settings_reminder_time"))
		{
			final SettingsActivity me = this;
			
			TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener()
			{
				public void onTimeSet(TimePicker arg0, int hour, int minute) 
				{
			        Editor editor = prefs.edit();
			        
			        editor.putInt(ScheduleManager.REMINDER_HOUR, hour);
			        editor.putInt(ScheduleManager.REMINDER_MINUTE, minute);
			        editor.commit();
			        
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("hour", hour);
					payload.put("minute", minute);
					payload.put("source", "settings");
					
					payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
					LogManager.getInstance(me).log("set_reminder_time", payload);
				}
			}, prefs.getInt(ScheduleManager.REMINDER_HOUR, 18), prefs.getInt(ScheduleManager.REMINDER_MINUTE, 0), DateFormat.is24HourFormat(this));
			
			dialog.show();

			return true;
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);
		
		return super.onPreferenceTreeClick(screen, preference);
	}

	private void fetchGitHubAuth()
	{
        Intent intent = new Intent(this, OAuthActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		intent.putExtra(OAuthActivity.CONSUMER_KEY, GitHubApi.CONSUMER_KEY);
		intent.putExtra(OAuthActivity.CONSUMER_SECRET, GitHubApi.CONSUMER_SECRET);
		intent.putExtra(OAuthActivity.REQUESTER, "github");
		intent.putExtra(OAuthActivity.CALLBACK_URL, "http://tech.cbits.northwestern.edu/oauth/github");
		
		this.startActivity(intent);
	}

	private void fetchFitbitAuth() 
	{
        Intent intent = new Intent(this, OAuthActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		intent.putExtra(OAuthActivity.CONSUMER_KEY, FitbitApi.CONSUMER_KEY);
		intent.putExtra(OAuthActivity.CONSUMER_SECRET, FitbitApi.CONSUMER_SECRET);
		intent.putExtra(OAuthActivity.REQUESTER, "fitbit");
		intent.putExtra(OAuthActivity.CALLBACK_URL, "http://tech.cbits.northwestern.edu/oauth/fitbit");
		
		this.startActivity(intent);
	}

	private void fetchJawboneAuth() 
	{
        Intent intent = new Intent(this, OAuthActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		intent.putExtra(OAuthActivity.CONSUMER_KEY, JawboneApi.CONSUMER_KEY);
		intent.putExtra(OAuthActivity.CONSUMER_SECRET, JawboneApi.CONSUMER_SECRET);
		intent.putExtra(OAuthActivity.REQUESTER, "jawbone");
		intent.putExtra(OAuthActivity.CALLBACK_URL, "https://tech.cbits.northwestern.edu/oauth/jawbone");
		
		this.startActivity(intent);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) 
	{
		if ("settings_fitbit_enabled".equalsIgnoreCase(key))
		{
			String where = "(feat_name = ? OR feat_name = ? OR feat_name = ?) AND feat_level = 99";
			String[] args = { this.getString(R.string.feat_fitbit_minutes), this.getString(R.string.feat_fitbit_distance), this.getString(R.string.feat_fitbit_steps) };
			
			ContentValues values = new ContentValues();
			values.put("enabled", preferences.getBoolean("settings_fitbit_enabled", false));
			
			this.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);
		}
		if ("settings_jawbone_enabled".equalsIgnoreCase(key))
		{
			String where = "(feat_name = ?) AND feat_level = 99";
			String[] args = { this.getString(R.string.feat_jawbone_steps) };
			
			ContentValues values = new ContentValues();
			values.put("enabled", preferences.getBoolean("settings_jawbone_enabled", false));
			
			this.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);
		}
		else if ("settings_github_enabled".equalsIgnoreCase(key))
		{
			String where = "feat_name = ? AND feat_level = 99";
			String[] args = { this.getString(R.string.feat_github_checkin) };
			
			ContentValues values = new ContentValues();
			values.put("enabled", preferences.getBoolean("settings_github_enabled", false));
			
			this.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);
		}
	}
}

