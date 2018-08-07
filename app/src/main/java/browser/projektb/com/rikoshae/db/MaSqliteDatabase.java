package browser.projektb.com.rikoshae.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by User on 7/25/2016.
 */
public class MaSqliteDatabase extends SQLiteOpenHelper {

    public static final String HISTORY_TABLE = "historyTable";
    public static final String BOOKMARK_URL_TABLE = "bookmarkUrlTable";

    public static final String KEY_ICON_URL = "iconUrl";
    public static final String KEY_TITLE = "historyTitle";
    public static final String KEY_LINK_URL = "linkUrl";
   // public static final String KEY_DATE = "linkDate";
    //public static final String KEY_TIME = "linkTime";

    public static final String COLUMN_TIME_STAMP = "timeStamp";
    public static final String KEY_NAME = "bookmarkName";
    public static final String KEY_ID = "id";

    public static final String CREATE_HISTORY_TABLE = "create table "
            + HISTORY_TABLE + "(" + KEY_ID + "  INTEGER PRIMARY KEY ," + KEY_ICON_URL + " TEXT," + KEY_TITLE
            + " TEXT," + KEY_LINK_URL + " TEXT," /*+ KEY_DATE + " TEXT," */+ COLUMN_TIME_STAMP + " real);";//, "
        //    + KEY_TIME
         //   + " TEXT);";

    public static final String CREATE_BOOKMARK_URL_TABLE = "create table "
            + BOOKMARK_URL_TABLE + "(" + KEY_ID + "  INTEGER PRIMARY KEY ," + KEY_ICON_URL + " TEXT," + KEY_NAME
            + " TEXT," + KEY_LINK_URL + " TEXT);";

    public MaSqliteDatabase(Context context, String name) {
        super(context, name, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_BOOKMARK_URL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_HISTORY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_BOOKMARK_URL_TABLE);
    }
}
