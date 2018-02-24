package mobile.labs.acw.Puzzle_Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.ExceptionHandling.Logging;
import mobile.labs.acw.Images.Image;
import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.R;

/**
 * Async task that is responsible for the downloading of a puzzle from a URL
 */
public class DownloadFullPuzzle extends AsyncTask<String, String, Puzzle> {

    public DownloadFullPuzzle(OnResultRecieved pListener, Context pContext) {
        mListener = pListener;
        mContext = pContext;
    }

    private Context mContext;
    private OnResultRecieved mListener;

    // Puzzle Information
    private String mPuzzleName;
    private String mLayoutName;
    private String mPictureSetName;
    private List<Row> mPuzzleLayout = new ArrayList<>();
    private List<Row> mPuzzleImages = new ArrayList<>();
    private int mPuzzleSizeX;
    private int mPuzzleSizeY;

    @Override
    protected Puzzle doInBackground(String... args) {
        mPuzzleName = args[0];

        try {
            getPuzzleInfo(mPuzzleName);
            getPuzzleLayout();
            getPuzzleImages();

            return new Puzzle(mPuzzleName, mPuzzleLayout, mPuzzleImages, mPuzzleSizeX, mPuzzleSizeY);
        } catch (Exception e) {
            Logging.Exception(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Puzzle puzzle) {
        //Calls the interface method
        if (mListener != null) {
            mListener.onResult(puzzle);
        }
    }

    private void getPuzzleInfo(String puzzleName) {
        JSONObject puzzle_info = JSON.ReadFromURL(
                        mContext.getString(R.string.baseURL) +
                        mContext.getString(R.string.puzzleInfoUrl) +
                        puzzleName);

        try {
            mLayoutName = (String)puzzle_info.get("layout");
            mPictureSetName = (String)puzzle_info.get("PictureSet");
        } catch (JSONException e) {
            Logging.Exception(e);
        }
    }
    private void getPuzzleLayout() {
        JSONObject puzzle_layout = JSON.ReadFromURL(
                                mContext.getString(R.string.baseURL) +
                                mContext.getString(R.string.puzzleLayoutUrl) + mLayoutName);

        JSONArray layout_info = null;
        try {
            layout_info = puzzle_layout.getJSONArray("layout");

            mPuzzleSizeY = layout_info.length();

            for (int i = 0; i < layout_info.length() ; i++) {
                JSONArray row = layout_info.getJSONArray(i);

                //Loops through the values and creates a new row
                Row<String> newRow = new Row<>();
                for (int j = 0; j < row.length() ; j++) {
                    newRow.add((String)row.get(j));
                }
                mPuzzleSizeX = newRow.getElements().size();

                mPuzzleLayout.add(newRow);
            }

        } catch (JSONException e) {
            Logging.Exception(e);
        }
    }
    private void getPuzzleImages() {

        //Loops row all the rows to grab image names
        for (int i = 0; i < mPuzzleLayout.size(); i++) {

            Row row = mPuzzleLayout.get(i);

            //New row being created
            Row<Bitmap> imageRow = new Row<>();

            for (int j = 0; j < row.getElements().size(); j++) {
                String fileName = (String)row.getElements().get(j);

                //Grabs the image from URL
                if (!fileName.equals("empty")) {
                    Bitmap image = Image.DownloadFromURL(
                                    mContext.getString(R.string.baseURL) +
                                    mContext.getString(R.string.puzzleImageUrl) + mPictureSetName + "/" + fileName + ".jpg");
                    imageRow.getElements().add(image);
                } else{
                    //Adds a blank position
                    imageRow.add(null);
                }
            }
            mPuzzleImages.add(imageRow);
        }
    }

    // Listener interface that allows this AsyncTask to be used Globally
    public interface OnResultRecieved {
        public void onResult(Puzzle result);
    }
}