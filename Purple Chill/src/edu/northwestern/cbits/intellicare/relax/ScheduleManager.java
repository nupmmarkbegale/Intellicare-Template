package edu.northwestern.cbits.intellicare.relax;

import java.security.SecureRandom;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ScheduleManager
{
	private static ScheduleManager _instance = null;

	private Context _context = null;
	private SecureRandom _random = new SecureRandom();

	public ScheduleManager(Context context) 
	{
		this._context  = context;
		
		AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);
		
		Intent broadcast = new Intent(this._context, ScheduleHelper.class);
		
		PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 30000, pi);
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
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
	
	private void remind()
	{
		LogManager.getInstance(this._context).log("reminder_shown", null);

		Intent intent = new Intent(this._context, IndexActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this._context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
		
		String title = this._context.getString(R.string.reminder_title);
		String message = this._context.getString(R.string.reminder_message);
		
		StatusNotificationManager.getInstance(this._context).notifyBigText(900, R.drawable.ic_reminder, title, message, pi);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		Editor e = prefs.edit();
		e.putLong("config_last_reminder", System.currentTimeMillis());
		e.commit();
	}
	
	public void updateSchedule()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		long lastReminder = prefs.getLong("config_last_reminder", 0);
		
		String noteType = prefs.getString("config_notification_type", "random");
		int day = Integer.parseInt(prefs.getString("config_notification_day", "4"));
		int hour = Integer.parseInt(prefs.getString("config_notification_hour", "12"));
		
		Calendar calendar = Calendar.getInstance();
		int thisDay = calendar.get(Calendar.DAY_OF_WEEK);
		int thisHour = calendar.get(Calendar.HOUR_OF_DAY);
		
		long now = System.currentTimeMillis();
		
		if (now - lastReminder > (1000 * 60 * 60 * 2))
		{
			if ("week".equals(noteType) && thisDay == day && thisHour == hour)
				this.remind();
			else if ("day".equals(noteType) && thisHour == hour)
				this.remind();
			else if (this._random.nextDouble() < 0.01)
				this.remind();
		}
	}
}
