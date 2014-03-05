package edu.northwestern.cbits.intellicare.mantra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationHelper extends BroadcastReceiver 
{
	public void onReceive(Context context, Intent intent) 
	{
		ScheduleManager manager = ScheduleManager.getInstance(context);
		
		manager.updateSchedule();
	}
}
