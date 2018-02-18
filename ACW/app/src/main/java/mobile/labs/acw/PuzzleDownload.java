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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import mobile.labs.acw.Views.PuzzleDownloadView;

public class PuzzleDownload extends AppCompatActivity {

    LinearLayout mDownloadLayout;
    Drawable mStockThumnnail;

    final String mPuzzleIndexUrl = "index.json";
    final String mPuzzleInfoUrl = "puzzles/";
    final String mPuzzleLayoutUrl = "layouts/";
    final String mPuzzleImageUrl = "images/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_download);
        setTitle("Puzzle Download");
        mDownloadLayout = (LinearLayout)findViewById(R.id.puzzle_download_layout);
        mStockThumnnail = getResources().getDrawable(R.mipmap.ic_launcher);

        //Downloads the JSON index for all the puzzles
        new DownloadJSON().execute(mPuzzleIndexUrl);
    }

    //Adds the custom control containing the puzzle code
    private void addDownloadPuzzle(String pDescription, Drawable pThumbnail) {
        PuzzleDownloadView downloadRow = new PuzzleDownloadView(this);
        downloadRow.setPuzzleDescription(pDescription);
        downloadRow.setThumbnail(pThumbnail);
        mDownloadLayout.addView(downloadRow);
    }

    private void addAllPuzzles(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("PuzzleIndex");

            //Loops through each value and add to to the array
            for (int i =0; i < jsonArray.length(); i++) {
                String jsonString = jsonArray.get(i).toString();
                addDownloadPuzzle(jsonString, mStockThumnnail);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class DownloadJSON extends AsyncTask<String, String, String> {

        final String mBaseUrl = "http://www.simongrey.net/08027/slidingPuzzleAcw/";


        private JSONObject mJSON;

        @Override
        protected String doInBackground(String... args) {

            //Downloads JSON code from an url
            InputStream inputStream = null;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                URL url = new URL(mBaseUrl + args[0]);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(1000);
                BufferedReader bufferedInputStream
                        = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line = null;
                while ((line = bufferedInputStream.readLine()) != null) {
                    stringBuilder.append(line);
                }

                String result = stringBuilder.toString();
                mJSON = new JSONObject(result);
                bufferedInputStream.close();
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //Displays an error message
            if (s == null) {
                Toast.makeText(PuzzleDownload.this, "Error downloaing JSON puzzles", Toast.LENGTH_LONG).show();
            }
            else {
                PuzzleDownload.this.addAllPuzzles(mJSON);
            }
        }
    }
}

