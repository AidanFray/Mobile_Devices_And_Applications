package mobile.labs.acw;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Puzzle_Class.Puzzle;
import mobile.labs.acw.Puzzle_Class.Row;

public class PuzzleSolvingActivity extends Activity {

    //Layout's views
    private RelativeLayout mGridLayout;
    private LinearLayout mMainLayout;
    List<ImageView> mGridElements = new ArrayList<>();

    private Spinner mPuzzleSpinner;

    private float deltaX;
    private float deltaY;

    private float max_screen_width = 0;
    private float max_screen_height = 0;

    //Both sides of the layout are exactly the same
    private float layoutSideWidth = 0;
    private float layoutSideHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_solving);

        mMainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        mGridLayout = (RelativeLayout) findViewById(R.id.gridLayout);
        mPuzzleSpinner = (Spinner) findViewById(R.id.puzzleSpinner);

        mPuzzleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onPuzzleSelection(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mGridLayout.post(new Runnable() {
            @Override
            public void run() {
                mGridLayout.getLayoutParams().height = mGridLayout.getWidth();

                float padding = (getResources().getDimension(R.dimen.gridCustomBorder) * 2);
                layoutSideWidth = mGridLayout.getWidth() - padding;

                controlSetup();
            }
        });
    }

    private void controlSetup() {
        loadDownloadedPuzzles();
    }

    /**
     * Run when the selected index on the puzzle spinner is changed
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    private void onPuzzleSelection(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView) view;
        String puzzleName = textView.getText().toString();

        //Loads the puzzle
        Puzzle puzzle = new Puzzle(this, puzzleName);

        //Puts the image into a linear list
        List<Bitmap> imageList = new ArrayList<>();
        for (Row row : puzzle.getPuzzlesImages()) {
            //Gets a list of the images
            List<Bitmap> images = row.getElements();

            for (Bitmap image : images) {
                imageList.add(image);
            }
        }

        generateGrid(puzzle.getPuzzleSizeX(), puzzle.getmPuzzleSizeY(), imageList);
    }


    /**
     * Toast used to inform the user that they need to download a puzzle
     */
    private void showNeedToDownloadPuzzleToast() {
        Toast.makeText(
                this,
                "There are not puzzles to play, please download a puzzle",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Method that generates a grid of a specific size. Each element is an image view
     *
     * @param sizeX  - Number of tiles in the X axis
     * @param sizeY  - Number of tiles in the y axis
     * @param images - Image list
     */
    private void generateGrid(int sizeX, int sizeY, List<Bitmap> images) {
        //Re-sizes the grids height

        mGridElements.clear();
        mGridLayout.removeAllViews();

        //Grabs the dimensions of each grid
        int stepSize = (int) (layoutSideWidth / sizeX);

        int imageIndex = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                ImageView view = new ImageView(this);

                view.setMaxWidth(stepSize);
                view.setMinimumWidth(stepSize);

                view.setMaxHeight(stepSize);
                view.setMinimumHeight(stepSize);

                //Adds all images to the imageViews
                Bitmap bmp = images.get(imageIndex);
                if (bmp != null) {
                    view.setImageBitmap(bmp);
                }
                imageIndex++;

                view.setOnTouchListener(new OnSwipeListener(this) {
                    //TODO: Add custom code here
                });
                mGridLayout.addView(view);

                //Changes position
                RelativeLayout.LayoutParams param
                        = (RelativeLayout.LayoutParams) view.getLayoutParams();

                param.leftMargin = (x * stepSize);
                param.topMargin = (y * stepSize);
                param.bottomMargin = 0;
                param.rightMargin = 0;

                mGridElements.add(view);
            }
        }

        //TODO: Why in the hell does this work??
        mGridLayout.getLayoutParams().height = -5;

        mGridLayout.invalidate();
    }

    /**
     * Method that displays the downloaded puzzles in the spinner at the bottom of the page
     */
    private void loadDownloadedPuzzles() {

        String puzzleIndexDir
                = getDir(getString(R.string.puzzleIndexDir), MODE_PRIVATE).getAbsolutePath();
        File indexFile
                = new File(puzzleIndexDir + "/" + getString(R.string.puzzleIndexLocalName));

        if (!indexFile.exists()) {
            showNeedToDownloadPuzzleToast();
            return;
        }

        //Grabs the values from the json
        JSONObject index = JSON.ReadFromFile(indexFile.getAbsolutePath());
        JSONArray indexValues = JSON.GetJSONArray(index, getString(R.string.jsonArrayIndexName));

        //Checks if puzzles are downloaded
        SharedPreferences sharedPreferences
                = getSharedPreferences(getString(R.string.puzzleSharedPreferencesID), MODE_PRIVATE);

        List<String> downloadedPuzzles = new ArrayList<>();
        for (int i = 0; i < indexValues.length(); i++) {
            String puzzleName = (String) JSON.GetIndex(indexValues, i);

            //Chops off the file name
            puzzleName = puzzleName.split(".json")[0];

            if (sharedPreferences.getBoolean(puzzleName, false)) {
                downloadedPuzzles.add(puzzleName);
            }
        }

        if (downloadedPuzzles.size() == 0) {
            showNeedToDownloadPuzzleToast();
            return;
        }

        //Adds the values to the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                android.R.id.text1,
                downloadedPuzzles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPuzzleSpinner.setAdapter(adapter);
    }

    class OnSwipeListener implements View.OnTouchListener {

        private final Context mContext;
        private final GestureDetector mGestureDetector;

        public OnSwipeListener(Context context) {
            mContext = context;
            mGestureDetector = new GestureDetector(context, new SwipeGesture());
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return mGestureDetector.onTouchEvent(motionEvent);
        }

        public void OnSwipeRight() {
            Toast.makeText(mContext, "Right!", Toast.LENGTH_SHORT).show();
        }

        public void OnSwipeLeft() {
            Toast.makeText(mContext, "Left!", Toast.LENGTH_SHORT).show();

        }

        public void OnSwipeDown() {
            Toast.makeText(mContext, "Down!", Toast.LENGTH_SHORT).show();

        }

        public void OnSwipeUp() {
            Toast.makeText(mContext, "Up!", Toast.LENGTH_SHORT).show();

        }

        class SwipeGesture extends GestureDetector.SimpleOnGestureListener {

            private int THRESHOLD_SWIPE = 100;
            private int THRESHOLD_VELOCITY = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                //If it's going in a strong horizontal direction
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (PastThreshold(diffX, velocityX)) {
                        if (diffX > 0) {
                            OnSwipeRight();
                        } else {
                            OnSwipeLeft();
                        }
                        return true;
                    }

                }
                //If it's going in a strong vertical direction
                else if (PastThreshold(diffY, velocityY)) {
                    if (diffY > 0) {
                        OnSwipeDown();
                    } else {
                        OnSwipeUp();
                    }
                    return true;
                }
                return false;
            }

            private boolean PastThreshold(float difference, float velocity) {

                if ((Math.abs(difference) > THRESHOLD_SWIPE) && (Math.abs(velocity) > THRESHOLD_VELOCITY)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
