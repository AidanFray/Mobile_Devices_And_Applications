package mobile.labs.acw.Views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import mobile.labs.acw.DownloadFullPuzzle;
import mobile.labs.acw.Puzzle;
import mobile.labs.acw.R;

// Custom view that contains the value for the downloadable puzzles
// It contains a Thumbnail, Description and Tick to show if it has been downloaded or not
public class PuzzleDownloadView extends LinearLayout implements View.OnClickListener {

    private ImageView mThumbnail;
    private ImageView mDownloadStatus;
    private TextView mPuzzleDescription;
    private Boolean mDownloadBool;

    private Drawable mDownloadStatus_Drawable;

    private View mNormalView;
    private View mDownloadView;

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
        mNormalView = inflater.inflate(R.layout.puzzle_download_view, this, false);
        mDownloadView = inflater.inflate(R.layout.puzzle_download_progress_view, this, false);
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

                    //Saves the presence of the puzzle download
                    SharedPreferences.Editor editor
                            = getContext().getSharedPreferences("Puzzles", Context.MODE_PRIVATE).edit();

                    String puzzleName = String.valueOf(mPuzzleDescription.getText());
                    editor.putBoolean(puzzleName, true);
                    editor.commit();

                    result.Save(getContext());

                    //Sets progress bar to done
                    InflateNormalView(getContext());
                    setDownloadStatus(true);

                }
            }).execute(String.valueOf(mPuzzleDescription.getText()));
        }
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

    private void refreshDownloadStatusImage() {
        if (mDownloadBool) {
            mDownloadStatus.setImageDrawable(mDownloadStatus_Drawable);
        } else {
            mDownloadStatus.setImageDrawable(null);
        }
    }
}