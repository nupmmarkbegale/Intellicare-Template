    package edu.northwestern.cbits.intellicare.mantra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.activities.IndexActivity;
import edu.northwestern.cbits.intellicare.mantra.activities.ProgressActivity;
import edu.northwestern.cbits.intellicare.mantra.activities.ReviewActivity;
//import edu.northwestern.cbits.intellicare.mantra.activities.TransparentActivity;

/**
 * Sets an alarm to handle a wakelock, then run some code.
 * Src: http://stackoverflow.com/questions/4459058/alarm-manager-example
 * @author mohrlab
 *
 */
public class NotificationAlarm extends BroadcastReceiver 
{	
	private static final int NOTE_ID = 8753;

	public static final String _12H_RENOTIFICATION = "12hRenotification";
	private static final String CN = "NotificationAlarm";

	
	private static boolean isAlreadyCalled = false;
	
     @Override
     public void onReceive(Context context, Intent intent) 
     {
    	 // get an Android wake-lock
    	 Log.d(CN+".onReceive", "entered; context = " + context.toString() + ": intent = " + intent.toString());
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();

         // Put here YOUR code.ll,
         Log.d(CN+".onReceive","alarm firing!");

         // get time bounds for notification
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         int startHour = sp.getInt(Constants.REMINDER_START_HOUR, Constants.DEFAULT_HOUR);
         int startMinute = sp.getInt(Constants.REMINDER_START_MINUTE, Constants.DEFAULT_MINUTE);
         int endHour = sp.getInt(Constants.REMINDER_END_HOUR, Constants.DEFAULT_HOUR);
         int endMinute = sp.getInt(Constants.REMINDER_END_MINUTE, Constants.DEFAULT_MINUTE);
         Calendar currentCalendarInstance = Calendar.getInstance();
         Calendar currentCalendarInstanceMinusPollingRate = (Calendar) currentCalendarInstance.clone();
         currentCalendarInstanceMinusPollingRate.add(Calendar.MINUTE, -(Constants.IMAGE_SCAN_RATE_MINUTES));
         int currHour = currentCalendarInstance.get(Calendar.HOUR_OF_DAY);
         int currMin = currentCalendarInstance.get(Calendar.MINUTE);
         
         // beginning-of-day notification 
         if		 (currHour == startHour && currMin == startMinute) {
        	 Log.d(CN+".onReceive", "at h = " + currHour + ", m = " + currMin + ", MAKE STARTING NOTIFICATION");
        	 
        	 String message = context.getString(R.string.notification_start_day);

        	 // put the user's list of mantra boards in the notification
        	 MantraBoardCursor mantraItemCursor = MantraBoardManager.get(context).queryMantraBoards();
        	 ArrayList<String> al = new ArrayList<String>();
        	 while(mantraItemCursor.moveToNext()) {
        		 MantraBoard mantra = MantraBoardManager.get(context).getMantraBoard(mantraItemCursor.getLong(mantraItemCursor.getColumnIndex("_id")));
        		 
        		 message += "\n" + mantra.getMantra();
        	 }
        	 
        	 NotificationAlarm.makeNotification(context, message, R.drawable.abc_ic_go, null, NotificationAlarm.NOTE_ID, null);
         }
         // end-of-day notification
         else if (currHour == endHour && currMin == endMinute) {
        	 Log.d(CN+".onReceive", "at h = " + currHour + ", m = " + currMin + ", MAKE ENDING NOTIFICATION");
        	 
        	 // v1: display ReviewActivity as a whole activity unto itself
        	 PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, ReviewActivity.class), 0);
        	 Uri u = ReviewActivity.activityUri(context);

