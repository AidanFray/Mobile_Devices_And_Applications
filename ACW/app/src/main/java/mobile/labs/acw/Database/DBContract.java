package mobile.labs.acw.Database;

import android.provider.BaseColumns;

/**
 * Class defined to design the schema for the database of puzzles
 */
public final class DBContract {

    //So the class cannot have an instance
    private DBContract() {
    }

    public static class Puzzle implements BaseColumns {
        static final String TABLE_NAME = "Puzzles";
        static final String TABLE_PRIMARY_KEY = "PuzzleName VARCHAR";
        static final String TABLE_PRIMARY_KEY_NAME = TABLE_PRIMARY_KEY.split(" ")[0];

        static final String[] TABLE_COLUMNS = {
                "Highscore INT",
                "TimesPlayed INT",
                "TimesCompleted INT",
                "PuzzleDimension VARCHAR"};

        static String CREATE_TABLE() {
            String sqlCmd =  String.format("CREATE TABLE IF NOT EXISTS %s (", TABLE_NAME);

            sqlCmd += TABLE_PRIMARY_KEY + " PRIMARY KEY, ";

            //Dynamically adds the columns and types
            for (int i = 0; i < TABLE_COLUMNS.length; i++) {
                if (i > 0) { sqlCmd += ", ";}
                sqlCmd += TABLE_COLUMNS[i];
            }
            sqlCmd += ")";

            return sqlCmd;
        }
    }
}
