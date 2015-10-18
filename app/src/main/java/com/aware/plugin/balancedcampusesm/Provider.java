package com.aware.plugin.balancedcampusesm;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.io.File;
import java.util.HashMap;

/**
 * Created by jennachoo on 10/16/15.
 */
public class Provider extends ContentProvider {
    /**
     * Authority of this content provider
     */
    public static String AUTHORITY = "com.aware.plugin.balancedcampusesm.provider.calendar";
    /**
     * ContentProvider database version. Increment every time you modify the database structure
     */
    public static final int DATABASE_VERSION = 2;

    public static final class Calendar_Data implements BaseColumns {
        private Calendar_Data(){};
        /**
         * Your ContentProvider table content URI.<br/>
         * The last segment needs to match your database table name
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/balancedcampusesm");

        /**
         * How your data collection is identified internally in Android (vnd.android.cursor.dir). <br/>
         * It needs to be /vnd.aware.plugin.XXX where XXX is your plugin name (no spaces!).
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.balancedcampusesm";

        /**
         * How each row is identified individually internally in Android (vnd.android.cursor.item). <br/>
         * It needs to be /vnd.aware.plugin.XXX where XXX is your plugin name (no spaces!).
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.balancedcampusesm";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String ACCOUNT_NAME = "account_name";
        public static final String CAL_NAME = "calendar_name";
        public static final String EVENT_ID = "event_id";
        public static final String TITLE = "title";
        public static final String LOCATION = "location";
        public static final String DESCRIPTION = "description";
        public static final String START = "start";
        public static final String END = "end";
        public static final String ALL_DAY = "all_day";
    }

    //ContentProvider query indexes
     private static final int CALENDAR = 1;
    private static final int CALENDAR_ID = 2;

    /**
     * Database stored in external folder: /AWARE/plugin_calendar.db
     */
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_calendar.db";



    /**
     * Database tables:<br/>
     * - plugin_calendar
     */
    public static final String[] DATABASE_TABLES = {"balancedcampusesm"};

    /**
     * Database table fields
     */
    public static final String[] TABLES_FIELDS = {
            Calendar_Data._ID + " integer primary key autoincrement," +
                    Calendar_Data.TIMESTAMP + " real default 0," +
                    Calendar_Data.DEVICE_ID + " text default ''," +
                    Calendar_Data.ACCOUNT_NAME + " text default ''," +
                    Calendar_Data.CAL_NAME + " text default ''," +
                    Calendar_Data.EVENT_ID + " text default ''," +
                    Calendar_Data.TITLE + " text default ''," +
                    Calendar_Data.LOCATION + " text default ''," +
                    Calendar_Data.DESCRIPTION + " text default ''," +
                    Calendar_Data.START + " text default ''," +
                    Calendar_Data.END + " text default ''," +
                    Calendar_Data.ALL_DAY + " text default ''," +
                    "UNIQUE (" + Calendar_Data.TIMESTAMP + "," + Calendar_Data.DEVICE_ID + ")"
    };


    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> tableMap = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    /**
     * Initialise the ContentProvider
     */
    private boolean initializeDB() {
        if (databaseHelper == null) {
            Log.d("PROVIDER", "db name: " + DATABASE_NAME);
            Log.d("PROVIDER", "tables field: " + TABLES_FIELDS[0]);
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );

        }
        if( databaseHelper != null && ( database == null || ! database.isOpen()) ) {
          //  Log.d("PROVIDER", "here2");
            database = databaseHelper.getWritableDatabase();
        }
       // Log.d("PROVIDER", "two "+ Boolean.toString(databaseHelper != null));

        return( database != null && databaseHelper != null);
    }
    /**
     * Allow resetting the ContentProvider when updating/reinstalling AWARE
     */
    public static void resetDB( Context c ) {
        Log.d("AWARE", "Resetting " + DATABASE_NAME + "...");

        File db = new File(DATABASE_NAME);
        db.delete();
        databaseHelper = new DatabaseHelper( c, DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if( databaseHelper != null ) {
            database = databaseHelper.getWritableDatabase();
        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.balancedcampusesm"; //make AUTHORITY dynamic

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], CALENDAR); //URI for all records
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", CALENDAR_ID); //URI for a single record

        tableMap = new HashMap<String, String>();
        tableMap.put(Calendar_Data._ID, Calendar_Data._ID);
        tableMap.put(Calendar_Data.TIMESTAMP, Calendar_Data.TIMESTAMP);
        tableMap.put(Calendar_Data.DEVICE_ID, Calendar_Data.DEVICE_ID);
        tableMap.put(Calendar_Data.ACCOUNT_NAME, Calendar_Data.ACCOUNT_NAME);
        tableMap.put(Calendar_Data.CAL_NAME, Calendar_Data.CAL_NAME);
        tableMap.put(Calendar_Data.EVENT_ID, Calendar_Data.EVENT_ID);
        tableMap.put(Calendar_Data.TITLE, Calendar_Data.TITLE);
        tableMap.put(Calendar_Data.LOCATION, Calendar_Data.LOCATION);
        tableMap.put(Calendar_Data.DESCRIPTION, Calendar_Data.DESCRIPTION);
        tableMap.put(Calendar_Data.START, Calendar_Data.START);
        tableMap.put(Calendar_Data.END, Calendar_Data.END);
        tableMap.put(Calendar_Data.ALL_DAY, Calendar_Data.ALL_DAY);

        Log.d("PROVIDER", "Done!");
        int hi = sUriMatcher.match(Calendar_Data.CONTENT_URI);
        Log.d("PROVIDER", AUTHORITY);
        Log.d("PROVIDER", Calendar_Data.CONTENT_URI.toString());
        Log.d("PROVIDER", "hi: " + Integer.toString(hi));
        return true; //let Android know that the database is ready to be used.
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            if( ! initializeDB() ) {
                Log.w(AUTHORITY,"Database unavailable...");
                return null;
            }

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            switch (sUriMatcher.match(uri)) {
                case CALENDAR:
                    qb.setTables(DATABASE_TABLES[0]);
                    qb.setProjectionMap(tableMap);
                    break;
                default:
                    throw new IllegalArgumentException("query Unknown URI " + uri);
            }
            try {
                Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            } catch (IllegalStateException e) {
                if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
                return null;
            }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CALENDAR:
                return Calendar_Data.CONTENT_TYPE;
            case CALENDAR_ID:
                return Calendar_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("getType Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues new_values) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (new_values != null) ? new ContentValues(new_values) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case CALENDAR:
                long _id = database.insert(DATABASE_TABLES[0],Calendar_Data.DEVICE_ID, values);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Calendar_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("insert Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case CALENDAR:
                count = database.delete(DATABASE_TABLES[0], selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("delete Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case CALENDAR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                database.close();
                throw new IllegalArgumentException("update Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
