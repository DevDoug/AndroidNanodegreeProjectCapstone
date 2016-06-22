package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import model.Mix;

/**
 * Created by douglas on 5/25/2016.
 */
public class MixDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "Mix.db";

    public static final String COLUMN_TYPE_INT_NULL       = " INTEGER";
    public static final String COLUMN_TYPE_INT_NOT_NULL   = " INTEGER";
    public static final String COLUMN_TYPE_TEXT_NULL      = " TEXT";
    public static final String COLUMN_TYPE_TEXT_NOT_NULL  = " TEXT NOT NULL";
    public static final String COMMA_SEPERATOR            = ",";

    //Query params
    public static final String WHERE_CLAUSE_LIKE          = "LIKE ?";
    public static final String WHERE_CLAUSE_EQUAL         = " = ? ";

    //Sort Types
    public static final String DB_SORT_TYPE_DESC          = " DESC";

    public MixDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create mix table
        final String CREATE_TABLE_MIX = "CREATE TABLE " + MixContract.MixEntry.TABLE_NAME + " (" +
                        MixContract.MixEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MixContract.MixEntry.COLUMN_NAME_MIX_TITLE + COLUMN_TYPE_TEXT_NULL + COMMA_SEPERATOR +
                        MixContract.MixEntry.COLUMN_NAME_MIX_ALBUM_ART_URL + COLUMN_TYPE_TEXT_NULL + COMMA_SEPERATOR +
                        MixContract.MixEntry.COLUMN_NAME_MIX_USER_ID + COLUMN_TYPE_INT_NOT_NULL + COMMA_SEPERATOR +

                        // Set up the Mix Items fk column as a foreign key to movie table.
                        " FOREIGN KEY (" + MixContract.MixEntry.COLUMN_NAME_MIX_USER_ID + ") REFERENCES " +
                        MixContract.UserEntry.TABLE_NAME + " (" + MixContract.UserEntry._ID + "));";

        final String CREATE_TABLE_MIX_ITEMS = "CREATE TABLE " + MixContract.MixItemsEntry.TABLE_NAME + " (" +
                        MixContract.MixItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MixContract.MixItemsEntry.COLUMN_NAME_MIX_ITEM_TITLE + COLUMN_TYPE_TEXT_NULL + COMMA_SEPERATOR +
                        MixContract.MixItemsEntry.COLUMN_NAME_MIX_ITEM_LEVEL + COLUMN_TYPE_INT_NULL + COMMA_SEPERATOR +
                        MixContract.MixItemsEntry.COLUMN_NAME_MIX_ITEMS_FOREIGN_KEY + COLUMN_TYPE_INT_NOT_NULL + COMMA_SEPERATOR +

                        // Set up the Mix Items fk column as a foreign key to movie table.
                        " FOREIGN KEY (" + MixContract.MixItemsEntry.COLUMN_NAME_MIX_ITEMS_FOREIGN_KEY + ") REFERENCES " +
                        MixContract.MixEntry.TABLE_NAME + " (" + MixContract.MixEntry._ID + "));";

        final String CREATE_TABLE_USER = "CREATE TABLE " + MixContract.UserEntry.TABLE_NAME + " (" +
                        MixContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MixContract.UserEntry.COLUMN_NAME_USER_NAME + COLUMN_TYPE_TEXT_NULL + COMMA_SEPERATOR +
                        MixContract.UserEntry.COLUMN_NAME_USER_PASSWORD + COLUMN_TYPE_TEXT_NOT_NULL +
                        " );";

        db.execSQL(CREATE_TABLE_MIX);
        db.execSQL(CREATE_TABLE_MIX_ITEMS);
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MixContract.MixEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MixContract.MixItemsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MixContract.UserEntry.TABLE_NAME);
        onCreate(db);
    }
}