package mobile.labs.acw;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import mobile.labs.acw.CustomViews.PuzzleDownloadView;
import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Puzzle_Class.Puzzle;


//===============================================================================================//
//TODO: Need to save the index file
//TODO: Update the index file every hour?
//  - Check last time it was saved and if it is longer than an hour re download
//TODO: Too much work is being done on the main thread when adding all the custom controls??
//===============================================================================================//

public class PuzzleDownloadActivity extends AppCompatActivity {

    private LinearLayout mDownloadLayout;

    private final String mBaseUrl = "http://www.simongrey.net/08027/slidingPuzzleAcw/";
    private final String mPuzzleIndexUrl = "index.json";
    private final String mPuzzleIndexLocalName = "index.dat";
    private final String mPuzzleIndexDir = "Index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_download);
        setTitle("Puzzle Download");

        mDownloadLayout = (LinearLayout)findViewById(R.id.puzzle_download_layout);

        if (!checkForDownlaodedPuzzleIndex()) {
            //Downloads the JSON index for all the puzzles
            new PuzzlePreviewDownload().execute(mBaseUrl + mPuzzleIndexUrl);
        } else {
            addAllPuzzles(JSON.ReadFromFile(getDir(mPuzzleIndexDir, MODE_PRIVATE).getAbsolutePath() + "/" + mPuzzleIndexLocalName));
        }
    }

    /**
     * Saves the index file so each time the activity is loaded a new file
     * isn't downlaoded
     * @param jsonObject - JSON puzzle index
     */
    private void saveIndexFile(JSONObject jsonObject) {

        try {
            //Writes the index to a file
            File indexDir = this.getDir(mPuzzleIndexDir, Context.MODE_PRIVATE);
            indexDir.mkdir();

            Writer stream =
                    new FileWriter(indexDir.getAbsolutePath() + "/" + mPuzzleIndexLocalName);

            stream.write(jsonObject.toString());
            stream.close();

        } catch (IOException e) {
            new Logging<IOException>().Exception(e, e.getMessage());
        }

    }

    /**
     * Method that loops through all JSON values and adds them to the view
     * This is also called when the AsyncTask has finished downloading
     * @param jsonObject - Containing all the JSON objects
     */
    private void addAllPuzzles(JSONObject jsonObject) {

        //Hides the loading bar and spacing
        mDownloadLayout.removeAllViews();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("PuzzleIndex");

            //Loops through each value and add to to the array
            for (int i =0; i < jsonArray.length(); i++) {

                String name = jsonArray.get(i).toString();

                //Grabs the bit before the filename
                name = name.split(".json")[0];

                //Adds the puzzle with the JSON string
                addDownloadPuzzle(name);
            }
        } catch (Exception e) {
            Logging.Exception(e);
        }
    }

    /**
     * Method that adds the custom control to the view
     * @param pDescription - Puzzle Name
     */
    private void addDownloadPuzzle(String pDescription) {
        //Adds each custom view
        PuzzleDownloadView downloadRow = new PuzzleDownloadView(this);

        Boolean downloaded = checkForDownloadedPuzzle(pDescription);

        if (downloaded) {
            Puzzle puzzle = new Puzzle(this, pDescription);
            downloadRow.setThumbnail(puzzle.getPuzzleThumbnail());
        }
        else {
            try {
                //Sets the thumbnail to a default photo
                downloadRow.setThumbnail(null);
            } catch (OutOfMemoryError e) {
                new Logging<OutOfMemoryError>().Exception(e, e.getMessage());
            }
        }

        //Checks to see if it has been downloaded before
        downloadRow.setDownloadStatus(downloaded);
        downloadRow.setPuzzleDescription(pDescription);
        mDownloadLayout.addView(downloadRow);
    }

    /**
     * Method that checks a puzzle has been downloaded
     * @param pPuzzleName - Name of the puzzle to check
     * @return - Returns a bool:
     *      True    - Puzzle has been downloaded
     *      False   - Puzzle has not been downlaoded
     */
    private Boolean checkForDownloadedPuzzle(String pPuzzleName) {

        SharedPreferences preferences = getSharedPreferences("Puzzles", MODE_PRIVATE);
        return preferences.getBoolean(pPuzzleName, false);
    }
    private Boolean checkForDownlaodedPuzzleIndex() {
        File indexDir = getDir(mPuzzleIndexDir, Context.MODE_PRIVATE);
        File file = new File(indexDir.getAbsolutePath() +"/" + mPuzzleIndexLocalName);
        return file.exists();
    }

    private class PuzzlePreviewDownload extends AsyncTask<String, String, JSONObject> {
        /**
         * An Async task that deals with downloading of JSON from a provided URL
         */

        private JSONObject mJSON;

        @Override
        protected JSONObject doInBackground(String... args) {
            mJSON = JSON.ReadFromURL(args[0]);
            return mJSON;
        }

        @Override
        protected void onPostExecute(JSONObject s) {
            //Displays an error message
            if (s == null) {
                Toast.makeText(PuzzleDownloadActivity.this, "Error downloaing JSON puzzles", Toast.LENGTH_LONG).show();
            }
            else {
                PuzzleDownloadActivity.this.saveIndexFile(mJSON);
                PuzzleDownloadActivity.this.addAllPuzzles(mJSON);
            }
        }

        private void saveIndex(JSONObject e) {}
    }
}