        	 NotificationAlarm.makeNotification(context, context.getString(R.string.notification_end_day), R.drawable.abc_ic_go, pi, NotificationAlarm.NOTE_ID, u);
         }
         
         boolean intentHasDelayedAlarmDate = intent.getExtras().containsKey(DELAYED_ALARM_DATE_KEY);
         
         // if it's time to scan for an image
         if (currMin % Constants.IMAGE_SCAN_RATE_MINUTES == 0) {
        	 
        	 ArrayList<String> newImageIds = getCameraImagesSinceDate(context, currentCalendarInstanceMinusPollingRate.getTime());
        	 if(newImageIds.size() > 0) {
        		 Log.d(CN+".onReceive", "intentHasDelayedAlarmDate = " + intentHasDelayedAlarmDate);
        		 if(!intentHasDelayedAlarmDate) {
        			 callCounter++;
        			 Log.d(CN+".onReceive", "callCounter = " + callCounter);

            		 // from TransparentActivity...
            		 Intent progressActivityIntent = new Intent(context, ProgressActivity.class);
            		 progressActivityIntent.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, true);

                	 PendingIntent pi = PendingIntent.getActivity(context, 0, progressActivityIntent, 0);
                	 Uri u = ProgressActivity.activityUri(context);

                	 NotificationAlarm.makeNotification(context, context.getString(R.string.note_add_images), R.drawable.abc_ic_go, pi, NotificationAlarm.NOTE_ID, u);

            		 Log.d(CN+".onReceive", "1 New photo found; making notification...");
            		 
        			 // set an alarm 12h in the future that displays a reminder notification
            		 Calendar c = Calendar.getInstance();
            		 c.add(Calendar.SECOND, INTERVAL_SCALAR);		// for dev/test
            		 long t = c.getTimeInMillis();
            		 AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            		 Intent delayedAlarmDateIntent = new Intent(context, NotificationAlarm.class);
            		 delayedAlarmDateIntent.putExtra(DELAYED_ALARM_DATE_KEY, (new Date()).getTime());
            		 Log.d(CN+".onReceive", "delayedAlarmDateIntent = " + delayedAlarmDateIntent + "; extras = " + delayedAlarmDateIntent.getExtras().toString());
            		 alarmSender = PendingIntent.getBroadcast(context, 0, delayedAlarmDateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            		 am.set(AlarmManager.RTC_WAKEUP, t, alarmSender);
        		 }
        	 }
         }

         // check for and handle the delayed notification
         long delayedAlarmDateMillis = !intentHasDelayedAlarmDate ? -1 : intent.getExtras().getLong(DELAYED_ALARM_DATE_KEY);
         long currDateMillis = (new Date()).getTime();
         boolean waitedLongEnough = (delayedAlarmDateMillis - currDateMillis) >= NUM_MILLIS_IN_INTERVAL;
         Log.d(CN+".onReceive", "*** waitedLongEnough = " + waitedLongEnough + "; intentHasDelayedAlarmDate = " + intentHasDelayedAlarmDate + "; delayedAlarmDateMillis = " + (new Date(delayedAlarmDateMillis)).toString() + "; currDateMillis = " + (new Date(currDateMillis)).toString());
         
         if(waitedLongEnough) {
    		 Intent progressActivityIntent = new Intent(context, ProgressActivity.class);
    		 progressActivityIntent.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, true);

        	 PendingIntent pi = PendingIntent.getActivity(context, 0, progressActivityIntent, 0);
        	 Uri u = ProgressActivity.activityUri(context);

    		 Log.d(CN+".onReceive", "2 New photo found; making notification...");
        	 NotificationAlarm.makeNotification(context, context.getString(R.string.note_add_images), R.drawable.abc_ic_go, pi, NotificationAlarm.NOTE_ID, u);
         }
         
         wl.release();
         Log.d(CN+".onReceive","exiting");
     }
     
     public static int callCounter = 0;
     public static String DELAYED_ALARM_DATE_KEY = "NotificationAlarmDate";
     public static int INTERVAL_SCALAR = 3;
     public static long NUM_MILLIS_IN_INTERVAL = 1000*60*60*INTERVAL_SCALAR;	// uncomment for prod
