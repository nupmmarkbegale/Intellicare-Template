/**
 * Created by Gabe on 9/23/13.
 */
package edu.northwestern.cbits.intellicare.dailyfeats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;

public class FeatsProvider extends android.content.ContentProvider
{
    private static final int RESPONSES = 1;
    private static final int RESPONSE = 2;
    private static final int FEATS = 3;
    private static final int FEAT = 4;

    private static final String RESPONSES_TABLE = "responses";
    private static final String FEATS_TABLE = "feats";
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.dailyfeats";

    public static final Uri RESPONSES_URI = Uri.parse("content://" + AUTHORITY + "/" + RESPONSES_TABLE);
    public static final Uri FEATS_URI = Uri.parse("content://" + AUTHORITY + "/" + FEATS_TABLE);

    public static final String DEPRESSION_LEVEL = "depression_level";
	protected static final String SUPPORTERS = "supporters";

    private UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase mDb = null;

    private static final int DATABASE_VERSION = 6;
	private static final long DAY_LENGTH = 1000 * 3600 * 24;
	protected static final String START_FEATS_DATE = "start_feats_date";
	private static final long STREAK_LENGTH = 3;

	private static final String MIN_FEAT_COUNT = "min_feat_count";
	private static final int DEFAULT_MIN_FEAT_COUNT = 2;
	public static final String LEVEL_CHANGE_DATE = "level_change_date";
    
    public FeatsProvider()
    {
    	super();
    	
    	this.mUriMatcher.addURI(AUTHORITY, RESPONSES_TABLE, RESPONSES);
    	this.mUriMatcher.addURI(AUTHORITY, RESPONSES_TABLE +  "/#",  RESPONSE);
    	this.mUriMatcher.addURI(AUTHORITY, FEATS_TABLE, FEATS);
    	this.mUriMatcher.addURI(AUTHORITY, FEATS_TABLE +  "/#",  FEAT);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        switch(this.mUriMatcher.match(uri))
        {
	        case FeatsProvider.FEATS:
	            return this.mDb.delete(FeatsProvider.FEATS_TABLE, selection, selectionArgs);
	        case FeatsProvider.RESPONSES:
	            return this.mDb.delete(FeatsProvider.RESPONSES_TABLE, selection, selectionArgs);
        }

		return 0;
    }

    public boolean onCreate()
    {
    	final Context context = this.getContext().getApplicationContext();
    	
    	SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "feats.db", null, FeatsProvider.DATABASE_VERSION)
    	{
			public void onCreate(SQLiteDatabase db) 
			{
		        db.execSQL(context.getString(R.string.db_create_responses_table));
		        db.execSQL(context.getString(R.string.db_create_feats_table));
		        
		        ContentValues feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_0));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_1));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_2));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_3));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_2_0));
		        feat.put("feat_level", 2);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_2_1));
		        feat.put("feat_level", 2);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_2_2));
		        feat.put("feat_level", 2);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_0));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_1));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_2));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_3));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_0));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_1));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_2));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);
		        
