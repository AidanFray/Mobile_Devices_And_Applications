package mobile.labs.acw;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import mobile.labs.acw.Views.PuzzleDownloadView;

public class PuzzleDownload extends AppCompatActivity {

    LinearLayout mDownloadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_download);
        setTitle("Puzzle Download");

        mDownloadLayout = (LinearLayout)findViewById(R.id.puzzle_download_layout);

        //Test Examples
        addDownloadPuzzle("Cat Puzzle 3x3");
        addDownloadPuzzle("Computer Puzzle 10x10");
        addDownloadPuzzle("Dog Puzzle 4x4");
    }

    private void addDownloadPuzzle(String description) {
        PuzzleDownloadView downloadRow = new PuzzleDownloadView(this);
        downloadRow.setPuzzleDescription(description);

        Drawable d = getResources().getDrawable(R.mipmap.ic_launcher);
        downloadRow.setThumbnail(d);

        mDownloadLayout.addView(downloadRow);
    }
}
