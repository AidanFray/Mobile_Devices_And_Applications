package mobile.labs.acw;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.JSON.JSON;

public class PuzzleSolvingActivity extends Activity {

    //Layout's views
    private RelativeLayout mGridLayout;
    List<ImageView> mGridElements = new ArrayList<>();

    private Spinner mPuzzleSpinner;

    private float deltaX;
    private float deltaY;

    private float max_screen_width = 0;
    private float max_screen_height = 0;

    //Both sides of the layout are exactly the same
    private float layoutSideWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_solving);

        mGridLayout = (RelativeLayout)findViewById(R.id.gridLayout);
        mPuzzleSpinner = (Spinner)findViewById(R.id.puzzleSpinner);

        mPuzzleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onPuzzleSelection(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mGridLayout.post(new Runnable() {
            @Override
            public void run() {
                mGridLayout.getLayoutParams().height = mGridLayout.getWidth();
                layoutSideWidth = mGridLayout.getWidth() - (getResources().getDimension(R.dimen.gridCustomBorder) * 2);
                controlSetup();
            }
        });
    }

    private void controlSetup() {
        loadDownloadedPuzzles();
    }

    /**
     * Run when the selected index on the puzzle spinner is changed
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    private void onPuzzleSelection(AdapterView<?> adapterView, View view, int i, long l) {
        //TODO: Add code here that loads a puzzle selected into the grid
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
     * @param size - Specifies the grids width and height (Size X Size)
     */
    private void generateGrid(int size) {

        //Gets the total size of the grid
        float totalWidth = layoutSideWidth;

        //Grabs the dimensions of each grid
        int stepSize = (int) (totalWidth / size);

        boolean colour = false;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                ImageView view = new ImageView(this);

                view.setMaxWidth(stepSize);
                view.setMaxHeight(stepSize);
                view.setMinimumWidth(stepSize);
                view.setMinimumHeight(stepSize);

                view.setAdjustViewBounds(true);

                //TODO: Temp - Puzzle loading code will go here
                if (colour) {
                    view.setBackgroundColor(Color.GRAY);
                } else {
                    view.setBackgroundColor(Color.DKGRAY);
                }

                mGridLayout.addView(view);

                view.setOnTouchListener(createOnTouch());

                //Changes position
                RelativeLayout.LayoutParams param
                        = (RelativeLayout.LayoutParams) view.getLayoutParams();

                param.leftMargin = (int) (x * stepSize);
                param.topMargin = (int) (y * stepSize);
                param.bottomMargin = 0;
                param.rightMargin = 0;

                mGridElements.add(view);

                colour = !colour;
            }

            //Only alternates the colour is an even value
            if (size % 2 == 0) {
                colour = !colour;
            }
        }
        mGridLayout.invalidate();
    }

    /**
     * Method that displays the downloaded puzzles in the spinner at the bottom of the page
     */
    private void loadDownloadedPuzzles() {
        //Use the index file
        //  If      - the index file is not downloaded push the user to the downloads page?
        //  Else    - use the list to search for downloaded puzzles
        //      If      - there are not puzzles push the user to the downloads page
        //      Else    - Places the downloaded puzzles in the spinner

        String puzzleIndexDir
                = getDir(PuzzleDownloadActivity.mPuzzleIndexDir, MODE_PRIVATE).getAbsolutePath();
        File indexFile
                = new File(puzzleIndexDir + "/" + PuzzleDownloadActivity.mPuzzleIndexLocalName);

        if (!indexFile.exists()) {
            showNeedToDownloadPuzzleToast();
        }

        //Grabs the values from the json

        JSONObject index = JSON.ReadFromFile(indexFile.getAbsolutePath());
        JSONArray indexValues = JSON.GetJSONArray(index, PuzzleDownloadActivity.mJSONArrayIndexID);

        //Checks if puzzles are downloaded
        SharedPreferences sharedPreferences
                = getSharedPreferences(PuzzleDownloadActivity.mPuzzleSharedPreferences, MODE_PRIVATE);

        List<String> downloadedPuzzles = new ArrayList<>();
        for (int i = 0; i < indexValues.length(); i++) {
            String puzzleName = (String)JSON.GetIndex(indexValues, i);

            //Chops off the file name
            puzzleName = puzzleName.split(".json")[0];

            if (sharedPreferences.getBoolean(puzzleName, false)) {
                downloadedPuzzles.add(puzzleName);
            }
        }

        if (downloadedPuzzles.size() == 0) {
            showNeedToDownloadPuzzleToast();
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

    /**
     * Method that returns a custom object that can be used to control the movement of the tiles
     *
     * @return The custom OnTouchListener object
     */
    private View.OnTouchListener createOnTouch() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getRawX() - view.getWidth() / 2;
                float y = motionEvent.getRawY() - view.getHeight() / 2;

                max_screen_width = layoutSideWidth - view.getWidth();
                max_screen_height = layoutSideWidth - view.getHeight();

                //Boundaries for keeping image views on screen
                if (x < 0) {
                    x = 0;
                }
                if (y < 0) {
                    y = 0;
                }
                if (x > max_screen_width) {
                    x = max_screen_width;
                }
                if (y > max_screen_height) {
                    y = max_screen_height;
                }

                deltaX = 0;
                deltaY = 0;

                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:

                        RelativeLayout.LayoutParams param
                                = (RelativeLayout.LayoutParams) view.getLayoutParams();

                        deltaX = x - param.leftMargin;
                        deltaY = y - param.rightMargin;
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams
                                = (RelativeLayout.LayoutParams) view.getLayoutParams();

                        layoutParams.leftMargin = (int) (x - deltaX);
                        layoutParams.topMargin = (int) (y - deltaY);
                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = 0;
                        view.setLayoutParams(layoutParams);
                        break;
                }


//                //TODO: Margin for error for movements
//                int buffer = 20;
//
//                float startX, startY;
//                float stopX, stopY;
//                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
//
//                    case MotionEvent.ACTION_DOWN:
//                        startX = motionEvent.getRawX();
//                        startY = motionEvent.getRawY();
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                        stopX = motionEvent.getRawX();
//                        stopY = motionEvent.getRawY();
//
//
//                }

                mGridLayout.invalidate();
                return true;
            }
        };
    }
}