//		        feat = new ContentValues();
//		        feat.put("feat_name", context.getString(R.string.feat_4_3));
//		        feat.put("feat_level", 4);
//		        db.insert(FEATS_TABLE, null, feat);

		        this.onUpgrade(db, 0, FeatsProvider.DATABASE_VERSION);
			}

			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
			{
                switch (oldVersion)
                {
                    case 0:
                    	
                    case 1: 
	                	db.execSQL(context.getString(R.string.db_alter_feats_table_add_enabled));
                    case 2: 
	                	db.execSQL(context.getString(R.string.db_clear_system_feats));
	                	
	    		        ContentValues feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_1_0));
	    		        feat.put("feat_level", 1);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_1_1));
	    		        feat.put("feat_level", 1);
	    		        db.insert(FEATS_TABLE, null, feat);

	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_1_2));
	    		        feat.put("feat_level", 1);
	    		        db.insert(FEATS_TABLE, null, feat);

	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_1_3));
	    		        feat.put("feat_level", 1);
	    		        db.insert(FEATS_TABLE, null, feat);

	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_2_0));
	    		        feat.put("feat_level", 2);
	    		        db.insert(FEATS_TABLE, null, feat);

	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_2_1));
	    		        feat.put("feat_level", 2);
	    		        db.insert(FEATS_TABLE, null, feat);

	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_2_2));
	    		        feat.put("feat_level", 2);
	    		        db.insert(FEATS_TABLE, null, feat);

	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_3_0));
	    		        feat.put("feat_level", 3);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_3_1));
	    		        feat.put("feat_level", 3);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_3_2));
	    		        feat.put("feat_level", 3);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_3_3));
	    		        feat.put("feat_level", 3);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_4_0));
	    		        feat.put("feat_level", 4);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_4_1));
	    		        feat.put("feat_level", 4);
	    		        db.insert(FEATS_TABLE, null, feat);
	    		        
	    		        feat = new ContentValues();
	    		        feat.put("feat_name", context.getString(R.string.feat_4_2));
	    		        feat.put("feat_level", 4);
	    		        db.insert(FEATS_TABLE, null, feat);
                    case 3: 
	    		        ContentValues autoFeat = new ContentValues();
	    		        autoFeat.put("feat_name", context.getString(R.string.feat_github_checkin));
	    		        autoFeat.put("feat_level", 99);
	    		        autoFeat.put("enabled", false);
	    		        db.insert(FEATS_TABLE, null, autoFeat);

	    		        autoFeat = new ContentValues();
	    		        autoFeat.put("feat_name", context.getString(R.string.feat_fitbit_minutes));
	    		        autoFeat.put("feat_level", 99);
	    		        autoFeat.put("enabled", false);
	    		        db.insert(FEATS_TABLE, null, autoFeat);
	    		        
	    		        autoFeat = new ContentValues();
	    		        autoFeat.put("feat_name", context.getString(R.string.feat_fitbit_distance));
	    		        autoFeat.put("feat_level", 99);
	    		        autoFeat.put("enabled", false);
	    		        db.insert(FEATS_TABLE, null, autoFeat);

	    		        autoFeat = new ContentValues();
	    		        autoFeat.put("feat_name", context.getString(R.string.feat_fitbit_steps));
	    		        autoFeat.put("feat_level", 99);
	    		        autoFeat.put("enabled", false);
	    		        db.insert(FEATS_TABLE, null, autoFeat);
                    case 4:
	    		        ContentValues newFeat = new ContentValues();

	    		        newFeat = new ContentValues();
	    		        newFeat.put("feat_name", context.getString(R.string.feat_2_3));
	    		        newFeat.put("feat_level", 2);
	    		        db.insert(FEATS_TABLE, null, newFeat);

	    		        newFeat = new ContentValues();
	    		        newFeat.put("feat_name", context.getString(R.string.feat_3_4));
	    		        newFeat.put("feat_level", 3);
	    		        db.insert(FEATS_TABLE, null, newFeat);

	    		        newFeat = new ContentValues();
	    		        newFeat.put("feat_name", context.getString(R.string.feat_4_3));
	    		        newFeat.put("feat_level", 4);
	    		        db.insert(FEATS_TABLE, null, newFeat);

	    		        newFeat = new ContentValues();
	    		        newFeat.put("feat_name", context.getString(R.string.feat_4_4));
	    		        newFeat.put("feat_level", 4);
	    		        db.insert(FEATS_TABLE, null, newFeat);

                    case 5:
	    		        autoFeat = new ContentValues();
	    		        autoFeat.put("feat_name", context.getString(R.string.feat_jawbone_steps));
	    		        autoFeat.put("feat_level", 99);
	    		        autoFeat.put("enabled", false);
	    		        db.insert(FEATS_TABLE, null, autoFeat);
                    
                    default:
                    	break;
                }
			}
    	};
    	
        this.mDb = helper.getWritableDatabase();

        return true;
    }

    public String getType(Uri uri)
    {
        switch(this.mUriMatcher.match(uri))
        {
            case FeatsProvider.RESPONSES:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".response";
            case FeatsProvider.RESPONSE:
                return "vnd.android.cursor.item/" + AUTHORITY + ".response";
            case FeatsProvider.FEATS:
                return "vnd.android.cursor.dir/"+AUTHORITY+".feat";
            case FeatsProvider.FEAT:
                return "vnd.android.cursor.item/"+AUTHORITY+".feat";
        }

        return null;
    }

    public Uri insert(Uri uri, ContentValues values)
    {
        long insertedId = -1;

        Uri inserted = Uri.EMPTY;

        switch(this.mUriMatcher.match(uri))
        {
            case FeatsProvider.RESPONSES:
                insertedId = this.mDb.insert(RESPONSES_TABLE, null, values);
                
                return Uri.withAppendedPath(RESPONSES_URI, "" + insertedId);

            case FeatsProvider.FEATS:
                insertedId = this.mDb.insert(FEATS_TABLE, null, values);

                return Uri.withAppendedPath(FEATS_URI, "" + insertedId);
        }

        return inserted;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        switch(this.mUriMatcher.match(uri))
        {
            case FeatsProvider.RESPONSE:
                return this.mDb.query(FeatsProvider.RESPONSES_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
            case FeatsProvider.RESPONSES:
                return this.mDb.query(FeatsProvider.RESPONSES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case FeatsProvider.FEAT:
                return this.mDb.query(FeatsProvider.FEATS_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
            case FeatsProvider.FEATS:
                return this.mDb.query(FeatsProvider.FEATS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
         }

        return null;
    }

    private String[] buildSingleSelectionArgs(Uri uri, String[] selectionArgs)
    {
        if (selectionArgs == null)
        {
            selectionArgs = new String[1];
            selectionArgs[0] = uri.getLastPathSegment();
        }
        else
        {
            String[] newSelectionArgs = new String[selectionArgs.length + 1];

            for (int i = 0; i < selectionArgs.length; i++)
            {
                newSelectionArgs[i] = selectionArgs[i];
            }

            newSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();

            selectionArgs = newSelectionArgs;
        }

        return selectionArgs;
    }

    private String buildSingleSelection(String selection)
    {
        if (selection == null)
            selection = "_id = ?";
        else
            selection = " AND _id = ?";

        return selection;
    }

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
        switch(this.mUriMatcher.match(uri))
        {
	        case FeatsProvider.FEATS:
	            return this.mDb.update(FeatsProvider.FEATS_TABLE, values, selection, selectionArgs);
        }

		return 0;
	}

	public static boolean hasFeatForDate(Context context, String featName, Date date) 
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		long start = calendar.getTimeInMillis();
		
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		long end = calendar.getTimeInMillis();

		String selection = "recorded >= ? AND recorded <= ? AND feat = ?";
		String[] args = { "" + start, "" + end, featName };
		
		Cursor c = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, selection, args, null);
		
		boolean hasFeat = c.getCount() > 0;
		
		c.close();
		
		return hasFeat;
	}

	public static Collection<String> featsForDate(Context context, Date date) 
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		long start = calendar.getTimeInMillis();
		long end = start + FeatsProvider.DAY_LENGTH;
		
		String selection = "recorded >= ? AND recorded < ?";
		String[] args = { "" + start, "" + end };
		
		HashSet<String> feats = new HashSet<String>();
		
		Cursor c = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, selection, args, null);
		
		while (c.moveToNext())
		{
			String feat = c.getString(c.getColumnIndex("feat"));
			
			if (feats.contains(feat) == false)
			{
				feats.add(feat);
			}
		}
		
		c.close();
		
		return feats;
	}

	public static void clearFeats(Context context, String featName, Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		long start = calendar.getTimeInMillis();
		
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		long end = calendar.getTimeInMillis();

		String selection = "recorded >= ? AND recorded <= ? AND feat = ?";
		String[] args = { "" + start, "" + end, featName };
		
		context.getContentResolver().delete(FeatsProvider.RESPONSES_URI, selection, args);
	}
	
	public static void createFeat(Context context, String featName, int depressionLevel)
	{
		ContentValues values = new ContentValues();
		values.put("depression_level", depressionLevel);
		values.put("recorded", System.currentTimeMillis());
		values.put("feat", featName);
		
		context.getContentResolver().insert(RESPONSES_URI, values);
	}

	public static int featCountForDate(Context context, Date date) 
	{
		return FeatsProvider.featsForDate(context, date).size();
	}

	public static void updateLevel(Context context, int level) 
	{
		ContentValues values = new ContentValues();
		values.put("enabled", false);
		
		context.getContentResolver().update(FeatsProvider.FEATS_URI, values, null, null);

		String where = "feat_level = ?";
		String[] args = { "3" };
		
		if (level < 3)
			where = "feat_level < ?";
		else if (level > 3)
			where = "feat_level >= ?";
		
		values = new ContentValues();
		values.put("enabled", true);
		
		context.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);
	}

	public static int checkLevel(Context context, int level) 
	{
		int streak = FeatsProvider.streakForLevel(context, level);

		if (streak >= FeatsProvider.STREAK_LENGTH && level < 4)
			level += 1;
		else if (streak <= (0 - FeatsProvider.STREAK_LENGTH) && level > 1)
		{
			level -= 1;

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			long startFeats = prefs.getLong(FeatsProvider.START_FEATS_DATE, 0);

			long now = System.currentTimeMillis();
			
			if (now - startFeats > FeatsProvider.DAY_LENGTH * FeatsProvider.STREAK_LENGTH)
			{
				Editor e = prefs.edit();
				e.putLong(FeatsProvider.START_FEATS_DATE, System.currentTimeMillis());
				e.commit();
			}
		}

		return level;
	}
	
	public static int streakForLevel(Context context, int level)
	{
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.SECOND, 0);

		long end = c.getTimeInMillis() + FeatsProvider.DAY_LENGTH;
		long start = end - FeatsProvider.DAY_LENGTH;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		long lastChange = prefs.getLong(FeatsProvider.LEVEL_CHANGE_DATE, 0);
		
		String where = "feat_level = ?";
		String[] args = { "" + level };
		
		ArrayList<String> feats = new ArrayList<String>();
		
		Cursor cursor = context.getContentResolver().query(FeatsProvider.FEATS_URI, null, where, args, null);
		
		final int minFeatCount = 2; // (cursor.getCount() / 2) + 1;
		
		while (cursor.moveToNext())
			feats.add(cursor.getString(cursor.getColumnIndex("feat_name")));
		
		cursor.close();
		
		int streak = 0;
		boolean continueStreak = true;
		
		while (continueStreak && start > lastChange)
		{
			int dayFeats = 0;
			
			for (String feat : feats)
			{
				where = "feat = ? AND recorded >= ? AND recorded <= ?";
				String[] featArgs = { feat, "" + start , "" + end }; 
				
				Cursor featCursor = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, where, featArgs, null);
				
				if (featCursor.getCount() > 0)
					dayFeats += 1;
				
				featCursor.close();
			}
			
			if (dayFeats >= minFeatCount)
				streak += 1;
			else
				continueStreak = false;
			
			end = start;
			start -= FeatsProvider.DAY_LENGTH;
		}
		
		if (streak == 0)
		{
			long startFeats = prefs.getLong(FeatsProvider.START_FEATS_DATE, 0);
			
			end = c.getTimeInMillis() + FeatsProvider.DAY_LENGTH;
			start = end - FeatsProvider.DAY_LENGTH;
	
			for (int i = 0; i < FeatsProvider.STREAK_LENGTH && end > startFeats; i++)
			{
				int dayFeats = feats.size();
				
				for (String feat : feats)
				{
					where = "feat = ? AND recorded >= ? AND recorded <= ?";
					String[] featArgs = { feat, "" + start , "" + end }; 
					
					Cursor featCursor = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, where, featArgs, null);
					
					if (featCursor.getCount() == 0)
						dayFeats -= 1;
					
					featCursor.close();
				}
				
				if (dayFeats < minFeatCount)
					streak -= 1;
				else
					break;
				
				end = start;
				start -= FeatsProvider.DAY_LENGTH;
			}
		}
		
		return streak;
	}

	public static boolean metGoalForDate(Context context, Date time) 
	{
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.SECOND, 0);

		long start = c.getTimeInMillis();
		long end = start + FeatsProvider.DAY_LENGTH;
		
		String where = "recorded >= ? AND recorded < ?";
		String[] args = { "" + start, "" + end };
		
		Cursor cursor = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, where, args, null);
		
		int count = 0;
		
		HashSet<String> seen = new HashSet<String>();
		
		while (cursor.moveToNext())
		{
			int recordLevel = cursor.getInt(cursor.getColumnIndex("depression_level"));
			
			String featsWhere = "feat_name = ?";
			String[] featsArgs = { cursor.getString(cursor.getColumnIndex("feat")) };

			Cursor featCursor = context.getContentResolver().query(FeatsProvider.FEATS_URI, null, featsWhere, featsArgs, null);
			
			String featName = cursor.getString(cursor.getColumnIndex("feat"));

			while (featCursor.moveToNext())
			{
				if (recordLevel == featCursor.getInt(featCursor.getColumnIndex("feat_level")) && seen.contains(featName) == false)
				{
					count += 1;
					seen.add(featName);
				}
			}
			
			featCursor.close();
		}
		
		cursor.close();
		
		/*
		String featsWhere = "feat_level = ?";
		String[] featsArgs = { "" + level };
		
		cursor = context.getContentResolver().query(FeatsProvider.FEATS_URI, null, featsWhere, featsArgs, null);

		cursor.close();
		*/

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int minFeatCount = prefs.getInt(FeatsProvider.MIN_FEAT_COUNT, FeatsProvider.DEFAULT_MIN_FEAT_COUNT); // (cursor.getCount() / 2) + 1;
		
		return (count >= minFeatCount);
	}

	public static ArrayList<FeatCount> activeStreaks(Context context) 
	{
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		Calendar cal = Calendar.getInstance();
		
		Date today = cal.getTime();
		
		Collection<String> feats = FeatsProvider.featsForDate(context, today);
		
		for (String feat : feats)
		{
			counts.put(feat, Integer.valueOf(1));
		}

		feats.add("");
		
		HashSet<String> currentFeats = null;
		
		while (feats.size() > 0)
		{
			cal.setTimeInMillis(cal.getTimeInMillis() - (24 * 60 * 60 * 1000));
			
			Date day = cal.getTime();
			
			feats = FeatsProvider.featsForDate(context, day);
			
			if (currentFeats == null)
			{
				currentFeats = new HashSet<String>();

				for (String feat : feats)
				{
					currentFeats.add(feat);
				}
			}

			HashSet<String> toRemove = new HashSet<String>();
			
			for (String feat : currentFeats)
			{
				if (feats.contains(feat) == false)
					toRemove.add(feat);
			}

			currentFeats.removeAll(toRemove);

			for (String feat : feats)
			{
				if (currentFeats.contains(feat))
				{
					Integer count = Integer.valueOf(0);
					
					if (counts.containsKey(feat))
						count = counts.get(feat);
					
					counts.put(feat, Integer.valueOf(count.intValue() + 1));
				}
			}
		}

		ArrayList<FeatCount> featCounts = new ArrayList<FeatCount>();
		
		for (String feat : counts.keySet())
		{
			FeatCount featCount = new FeatCount();
			featCount.feat = feat;
			featCount.count = counts.get(feat);
			
			featCounts.add(featCount);
		}
		
		Collections.sort(featCounts, new Comparator<FeatCount>()
		{
			public int compare(FeatCount one, FeatCount two) 
			{
				int compare = two.count.compareTo(one.count);
				
				if (compare == 0)
					compare = one.feat.compareTo(two.feat);
				
				return compare;
			}
		});
		
		return featCounts;
	}
	
	static class FeatCount
	{
		public String feat = "";
		public Integer count = Integer.valueOf(0); 
	}

	public static int fetchMonthCount(Context context) 
	{
		Cursor c = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, null, null, null);

		SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy");
		
		HashSet<String> months = new HashSet<String>();

		while (c.moveToNext())
		{
			long date = c.getLong(c.getColumnIndex("recorded"));
			
			Date d = new Date(date);
			
			months.add(sdf.format(d));
		}

		c.close();
		
		if (months.size() == 0)
			months.add("");

		return months.size() + 2;
	}

	public static Date dateForMonthIndex(Context context, int position) 
	{
		int count = FeatsProvider.fetchMonthCount(context);
		
		if (position == (count - 2))
			return new Date();
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 15);
		
		c.add(Calendar.MONTH, position - (count - 2));
		
		return c.getTime();
	}
}
