package mobile.labs.acw.Views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import mobile.labs.acw.R;

// Custom view that contains the value for the downloadable puzzles
// It contains a Thumbnail, Description and Tick to show if it has been downloaded or not
public class PuzzleDownloadView extends LinearLayout implements View.OnClickListener {

    private ImageView mThumbnail;
    private ImageView mDownloadStatus;
    private TextView mPuzzleDescription;
    private Boolean mDownloadBool;
    private ProgressBar mDownloadProgress;

    private Drawable mDownloadStatus_Drawable;

    public PuzzleDownloadView(Context context) {
        super(context);
        InflateNormalView(context);
        LoadViews();
    }

    public PuzzleDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        InflateNormalView(context);
        LoadViews();
    }

    private void LoadViews(){

        //Grabs all controls
        mThumbnail = (ImageView)findViewById(R.id.puzzle_thumbnail_ImageView);
        mThumbnail.setClickable(true);
        mThumbnail.setOnClickListener(this);
        mDownloadStatus = (ImageView)findViewById(R.id.puzzle_download_status_ImageView);
        mDownloadStatus.setImageDrawable(null);
        mDownloadStatus.setClickable(true);
        mDownloadStatus.setOnClickListener(this);
        mDownloadStatus_Drawable = getResources().getDrawable(R.drawable.download_status);
        mPuzzleDescription = (TextView)findViewById(R.id.puzzle_description_TextView);
        mPuzzleDescription.setClickable(true);
        mPuzzleDescription.setOnClickListener(this);
        mDownloadProgress = (ProgressBar)findViewById(R.id.puzzle_download_progess);
        mDownloadBool = false;

    }

    private void InflateNormalView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View normalView =  inflater.inflate(R.layout.puzzle_download_view, this, false);
        this.removeAllViews();
        this.addView(normalView);
    }

    private void InflateDownloadView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View downloadView =  inflater.inflate(R.layout.puzzle_download_progress_view, this, false);
        this.removeAllViews();
        this.addView(downloadView);
    }

    @Override
    public void onClick(View pView) {
        //toggleDownloadStatus();
        InflateDownloadView(getContext());
    }

    public void setThumbnail(Drawable pIcon) {
        mThumbnail.setImageDrawable(pIcon);
    }
    public void setPuzzleDescription(String pDescription) {
        mPuzzleDescription.setText(pDescription);
    }
    public void setDownloadStatus(Boolean pState) {
        mDownloadBool = pState;
        refreshDownloadStatusImage();
    }
    public void toggleDownloadStatus() {

        //A toggle for the boolean value
        mDownloadBool = !mDownloadBool;
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