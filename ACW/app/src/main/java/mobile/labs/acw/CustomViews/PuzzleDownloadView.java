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

// Custom view that contains the value for the downloadable puzzles
// It contains a Thumbnail, Description and Tick to show if it has been downloaded or not
public class PuzzleDownloadView extends LinearLayout implements View.OnClickListener {

    //Shared blank thumbnail. Static so each class doesn't keep a copy
    private static Bitmap mBlankThumbnail = null;
    private static Drawable mDownloadStatus_Drawable;

    private View mNormalView;
    private View mDownloadView;

    private ImageView mThumbnail;
    private ImageView mDownloadStatus;
    private TextView mPuzzleDescription;

    private Boolean mDownloadBool;


    public PuzzleDownloadView(Context context) {
        super(context);
        Setup();
        InflateNormalView(context);
        LoadViews();
    }

    public PuzzleDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Setup();
        InflateNormalView(context);
        LoadViews();
    }

    private void Setup() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        //Loads in the thumbnail once
        if (mBlankThumbnail == null) {
            mBlankThumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.blank_puzzle);
        }

        try {
            mNormalView = inflater.inflate(R.layout.puzzle_download_view, this, false);
            mDownloadView = inflater.inflate(R.layout.puzzle_download_progress_view, this, false);
        } catch (Exception e) {
            Logging.Exception(e);
        }
    }

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
        mDownloadBool = false;

    }

    private void InflateNormalView(Context context) {
        this.removeView(mDownloadView);
        this.addView(mNormalView);
    }

    private void InflateDownloadView(Context context) {
        this.removeView(mNormalView);
        this.addView(mDownloadView);
    }

    @Override
    public void onClick(final View pView) {

        //Only allows download if the puzzle can be downloaded
        if (!mDownloadBool) {
            InflateDownloadView(getContext());

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
                        InflateNormalView(getContext());
                        setDownloadStatus(true);
                    }
                    else {
                        InflateNormalView(getContext());
                        Toast.makeText(getContext(), "Error downloading puzzle. No Connection!", Toast.LENGTH_LONG).show();
                    }
                }
            }).execute(String.valueOf(mPuzzleDescription.getText()));
        }
    }

    public void setThumbnail(Bitmap pIcon) {

        if (pIcon == null) {
            mThumbnail.setImageBitmap(mBlankThumbnail);
        }
        else {
            mThumbnail.setImageBitmap(pIcon);
        }
    }
    public void setPuzzleDescription(String pDescription) {
        mPuzzleDescription.setText(pDescription);
    }
    public void setDownloadStatus(Boolean pState) {
        mDownloadBool = pState;
        refreshDownloadStatusImage();
    }
    private void refreshDownloadStatusImage() {
        if (mDownloadBool) {
            mDownloadStatus.setImageDrawable(mDownloadStatus_Drawable);
        } else {
            mDownloadStatus.setImageDrawable(null);
        }
    }
}