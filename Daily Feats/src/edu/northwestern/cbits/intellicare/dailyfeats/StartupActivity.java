package edu.northwestern.cbits.intellicare.dailyfeats;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class StartupActivity extends Activity 
{
    public static final Uri URI = Uri.parse("intellicare://daily-feats/home");

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        
		if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false))
			this.startActivity(new Intent(this, CalendarActivity.class));
		else
			this.startActivity(new Intent(this, IntroActivity.class));
		
		ScheduleManager.getInstance(this);
    }

    protected void onResume() 
    {
        super.onResume();

        this.finish();
    }
}