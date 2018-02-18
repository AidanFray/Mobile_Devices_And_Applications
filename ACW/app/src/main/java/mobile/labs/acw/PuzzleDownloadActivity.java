package mobile.labs.acw;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Views.PuzzleDownloadView;

public class PuzzleDownloadActivity extends AppCompatActivity {

    LinearLayout mDownloadLayout;

    //Temp placeholder
    Drawable mStockThumnnail;

    private final String mBaseUrl = "http://www.simongrey.net/08027/slidingPuzzleAcw/";
    final String mPuzzleIndexUrl = "index.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_download);
        setTitle("Puzzle Download");

        mDownloadLayout = (LinearLayout)findViewById(R.id.puzzle_download_layout);
        mStockThumnnail = getResources().getDrawable(R.mipmap.ic_launcher);

        //Downloads the JSON index for all the puzzles
        new PuzzlePreviewDownload().execute(mBaseUrl + mPuzzleIndexUrl);
    }

    //Adds the custom control containing the puzzle code
    private void addDownloadPuzzle(String pDescription, Drawable pThumbnail) {
        //Adds each custom view
        PuzzleDownloadView downloadRow = new PuzzleDownloadView(this);
        downloadRow.setPuzzleDescription(pDescription);
        downloadRow.setThumbnail(pThumbnail);
        mDownloadLayout.addView(downloadRow);
    }

    //Method that loops through all JSON values and adds them to the view
    //This is also called when the AsyncTask has finished downloading
    private void addAllPuzzles(JSONObject jsonObject) {

        //Hides the loading bar and spacing
        mDownloadLayout.removeAllViews();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("PuzzleIndex");

            //Loops through each value and add to to the array
            for (int i =0; i < jsonArray.length(); i++) {

                //Adds the puzzle with the JSON string
                addDownloadPuzzle(jsonArray.get(i).toString(), mStockThumnnail);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Puzzle download classes
    public class PuzzlePreviewDownload extends AsyncTask<String, String, JSONObject> {

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
                PuzzleDownloadActivity.this.addAllPuzzles(mJSON);
            }
        }
    }

}

