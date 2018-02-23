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
    private Spinner mPuzzleSpinner;

    ImageView[][] mGridElements;

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

                loadDownloadedPuzzles();
            }
        });
    }

    /**
     * Method that displays the downloaded puzzles in the spinner at the bottom of the page
     */
    private void loadDownloadedPuzzles() {

        String puzzleIndexDir
                = getDir(getString(R.string.puzzleIndexDir), MODE_PRIVATE).getAbsolutePath();
        File indexFile
                = new File(puzzleIndexDir + "/" + getString(R.string.puzzleIndexLocalName));

        //If there is not index file
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

        //If there are no puzzles downloaded
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

        mGridElements = new ImageView[sizeY][sizeX];
        mGridLayout.removeAllViews();

        //Grabs the dimensions of each grid
        int stepSize = (int) (layoutSideWidth / sizeX);

        int imageIndex = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {

                ImageView view = null;
                Bitmap bmp = images.get(imageIndex);

                if (bmp != null) {
                    view = new ImageView(this);
                    view.setImageBitmap(bmp);

                    view.setMaxWidth(stepSize);
                    view.setMinimumWidth(stepSize);
                    view.setMaxHeight(stepSize);
                    view.setMinimumHeight(stepSize);

                    mGridLayout.addView(view);

                    //Changes position
                    RelativeLayout.LayoutParams param
                            = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    param.leftMargin = (x * stepSize);
                    param.topMargin = (y * stepSize);
                    param.bottomMargin = 0;
                    param.rightMargin = 0;

                    view.setOnTouchListener(createTileMovementCode());
                }
                mGridElements[y][x] = view;
                imageIndex++;
            }
        }

        //TODO: Why in the hell does this work??
        mGridLayout.getLayoutParams().height = -5;
        mGridLayout.invalidate();
    }


    /**
     * Method that codes the listener
     */
    private OnSwipeListener createTileMovementCode() {
        return new OnSwipeListener(this) {

            //Move IDs
            private final int MOVE_UP = 0;
            private final int MOVE_RIGHT = 1;
            private final int MOVE_DOWN = 2;
            private final int MOVE_LEFT = 3;

            private Position mCurrentPosition;
            private Position mDestinationPosition;

            /**
             * Class that is used to hold X and Y values for a position. It also contains
             * functionality to move values in a grid direction
             */
            class Position {

                public Position(int pX, int pY) {
                    x = pX;
                    y = pY;
                }

                public Position Move(int pDirection) {

                    Position newPosition = new Position(x, y);
                    switch (pDirection) {

                        //Clockwise movement from top 0 (Up) -> 3(Left)
                        case MOVE_UP:
                            newPosition.y -= 1;
                            break;

                        case MOVE_RIGHT:
                            newPosition.x += 1;
                            break;

                        case MOVE_DOWN:
                            newPosition.y += 1;
                            break;

                        case MOVE_LEFT:
                            newPosition.x -= 1;
                            break;

                    }
                    return newPosition;
                }


                int x;
                int y;
            }

            /**
             * Moves the positions a set direction
             * @param moveID - The move ID that are defined by the variables above
             */
            private void setCurrentAndDestinationPositions(int moveID) {
                mCurrentPosition = getPosition(mCurrentTile);
                mDestinationPosition = mCurrentPosition.Move(moveID);
            }

            /**
             * Gets the current positons of the ImageView by using the mGridPositons array
             * @param currentTile - The ImageView being moved
             * @return - Returns a simple object containing x and y positions
             */
            private Position getPosition(View currentTile) {

                int sizeY = mGridElements.length;
                int sizeX = mGridElements[0].length;

                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        ImageView view = mGridElements[y][x];

                        if (view == currentTile) {
                            return new Position(x, y);
                        }
                    }
                }
                //Error none found
                return null;
            }

            /**
             * This method checks if the tile can move i.e. the direction would move it into a
             * blank spot. The method also checks that the move stays on the board
             * @param position - positions to move to
             * @return - Boolean if it's a valid move
             *          True - Valid move
             *          False - Invalid move
             */
            private Boolean checkIfValidMove(Position position) {

                int sizeY = mGridElements.length;
                int sizeX = mGridElements[0].length;

                //Checks if the move is on screen
                if ((position.x >= 0) && (position.y >= 0) &&
                        (position.y < sizeY) && (position.x < sizeX)) {

                    //Checks if the place to move is blank
                    if (mGridElements[position.y][position.x] == null) {
                        return true;
                    }
                }

                return false;
            }

            /**
             * Method that updates the 2D array with the new positions
             * @param pCurrentPosition - The position of the tile being moved
             * @param pDestinationPosition - The position the tile is moving to
             */
            private void moveOperation(Position pCurrentPosition, Position pDestinationPosition) {
                mGridElements[pDestinationPosition.y][pDestinationPosition.x]
                        = mGridElements[pCurrentPosition.y][pCurrentPosition.x];
                mGridElements[pCurrentPosition.y][pCurrentPosition.x] = null;
            }

            @Override
            public void OnSwipeLeft() {
                setCurrentAndDestinationPositions(MOVE_LEFT);

                //If the blank spot is to the left of the current position
                if (checkIfValidMove(mDestinationPosition)) {
                    mCurrentTile.setX(mCurrentTile.getX() - (mTileSize));
                    moveOperation(mCurrentPosition, mDestinationPosition);
                }
            }

            @Override
            public void OnSwipeRight() {
                setCurrentAndDestinationPositions(MOVE_RIGHT);

                //If the blank spot is to the left of the current position
                if (checkIfValidMove(mDestinationPosition)) {
                    mCurrentTile.setX(mCurrentTile.getX() + (mTileSize));
                    moveOperation(mCurrentPosition, mDestinationPosition);
                }
            }

            @Override
            public void OnSwipeUp() {
                setCurrentAndDestinationPositions(MOVE_UP);

                //If the blank spot is to the left of the current position
                if (checkIfValidMove(mDestinationPosition)) {
                    mCurrentTile.setY(mCurrentTile.getY() - (mTileSize));
                    moveOperation(mCurrentPosition, mDestinationPosition);
                }
            }

            @Override
            public void OnSwipeDown() {
                setCurrentAndDestinationPositions(MOVE_DOWN);

                //If the blank spot is to the left of the current position
                if (checkIfValidMove(mDestinationPosition)) {
                    mCurrentTile.setY(mCurrentTile.getY() + (mTileSize));
                    moveOperation(mCurrentPosition, mDestinationPosition);
                }

            }
        };
    }

    /**
     * Listener that links to a OnTouchListener that is setup to look for horizontal and vertical
     * swipes
     */
    class OnSwipeListener implements View.OnTouchListener {

        private final Context mContext;
        private final GestureDetector mGestureDetector;
        public View mCurrentTile;
        public int mTileSize;

        public OnSwipeListener(Context context) {
            mContext = context;
            mGestureDetector = new GestureDetector(context, new SwipeGesture());
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCurrentTile = view;
            mTileSize = view.getWidth();
            return mGestureDetector.onTouchEvent(motionEvent);
        }

        //Action methods
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
