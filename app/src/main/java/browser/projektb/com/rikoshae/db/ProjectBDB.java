package browser.projektb.com.rikoshae.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import browser.projektb.com.rikoshae.model.HistoryModel;
import browser.projektb.com.rikoshae.util.Util;

/**
 * Created by User on 7/25/2016.
 */
public class ProjectBDB {

    private static ProjectBDB _instance;
    private static final String NOM_BDD = "projectBrowser.db";
    private static final String DB_NAME = "projectBrowser";
    private Context _ctx;
    private SQLiteDatabase sqLiteDatabase;
    private MaSqliteDatabase maBaseSqlite;

    public ProjectBDB(Context context) {
        maBaseSqlite = new MaSqliteDatabase(context, NOM_BDD);
    }

    public static ProjectBDB get_instance(Context ctx) {
        if (_instance == null)
            _instance = new ProjectBDB(ctx);
        return _instance;
    }

    private void closeDB() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen())
            sqLiteDatabase.close();
    }

    public void open() {
        sqLiteDatabase = maBaseSqlite.getWritableDatabase();
    }

    public void insertSearchUrlHistory(HistoryModel model) {
        deleteDateAfter7Days();
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(MaSqliteDatabase.KEY_ICON_URL, model.icon);
            cv.put(MaSqliteDatabase.KEY_TITLE, model.title);
            cv.put(MaSqliteDatabase.KEY_LINK_URL, model.url);
            //cv.put(MaSqliteDatabase.KEY_DATE, model.date);
            //cv.put(MaSqliteDatabase.KEY_TIME, model.time);
            cv.put(MaSqliteDatabase.COLUMN_TIME_STAMP, Util.getDateCurrentMillis());

            sqLiteDatabase.insert(MaSqliteDatabase.HISTORY_TABLE, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDB();
        }
    }

    public void CheckIsDataAlreadyInDBorNot(String title, String url) {
        try {
            open();
            String Query = "delete from " + MaSqliteDatabase.BOOKMARK_URL_TABLE + " where " + MaSqliteDatabase.KEY_NAME + " = '" + title + "' and " + MaSqliteDatabase.KEY_LINK_URL + " = '" + url + "'";
            sqLiteDatabase.execSQL(Query);
        } finally {
            closeDB();
        }
    }

    public void insertBookmarkUrl(String title, String url) {
        CheckIsDataAlreadyInDBorNot(title, url);
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(MaSqliteDatabase.KEY_ICON_URL, "");
            cv.put(MaSqliteDatabase.KEY_NAME, title);
            cv.put(MaSqliteDatabase.KEY_LINK_URL, url);

            sqLiteDatabase.insert(MaSqliteDatabase.BOOKMARK_URL_TABLE, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDB();
        }
    }

    public ArrayList<HistoryModel> getSearchedHistory() {

        open();
        String existDay = "";
        ArrayList<HistoryModel> history = new ArrayList<HistoryModel>();
        Cursor c = null;
        try {
            c = sqLiteDatabase.query(MaSqliteDatabase.HISTORY_TABLE,
                    new String[]{MaSqliteDatabase.KEY_ID, MaSqliteDatabase.KEY_ICON_URL,
                            MaSqliteDatabase.KEY_TITLE, MaSqliteDatabase.KEY_LINK_URL, MaSqliteDatabase.COLUMN_TIME_STAMP},
                    null, null, null, null, MaSqliteDatabase.COLUMN_TIME_STAMP + " DESC");//, " + MaSqliteDatabase.KEY_TIME + " DESC");
            if (c.getCount() == 0)
                return history;

            while (c.moveToNext()) {
                HistoryModel tm = new HistoryModel();

                tm.id = c.getInt(0);
                tm.icon = c.getString(1);
                tm.title = c.getString(2);
                tm.url = c.getString(3);
                tm.timeStamp = c.getLong(4);
                //tm.date = c.getString(4);
                //tm.time = c.getString(5);
                String fullDate = Util.oneDayDifferenceDate(Util.getDate(tm.timeStamp));
                if (fullDate != null && !fullDate.equalsIgnoreCase(existDay)) {
                    tm.fullDate = fullDate;
                    existDay = fullDate;
                } else {
                    tm.fullDate = "";
                }
                if (tm.title == null && tm.url == null)
                    continue;
                else
                    history.add(tm);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
            closeDB();
            return history;
        }

    }

    public ArrayList<HistoryModel> getBookmarkData() {

        open();
        String existDay = "";
        ArrayList<HistoryModel> history = new ArrayList<HistoryModel>();
        Cursor c = null;
        try {
            c = sqLiteDatabase.query(MaSqliteDatabase.BOOKMARK_URL_TABLE,
                    new String[]{MaSqliteDatabase.KEY_ID, MaSqliteDatabase.KEY_ICON_URL,
                            MaSqliteDatabase.KEY_NAME, MaSqliteDatabase.KEY_LINK_URL},
                    null, null, null, null, null);
            if (c.getCount() == 0)
                return history;

            while (c.moveToNext()) {
                HistoryModel tm = new HistoryModel();

                tm.id = c.getInt(0);
                tm.icon = c.getString(1);
                tm.title = c.getString(2);
                tm.url = c.getString(3);

                if (tm.title == null && tm.url == null)
                    continue;
                else
                    history.add(tm);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
            closeDB();
            return history;
        }

    }

    public void deleteAllHistoryData() {
        open();
        try {
            String query = "delete from " + MaSqliteDatabase.HISTORY_TABLE;
            sqLiteDatabase.execSQL(query);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDB();
        }
    }

    public void deleteSelectedHistoryData(ArrayList<HistoryModel> selectedList) {
        open();
        try {
            for (int i = 0; i < selectedList.size(); i++) {
                HistoryModel model = selectedList.get(i);
                if (model.isChecked) {
                    String query = "delete from " + MaSqliteDatabase.HISTORY_TABLE + " where " + MaSqliteDatabase.KEY_ID + " = " + model.id;
                    sqLiteDatabase.execSQL(query);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDB();
        }
    }

    public void deleteBookmarkData(HistoryModel selectedList) {
        open();
        try {
            String query = "delete from " + MaSqliteDatabase.BOOKMARK_URL_TABLE + " where " + MaSqliteDatabase.KEY_ID + " = " + selectedList.id;
            sqLiteDatabase.execSQL(query);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDB();
        }
    }

    public void deleteDateAfter7Days() {
        open();
        try {
           String query = "DELETE FROM " + MaSqliteDatabase.HISTORY_TABLE + " WHERE "+MaSqliteDatabase.COLUMN_TIME_STAMP+" < "+Util.getTimeStampBefore7Days();
            sqLiteDatabase.execSQL(query);
        }
       catch (Exception e)
       {
           e.printStackTrace();
           closeDB();
       }

    }
}