//     public static long NUM_MILLIS_IN_INTERVAL = 1000*INTERVAL_SCALAR;	// for dev/test
     private PendingIntent alarmSender;
 	
 	/**
 	 * @param context
 	 */
 	public void set12hFlag(final Context context) {
 		// set the 12h flag
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 		Editor e = sharedPrefs.edit();
 		e.putString(NotificationAlarm._12H_RENOTIFICATION, (new Date()).toString());
 		e.apply();
 	}

 	/**
 	 * @param context
 	 */
 	public void clear12hFlag(final Context context) {
 		//clear the 12h flag
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 		Editor e = sharedPrefs.edit();
 		e.putString(NotificationAlarm._12H_RENOTIFICATION, "");
 		e.apply();
 	}


	/**
	 * Gets the set of camera image references from the local Android media store where the photo was taken within some period of most-recent time.
	 * DB contents sample:
	 * 		03-06 12:48:34.840: D/Util.logCursor(2862): col names = ; _id; bucket_id; bucket_display_name; datetaken
	 * 		03-06 12:48:34.840: D/Util.logCursor(2862): row values = ; 323; -1739773001; Camera; 1393364927927
	 * 		03-06 12:48:34.840: D/Util.logCursor(2862): row values = ; 333; -933110263; Mantra; 1248381767000
      
	 * @param context
	 * @return 
	 */
	public static ArrayList<String> getCameraImagesSinceDate(Context context, Date sinceDate) {
		Log.d(CN+".getCameraImagesSinceDate", "sinceDate = " + sinceDate);
		Uri mediaImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		 String[] imagesMediaProjection = { 
				   MediaStore.Images.Media._ID
				 , MediaStore.Images.Media.BUCKET_ID
				 , MediaStore.Images.Media.BUCKET_DISPLAY_NAME
				 , MediaStore.Images.Media.DATE_TAKEN
				 };
		 Cursor imagesMediaCursor = context.getContentResolver().query(mediaImagesUri, imagesMediaProjection, null, null, null);
		 ArrayList<String> ids = new ArrayList<String>();
		 
		 while(imagesMediaCursor.moveToNext()) {
			 long imageDateTaken = imagesMediaCursor.getLong(imagesMediaCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
			 String imageSourceType = imagesMediaCursor.getString(imagesMediaCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
			 
			 // image must be newer than the last scan time AND from the camera
			 if(sinceDate.getTime() <= imageDateTaken && imageSourceType.equalsIgnoreCase("Camera")) {
				 ids.add(imagesMediaCursor.getString(imagesMediaCursor.getColumnIndex(MediaStore.Images.Media._ID)));
			 }
		 }
		 Log.d(CN+".getCameraImagesSinceDate", "returning ids = " + ids.toString());
		 return ids;
	}

    /**
     * Creates and displays a notification in the notification menu.
     * src: http://www.vogella.com/tutorials/AndroidNotifications/article.html
     * @param context
     * @param message
     * @param iconId
     * @param messageList If null, then default-sized notification, else, large notification (like GMail's).
     */
	public static void makeLegacyNotification(Context context, String message, int iconId, String[] messageList, Intent onClickIntent, int notificationId) {
		// setup the notification intent
		 Intent intent1 = new Intent(context, NotificationAlarm.class);
		 PendingIntent pi = PendingIntent.getActivity(context, 0, intent1, 0);
		 NotificationCompat.Builder n = null;
		 String appName = context.getString(R.string.app_name);
		 
		 // if the caller specifies an onClickIntent (destination intent when the notification is tapped), then apply it, else don't.
		 if(onClickIntent != null) {
			 n = new NotificationCompat.Builder(context)
			 	.setContentTitle(appName)
			 	.setContentText(message)
			 	.setSmallIcon(R.drawable.ic_note_mantra)
			 	.setContentIntent(pi)
			 	.setAutoCancel(true)
			 	.setContentIntent(PendingIntent.getActivity(context, 0, onClickIntent, 0))
			 	;
		 }
		 else {
			 n = new NotificationCompat.Builder(context)
			 	.setContentTitle(appName)
			 	.setContentText(message)
			 	.setSmallIcon(R.drawable.ic_note_mantra)
			 	.setContentIntent(pi)
			 	.setAutoCancel(true)
			 	;
		 }
		 
		 // if large notification desired...
		 // src: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
		 if(messageList != null) {
			 NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			 inboxStyle.setBigContentTitle(context.getString(R.string.app_name));
			 for(String m : messageList) {
				 inboxStyle.addLine(m);
			 }
			 n.setStyle(inboxStyle);
		 }
		 NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		 nm.notify(appName, notificationId, n.build());
	}
	
	public static void makeNotification(Context context, String message, int iconId, PendingIntent intent, int notificationId, Uri uri) 
	{
		if (intent == null)
			intent = PendingIntent.getActivity(context, 0, new Intent(context, IndexActivity.class), 0);
		
		if (uri == null)
			uri = IndexActivity.activityUri(context);
		
		String title = context.getString(R.string.app_name);
		
		StatusNotificationManager.getInstance(context).notifyBigText(notificationId, iconId, title, message, intent, uri);
	}
	
	 public void SetAlarm(Context context)
	 {
		 if(!isAlreadyCalled) {
			 isAlreadyCalled = true;
	    	 Log.d(CN+".SetAlarm","entered");
	         AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	         Intent i = new Intent(context, NotificationAlarm.class);
	         PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
	         am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.ALARM_POLLING_RATE_MILLIS, pi); 
	    	 Log.d(CN+".SetAlarm","exiting; am = " + am.toString());
		 }
	 }
	
	 public void CancelAlarm(Context context)
	 {
	     Intent intent = new Intent(context, NotificationAlarm.class);
	     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	     alarmManager.cancel(sender);
	 }
}