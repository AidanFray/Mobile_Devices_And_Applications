package mobile.labs.acw.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class for handling communication with a local database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int mDatabaseVersion = 6;
    private static final String mDatabaseName = "database_puzzles";

    public DatabaseHelper(Context context) {
        super(context, mDatabaseName, null, mDatabaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBContract.Puzzle.CREATE_TABLE());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBContract.Puzzle.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}

