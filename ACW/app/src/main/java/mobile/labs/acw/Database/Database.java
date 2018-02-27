package mobile.labs.acw.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import mobile.labs.acw.ExceptionHandling.Logging;

public class Database {

    private Context mContext;
    public Database(Context pContext) {
        mContext = pContext;
    }

    /**
     * Method that saves data to a database
     * @param pPrivateKey - The PKey for the database
     * @param pValues - The list of values to be saved
     */
    public void SaveToDatabase(String pPrivateKey, String[] pValues) {
        try {
            SQLiteDatabase database = new DatabaseHelper(mContext).getWritableDatabase();
            ContentValues values = new ContentValues();

            //Adds the private key
            values.put(DBContract.Puzzle.TABLE_PRIMARY_KEY.split(" ")[0], pPrivateKey);

            for (int i = 0; i < DBContract.Puzzle.TABLE_COLUMNS.length; i++) {
                String key = DBContract.Puzzle.TABLE_COLUMNS[i].split(" ")[0];
                values.put(key, pValues[i]);
            }

            database.insert(DBContract.Puzzle.TABLE_NAME, null, values);
        } catch (Exception e) {
            Logging.Exception(e);
        }
    }

    /**
     * Read a value from the database
     * @param pPuzzleName - The puzzle to access data for
     * @param pColumnName - The name of the element that is to be accessed
     * @return - The value retrieved from the database
     */
    public String ReadFromDatabase(String pPuzzleName, String pColumnName) {
        SQLiteDatabase database = new DatabaseHelper(mContext).getReadableDatabase();

        String sqlCommand = String.format("SELECT * FROM %s WHERE %s=\'%s\'",
                DBContract.Puzzle.TABLE_NAME,
                DBContract.Puzzle.TABLE_PRIMARY_KEY.split(" ")[0],
                pPuzzleName);

        Cursor cursor =  database.rawQuery(sqlCommand, null);

        //Checks if the pColumnName is correct
        try {
            boolean columnNameValid = false;
            for (int i = 0; i < DBContract.Puzzle.TABLE_COLUMNS.length; i++) {
                String columnName = DBContract.Puzzle.TABLE_COLUMNS[i].split(" ")[0];

                if (columnName.equals(pColumnName)) {
                    columnNameValid = true;
                    break;
                }
            }
            if(!columnNameValid) { throw new Exception("Error: Column named entered does not exist");}

        } catch (Exception e) {
            Logging.Exception(e);
        }

        //Checks if the cursor is empty
        if( cursor != null && cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(pColumnName));
        }

        //Grabs the intended value
        return null;
    }

    /**
     * Used to update a cell value in a database
     * @param pPuzzleName - The private key (PuzzleName)
     * @param pColumnName - The column that needs changing
     * @param pValue - The value to change the cell to
     */
    public void UpdateCell(String pPuzzleName, String pColumnName, String pValue) {
        SQLiteDatabase database = new DatabaseHelper(mContext).getWritableDatabase();

        String sqlCmd = String.format("UPDATE %s SET %s=\'%s\' WHERE %s=\'%s\'",
                DBContract.Puzzle.TABLE_NAME,
                pColumnName, pValue,
                DBContract.Puzzle.TABLE_PRIMARY_KEY_NAME, pPuzzleName);

        database.execSQL(sqlCmd);
    }
}
