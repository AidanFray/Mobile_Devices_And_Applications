package mobile.labs.acw.CustomViews;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import mobile.labs.acw.ExceptionHandling.Logging;
import mobile.labs.acw.Puzzle_Class.DownloadFullPuzzle;
import mobile.labs.acw.Puzzle_Class.Puzzle;
import mobile.labs.acw.R;


/**
 * Custom view that contains the value for the downloadable puzzles
 * It contains a Thumbnail, Description and Tick to show if it has been downloaded or not
 */
public class PuzzleDownloadView extends LinearLayout implements View.OnClickListener {

    //Shared images between all instances;
    private static Bitmap mBlankThumbnail = null;
    private static Drawable mDownloadStatus_Drawable = null;

    private View mNormalView;
    private View mDownloadView;

    private ImageView mThumbnail;
    private ImageView mDownloadStatus;
    private TextView mPuzzleDescription;
    private TextView mPuzzleHighscore;

    private Boolean mDownloadBool;

    public PuzzleDownloadView(Context context) {
        super(context);
        Setup();
        InflateNormalView();
        LoadViews();
    }

    public PuzzleDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Setup();
        InflateNormalView();
        LoadViews();
    }

    @Override
    public void onClick(final View pView) {

        //Only allows download if the puzzle can be downloaded
        if (!mDownloadBool) {
            InflateDownloadView();

            new DownloadFullPuzzle(new DownloadFullPuzzle.OnResultRecieved() {
                @Override
                public void onResult(Puzzle result) {

                    if (result != null) {
                        //Saves the presence of the puzzle download
                        SharedPreferences.Editor editor
                                = getContext().getSharedPreferences("Puzzles", Context.MODE_PRIVATE).edit();

                        String puzzleName = String.valueOf(mPuzzleDescription.getText());
                        editor.putBoolean(puzzleName, true);
                        editor.commit();

                        result.Save(getContext());

                        //Gets the first image
                        mThumbnail.setImageBitmap(result.getPuzzleThumbnail());

                        //Sets progress bar to done
                        InflateNormalView();
                        setDownloadStatus(true);
                    }
                    else {
                        InflateNormalView();
                        Toast.makeText(getContext(), "Error downloading puzzle. No Connection!", Toast.LENGTH_LONG).show();
                    }
                }
            }, getContext()).execute(String.valueOf(mPuzzleDescription.getText()));
        }
    }


    /**
     * Code that is used to create objects and inflate views
     */
    private void Setup() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        //Loads in the thumbnail once
        if (mBlankThumbnail == null) {

            //TODO: Would it be more effcient to create a blank canvas with a border??
            mBlankThumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.blank_puzzle);
        }

        try {
            mNormalView = inflater.inflate(R.layout.puzzle_download_view, this, false);
            mDownloadView = inflater.inflate(R.layout.puzzle_download_progress_view, this, false);
        } catch (Exception e) {
            Logging.Exception(e);
        }
    }

    /**
     * Loads in all the necessary views for the
     */
    private void LoadViews() {

        //Grabs all controls
        mThumbnail = (ImageView) findViewById(R.id.puzzle_thumbnail_ImageView);
        mThumbnail.setClickable(true);
        mThumbnail.setOnClickListener(this);
        mDownloadStatus = (ImageView) findViewById(R.id.puzzle_download_status_ImageView);
        mDownloadStatus.setImageDrawable(null);
        mDownloadStatus.setClickable(true);
        mDownloadStatus.setOnClickListener(this);
        mDownloadStatus_Drawable = getResources().getDrawable(R.drawable.download_status);
        mPuzzleDescription = (TextView) findViewById(R.id.puzzle_description_TextView);
        mPuzzleDescription.setClickable(true);
        mPuzzleDescription.setOnClickListener(this);
        mPuzzleHighscore = (TextView)findViewById(R.id.puzzle_highscore_TextView);
        mPuzzleHighscore.setClickable(true);
        mPuzzleHighscore.setOnClickListener(this);
        mDownloadBool = false;

    }

    /**
     * Inflates the view containing a thumbnail and puzzle name plus an optional
     * image showing if the puzzle has previously been downloaded
     */
    private void InflateNormalView() {
        this.removeView(mDownloadView);
        this.addView(mNormalView);
    }

    /**
     * Inflates the view containing the download bar to inform the user
     * that the puzzle is in the process of being downloaded
     */
    private void InflateDownloadView() {
        this.removeView(mNormalView);
        this.addView(mDownloadView);
    }

    /**
     * Sets the custom thumbnail for the view
     * @param pIcon - The bitmap object that will be used as the thumbnail
     */
    public void setThumbnail(Bitmap pIcon) {

        if (pIcon == null) {
            mThumbnail.setImageBitmap(mBlankThumbnail);
        }
        else {
            mThumbnail.setImageBitmap(pIcon);
        }
    }

    /**
     * Sets the TextView that contains the name of the puzzle
     * @param pDescription - The text that will be provided to the TextView
     */
    public void setPuzzleDescription(String pDescription) {
        mPuzzleDescription.setText(pDescription);
    }

    /**
     * Sets if the puzzle has been downloaded or not.
     * @param pState - The boolean representing the download state:
     *            True: Puzzle has been downloaded and tick image is shown
     *            False: Puzzle has not been downloaded and no image is shown
     */
    public void setDownloadStatus(Boolean pState) {
        mDownloadBool = pState;
        refreshDownloadStatusImage();
    }

    /**
     * Refreshes the state of the of the download state image
     */
    private void refreshDownloadStatusImage() {
        if (mDownloadBool) {
            mDownloadStatus.setImageDrawable(mDownloadStatus_Drawable);
            mPuzzleHighscore.setVisibility(VISIBLE);
        } else {
            mDownloadStatus.setImageDrawable(null);
            mPuzzleHighscore.setVisibility(GONE);
        }
    }
}