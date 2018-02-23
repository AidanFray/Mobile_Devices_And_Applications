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
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.CustomViews.PuzzleDownloadView;
import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Puzzle_Class.Puzzle;


public class PuzzleDownloadActivity extends AppCompatActivity {

    private LinearLayout mDownloadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_download);
        setTitle("Puzzle Download");

        mDownloadLayout = (LinearLayout)findViewById(R.id.puzzle_download_layout);

        if (checkIfPuzzleIndexNeedsDownload()) {
            //Downloads the JSON index for all the puzzles
            new PuzzlePreviewDownload().execute(
                            getString(R.string.baseURL) +
                            getString(R.string.puzzleIndexFileName));
        }
        else {
            loadIndexFile();
        }
    }

    /**
     * TODO:
     */
    private void loadIndexFile() {
        String indexDir = getDir(this.getString(R.string.puzzleIndexDir), MODE_PRIVATE).getAbsolutePath();
        new CreateCustomDownloadViews().execute(
                JSON.ReadFromFile(indexDir + "/" + getString(R.string.puzzleIndexLocalName)));
    }

    /**
     * TODO:
     * @return
     */
    private File getFileIndexFile() {
        File indexDir = getDir(getString(R.string.puzzleIndexDir), Context.MODE_PRIVATE);
        File file = new File(indexDir.getAbsolutePath() +"/" + getString(R.string.puzzleIndexLocalName));
        return file;
    }

    /**
     * Saves the index file so each time the activity is loaded a new file
     * isn't downlaoded
     * @param jsonObject - JSON puzzle index
     */
    private void saveIndexFile(JSONObject jsonObject) {

        try {
            //Writes the index to a file
            File indexDir = this.getDir(getString(R.string.puzzleIndexDir), Context.MODE_PRIVATE);
            indexDir.mkdir();

            Writer stream =
                    new FileWriter(indexDir.getAbsolutePath() + "/" + getString(R.string.puzzleIndexLocalName));

            stream.write(jsonObject.toString());
            stream.close();

        } catch (IOException e) {
            new Logging<IOException>().Exception(e, e.getMessage());
        }
    }

    /**
     * Method called after the AsycTask has created all the custom download views. The views are
     * then just simply added to the layout
     * @param puzzleDownloadView - The final list of created views
     */
    private void addAllViews(List<PuzzleDownloadView> puzzleDownloadView) {
        //Hides the loading bar and spacing
        mDownloadLayout.removeAllViews();

        for (PuzzleDownloadView view: puzzleDownloadView) {
            mDownloadLayout.addView(view);
        }
    }

    /**
     * Method that checks a puzzle has been downloaded
     * @param pPuzzleName - Name of the puzzle to check
     * @return - Returns a bool:
     *      True    - Puzzle has been downloaded
     *      False   - Puzzle has not been downlaoded
     */
    private Boolean checkForDownloadedPuzzle(String pPuzzleName) {

        SharedPreferences preferences = getSharedPreferences(getString(R.string.puzzleSharedPreferencesID), MODE_PRIVATE);
        return preferences.getBoolean(pPuzzleName, false);
    }

    /**
     * Method that checks if the Index file needs a download. It checks for it's existence and
     * when it was last modified. If it was last modifies an hour ago it re downloads
     * @return - A boolean stating if it should be downloaded or not:
     *      True    - Re-download
     *      False   - Use saved version
     */
    private Boolean checkIfPuzzleIndexNeedsDownload() {

        File puzzleIndex = getFileIndexFile();
        if (!puzzleIndex.exists()) return true;

        //Grabs the last time the file was saved
        long currentTime = System.currentTimeMillis();
        long lastSaveTime = puzzleIndex.lastModified();
        long difference = currentTime - lastSaveTime;

        //3600000 is an hour in milli seconds
        if (difference > 3600000) return true;
        return false;
    }

    /**
     * AsyncTask that downloads the JSON index file
     */
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
                mDownloadLayout.removeAllViews();
                Toast.makeText(PuzzleDownloadActivity.this, "Error: No Connection!", Toast.LENGTH_LONG).show();

                //If the file is cached
                if (getFileIndexFile().exists()) {
                    loadIndexFile();
                }
            }
            else {
                PuzzleDownloadActivity.this.saveIndexFile(mJSON);
                new CreateCustomDownloadViews().execute(mJSON);
            }
        }
    }

    /**
     * AsyncTask that uses the index file to create a series of custom download views
     */
    private class CreateCustomDownloadViews extends AsyncTask<JSONObject, JSONObject, Void> {

        //List that holds the created views
        private List<PuzzleDownloadView> downloadViewList = new ArrayList<>();

        @Override
        protected Void doInBackground(JSONObject... json) {
            createCustomViews(json[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Adds all the created views to the window
            addAllViews(downloadViewList);
        }

        /**
         * Method that loops through all JSON values and adds them to the view
         * This is also called when the AsyncTask has finished downloading
         * @param jsonObject - Containing all the JSON objects
         */
        private void createCustomViews(JSONObject jsonObject) {

            try {
                JSONArray jsonArray = jsonObject.getJSONArray(getString(R.string.jsonArrayIndexName));

                //Loops through each value and add to to the array
                for (int i =0; i < jsonArray.length(); i++) {

                    String name = jsonArray.get(i).toString();

                    //Grabs the bit before the filename
                    name = name.split(".json")[0];

                    //Adds each custom view
                    PuzzleDownloadView downloadRow = new PuzzleDownloadView(PuzzleDownloadActivity.this);
                    Boolean downloaded = checkForDownloadedPuzzle(name);

                    if (downloaded) {
                        Puzzle puzzle = new Puzzle(PuzzleDownloadActivity.this, name);
                        downloadRow.setThumbnail(puzzle.getPuzzleThumbnail());
                    }
                    else {
                        //Sets the thumbnail to a default photo
                        downloadRow.setThumbnail(null);

                    }

                    //Checks to see if it has been downloaded before
                    downloadRow.setDownloadStatus(downloaded);
                    downloadRow.setPuzzleDescription(name);
                    downloadViewList.add(downloadRow);
                }
            } catch (Exception e) {
                Logging.Exception(e);
            }
        }
    }
}

