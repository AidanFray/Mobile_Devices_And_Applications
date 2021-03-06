package mobile.labs.acw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import mobile.labs.acw.ExceptionHandling.Logging;
import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Puzzle_Class.Puzzle;

/**
 * Activity that handles the process of downloading new puzzles. It has the responsibilities of
 * checking for new puzzles and downloading and formatting new puzzles.
 */
public class PuzzleDownloadActivity extends AppCompatActivity {

    private LinearLayout mDownloadLayout;
    private List<PuzzleDownloadView> mDownloadViews;

    private boolean mJustShowDownloadedPuzzles = false;
    private boolean mJustShowNeverPlayedPuzzles = false;
    private boolean mJustFilterCertainPuzzleSize = false;
    private String mFilterPuzzleSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_download);
        setTitle(getString(R.string.puzzle_download_menu_text));

        mDownloadLayout = (LinearLayout) findViewById(R.id.puzzle_download_layout);

        if (checkIfPuzzleIndexNeedsDownload()) {
            //Downloads the JSON index for all the puzzles
            new PuzzlePreviewDownload().execute(
                    getString(R.string.baseURL) +
                            getString(R.string.puzzleIndexFileName));
        } else {
            loadIndexFile();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.filterAction:

                final String[] puzzle_sizes = new String[]{"3x3", "4x3", "3x4", "4x4"};
                final String[] menu_items = new String[]{
                        getString(R.string.filter_menu_ShowDownloaded),
                        getString(R.string.filter_menu_NeverPlayed),
                        getString(R.string.filter_menu_puzzle_size) + puzzle_sizes[0],
                        getString(R.string.filter_menu_puzzle_size) + puzzle_sizes[1],
                        getString(R.string.filter_menu_puzzle_size) + puzzle_sizes[2],
                        getString(R.string.filter_menu_puzzle_size) + puzzle_sizes[3],
                        getString(R.string.filter_menu_Clear),
                };

                //Builds an alert dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(getString(R.string.filter_menu_title));
                alert.setItems(menu_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If statements have been used instead of switch-case because the
                        //use of resource strings does not work
                        if (mDownloadViews != null) {
                            //Just show downloaded options
                            if (menu_items[i].equals(getString(R.string.filter_menu_ShowDownloaded))) {
                                clearFilters();
                                mJustShowDownloadedPuzzles = true;
                            }
                            if (menu_items[i].equals(getString(R.string.filter_menu_NeverPlayed))) {
                                clearFilters();
                                mJustShowNeverPlayedPuzzles = true;

                            } else if (menu_items[i].equals(getString(R.string.filter_menu_Clear))) {
                                clearFilters();
                            } else {

                                //Dynamically loops round and checks for the different puzzle sizes
                                for (int j = 0; j < puzzle_sizes.length; j++) {
                                    String size = puzzle_sizes[j];

                                    if (menu_items[i].equals(getString(R.string.filter_menu_puzzle_size) + size)) {
                                        clearFilters();
                                        mJustFilterCertainPuzzleSize = true;
                                        mFilterPuzzleSize = size;
                                    }
                                }

                            }

                            addAllViews(mDownloadViews);
                        }
                        //If there are no active views
                        else {
                            Toast.makeText(
                                    PuzzleDownloadActivity.this,
                                    "Filter cannot be applied at this time, please wait for a download",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Clears the boolean filter variables
     */
    private void clearFilters() {
        mJustShowNeverPlayedPuzzles = false;
        mJustShowDownloadedPuzzles = false;
        mJustFilterCertainPuzzleSize = false;
        mFilterPuzzleSize = "";
    }

    /**
     * Loads the JSON index file from the phones internal storage
     */
    private void loadIndexFile() {
        String indexDir = getDir(this.getString(R.string.puzzleIndexDir), MODE_PRIVATE).getAbsolutePath();
        new CreateCustomDownloadViews().execute(
                JSON.ReadFromFile(indexDir + "/" + getString(R.string.puzzleIndexLocalName)));
    }

    /**
     * Gets the file object for the index file
     *
     * @return - File object pointing at the JSON index
     */
    private File getIndexFileObject() {
        File indexDir = getDir(getString(R.string.puzzleIndexDir), Context.MODE_PRIVATE);
        File file = new File(indexDir.getAbsolutePath() + "/" + getString(R.string.puzzleIndexLocalName));
        return file;
    }

    /**
     * Saves the index file so each time the activity is loaded a new file
     * isn't downlaoded
     *
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
     *
     * @param puzzleDownloadView - The final list of created views
     */
    private void addAllViews(List<PuzzleDownloadView> puzzleDownloadView) {
        //Hides the loading bar and spacing
        mDownloadLayout.removeAllViews();

        //Just shows the downloaded puzzles
        if (mJustShowDownloadedPuzzles) {
            for (PuzzleDownloadView view : puzzleDownloadView) {
                if (view.getDownloadBool()) {
                    mDownloadLayout.addView(view);
                }
            }
        } else if (mJustShowNeverPlayedPuzzles) {
            for (PuzzleDownloadView view : puzzleDownloadView) {
                if (view.getDownloadBool()) {

                    //Checks if the puzzle has ever been played
                    if (Integer.parseInt(view.getPuzzle().getTimesPlayed(this)) == 0) {
                        mDownloadLayout.addView(view);
                    }
                }
            }
        } else if (mJustFilterCertainPuzzleSize) {
            for (PuzzleDownloadView view : puzzleDownloadView) {

                if (view.getDownloadBool()) {
                    //Loads the puzzle

                    Puzzle puzzle = view.getPuzzle();

                    String size = String.valueOf(puzzle.getPuzzleSizeX()) +
                            "x" +
                            String.valueOf(puzzle.getPuzzleSizeY());

                    //If the puzzles size matches the filter
                    if (size.equals(mFilterPuzzleSize)) {
                        mDownloadLayout.addView(view);
                    }
                }
            }
        } else {
            for (PuzzleDownloadView view : puzzleDownloadView) {
                mDownloadLayout.addView(view);
            }
        }
    }

    /**
     * Method that checks a puzzle has been downloaded
     *
     * @param pPuzzleName - Name of the puzzle to check
     * @return - Returns a bool:
     * True    - Puzzle has been downloaded
     * False   - Puzzle has not been downlaoded
     */
    private Boolean checkForDownloadedPuzzle(String pPuzzleName) {

        SharedPreferences preferences = getSharedPreferences(getString(R.string.puzzleSharedPreferencesID), MODE_PRIVATE);
        return preferences.getBoolean(pPuzzleName, false);
    }

    /**
     * Method that checks if the Index file needs a download. It checks for it's existence and
     * when it was last modified. If it was last modifies an hour ago it re downloads
     *
     * @return - A boolean stating if it should be downloaded or not:
     * True    - Re-download
     * False   - Use saved version
     */
    private Boolean checkIfPuzzleIndexNeedsDownload() {

        File puzzleIndex = getIndexFileObject();
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
                if (getIndexFileObject().exists()) {
                    loadIndexFile();
                }
            } else {
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
            mDownloadViews = downloadViewList;
            addAllViews(downloadViewList);
        }

        /**
         * Method that loops through all JSON values and adds them to the view
         * This is also called when the AsyncTask has finished downloading
         *
         * @param jsonObject - Containing all the JSON objects
         */
        private void createCustomViews(JSONObject jsonObject) {

            try {
                JSONArray jsonArray = jsonObject.getJSONArray(getString(R.string.jsonArrayIndexName));

                //Loops through each value and add to to the array
                for (int i = 0; i < jsonArray.length(); i++) {

                    String name = jsonArray.get(i).toString();

                    //Grabs the bit before the filename
                    name = name.split(".json")[0];

                    //Adds each custom view
                    PuzzleDownloadView downloadRow = new PuzzleDownloadView(PuzzleDownloadActivity.this);
                    Boolean downloaded = checkForDownloadedPuzzle(name);

                    if (downloaded) {
                        Puzzle puzzle = new Puzzle(PuzzleDownloadActivity.this, name);
                        downloadRow.setThumbnail(puzzle.getPuzzleThumbnail());

                        //Saves the puzzle instance
                        downloadRow.setPuzzle(puzzle);

                        String highScore = puzzle.getHighscore(PuzzleDownloadActivity.this);
                        downloadRow.setHighScore(highScore);
                    } else {
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

