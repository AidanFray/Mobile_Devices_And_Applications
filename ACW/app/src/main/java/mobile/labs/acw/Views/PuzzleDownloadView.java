package mobile.labs.acw.Views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import mobile.labs.acw.R;

public class PuzzleDownloadView extends LinearLayout {

    private ImageView mThumbnail;
    private ImageView mDownloadStatus;
    private TextView mPuzzleDescription;

    private void Inflate(Context context) {
        //Inflates the XML view
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.puzzle_download_view, this);
    }

    public PuzzleDownloadView(Context context) {
        super(context);
        Inflate(context);
        LoadViews();
    }

    public PuzzleDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Inflate(context);
        LoadViews();
    }

    private void LoadViews(){
        mThumbnail = (ImageView)findViewById(R.id.puzzle_thumbnail_ImageView);
        mDownloadStatus = (ImageView)findViewById(R.id.puzzle_download_status_ImageView);
        mPuzzleDescription = (TextView)findViewById(R.id.puzzle_description_TextView);
    }

    public void setThumbnail(Drawable icon) {
        mThumbnail.setImageDrawable(icon);
    }

    public void setPuzzleDescription(String text) {
        mPuzzleDescription.setText(text);
    }

    public void setmDownloadStatus(Boolean state) {
        if (state) {
            //TODO: Set tick image
        }
    }
}