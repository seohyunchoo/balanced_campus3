package com.aware.plugin.balancedcampusesm;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

import java.util.Date;
import java.util.HashSet;

public class Plugin extends Aware_Plugin {

    private static ContextProducer sContext;
    private static String accountName = "";
    private static String displayName = "";
    private static String eventID = "";
    private static String title = "";
    private static String location = "";
    private static String description = "";
    private static String start = null;
    private static String end = null;
    private static String all_Day = "";

    private CalendarObserver observer;


    @Override
    public void onCreate() {
        super.onCreate();
        TAG = "AWARE::" + getResources().getString(R.string.app_name);

        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, true);

        if (DEBUG) Log.d(TAG, "Balanced Campus Calendar plugin running");

        Aware.startPlugin(this, "com.aware.plugin.balancedcampusesm");
        //To sync data to the server, you'll need to set this variables from your ContentProvider
        //Shares this plugin's context to AWARE and applications

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.Calendar_Data.CONTENT_URI };

        sContext = new ContextProducer() {
            @Override
            public void onContext() {
                ContentValues context_data = new ContentValues();
                context_data.put(Provider.Calendar_Data.TIMESTAMP, System.currentTimeMillis());
                context_data.put(Provider.Calendar_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                context_data.put(Provider.Calendar_Data.ACCOUNT_NAME, accountName);
                context_data.put(Provider.Calendar_Data.CAL_NAME, displayName);
                context_data.put(Provider.Calendar_Data.EVENT_ID, eventID);
                context_data.put(Provider.Calendar_Data.TITLE, title);
                context_data.put(Provider.Calendar_Data.LOCATION, location);
                context_data.put(Provider.Calendar_Data.DESCRIPTION, description);
                context_data.put(Provider.Calendar_Data.START, start);
                context_data.put(Provider.Calendar_Data.END, end);
                context_data.put(Provider.Calendar_Data.ALL_DAY, all_Day);

                if( DEBUG ) Log.d(TAG, context_data.toString());

                //Log.d("AWARE","uri: " + CONTEXT_URIS[0].toString());
                getContentResolver().insert(CONTEXT_URIS[0], context_data);

            }

        };

        observer = new CalendarObserver(new Handler());
        getContentResolver().registerContentObserver(CONTEXT_URIS[0],true, observer);

        readCalendar();
    }

    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    public static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,        // 0
            CalendarContract.Instances.TITLE,           // 1
            CalendarContract.Instances.EVENT_LOCATION,  // 2
            CalendarContract.Instances.DESCRIPTION,     // 3
            CalendarContract.Instances.DTSTART,         // 4
            CalendarContract.Instances.DTEND,           // 5
            CalendarContract.Instances.ALL_DAY         // 6
    };

    public void readCalendar() {
        Cursor cursor = null;
        ContentResolver contentResolver = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";

        cursor = contentResolver.query(uri, EVENT_PROJECTION,CalendarContract.Calendars.VISIBLE + " = 1",
                null,  CalendarContract.Calendars._ID + " ASC");
        HashSet<String> calendarIds = new HashSet<String>();
        while (cursor.moveToNext()) {
            final String _id = cursor.getString(0);
            accountName = cursor.getString(1);
            displayName = cursor.getString(2);
            final String ownerAccount = cursor.getString(3);

            calendarIds.add(_id);
        }
        // For each calendar, display all the events from the previous week to the end of next week.
        for (String id : calendarIds) {

            String selection2 = CalendarContract.Instances.EVENT_ID + " = ?";
            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            long now = new Date().getTime();
            ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
            ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);
            Cursor eventCursor = contentResolver.query(builder.build(),
                    INSTANCE_PROJECTION, null,null
                    , "startDay ASC, startMinute ASC");


            while (eventCursor.moveToNext()) {
                eventID = eventCursor.getString(0);
                title = eventCursor.getString(1);
                location = eventCursor.getString(2);
                description = eventCursor.getString(3);
                start = Long.toString(eventCursor.getLong(4));
                end = Long.toString(eventCursor.getLong(5));
                all_Day = eventCursor.getString(6);

                sContext.onContext();
            }

        }
    }

    public void update(Uri uri){
       // Log.d("AWARE", "update");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(observer);
        if (DEBUG) Log.d(TAG, "Balanced Campus Calendar plugin terminating.");
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }


    class CalendarObserver extends ContentObserver {
        public CalendarObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //Log.d("AWARE", "change detected");
            update(uri);
        }
    }


}

