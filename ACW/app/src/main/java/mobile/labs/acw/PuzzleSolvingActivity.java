package mobile.labs.acw;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Puzzle_Class.Puzzle;
import mobile.labs.acw.Puzzle_Class.Row;

/**
 * Activity that contains the grid of images and where the game will actually be played.
 * It will handle loading images into the grid, score calculation and the code required to play
 * the game
 */
public class PuzzleSolvingActivity extends Activity {

    //TODO: Need to generate a winning arrangement, needs to be dynamic because of varying puzzle sizes
    //      - Then after every piece movement the current state of the board needs to be checked against
    //          the winning condition
    //      - Tiles intended positions could be found out from their filenames??

    //The max score the puzzle can have
    private int MAX_SCORE = 100000;

    //Layout's views
    private RelativeLayout mGridLayout;
    private LinearLayout mMainLayout;
    private Spinner mPuzzleSpinner;

    //2D mapping the positions of the tiles
    ImageView[][] mGridElements;

    String[][] mCurrentLayout;
    String[][] mWinningLayout;

    Puzzle mCurrentPuzzle;

    //Scoring
    int mNumberOfMoves = 0;
    long mStartTime;

    private float tileWidth = 0;

    private static final String Spinner_Nothing_Selected = "Please select a puzzle";

    private static boolean mTileCurrentlyMoving = false;

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
                tileWidth = mGridLayout.getWidth() - padding;

                loadDownloadedPuzzles();
            }
        });
    }

    private void ResetActivity() {
        this.recreate();
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
        mTileCurrentlyMoving = false;

        TextView textView = (TextView) view;
        String textViewContent = textView.getText().toString();

        mGridLayout.removeAllViews();

        if (!textViewContent.equals(Spinner_Nothing_Selected)) {
            String puzzleName = textViewContent;

            //Loads the puzzle
            mCurrentPuzzle = new Puzzle(this, puzzleName);

            int puzzleSizeX = mCurrentPuzzle.getPuzzleSizeX();
            int puzzleSizeY = mCurrentPuzzle.getPuzzleSizeY();

            //Puts the image into a linear list
            List<Bitmap> imageList = new ArrayList<>();
            for (Row row : mCurrentPuzzle.getPuzzlesImages()) {
                //Gets a list of the images
                List<Bitmap> images = row.getElements();

                for (Bitmap image : images) {
                    imageList.add(image);
                }
            }

            //Axis are flipped so it's easier to visual when debugging
            mCurrentLayout = new String[puzzleSizeY][puzzleSizeX];
            List<Row> layout = mCurrentPuzzle.getInitialPositions();

            for (int y = 0; y < layout.size(); y++) {
                Row row = layout.get(y);
                for (int x = 0; x < row.getElements().size(); x++) {
                    mCurrentLayout[y][x] = (String) row.getElements().get(x);
                }
            }

            //Increments the number of times played
            mCurrentPuzzle.UpdateTimesPlayed(this);

            generateGrid(puzzleSizeX, puzzleSizeY, imageList);

            //Reset of scoring
            mStartTime = System.nanoTime();
            mNumberOfMoves = 0;
        }
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

        //Adds the nothing selected value
        downloadedPuzzles.add(Spinner_Nothing_Selected);

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

        generateWinningLayout(sizeX, sizeY);

        //Re-sizes the grids height
        mGridElements = new ImageView[sizeY][sizeX];

        //Grabs the dimensions of each grid
        int stepSize = (int) (tileWidth / sizeX);

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

        //Puts the height into a sort of dynamic wrap content mode
        mGridLayout.getLayoutParams().height = -5;
        mGridLayout.invalidate();
    }

    /**
     * TODO
     *
     * @param sizeX
     * @param sizeY
     */
    private void generateWinningLayout(int sizeX, int sizeY) {

        mWinningLayout = new String[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                mWinningLayout[y][x] = String.valueOf(x + 1) + String.valueOf(y + 1);
            }
        }
        //Top left is always empty
        mWinningLayout[0][0] = "empty";
    }

    /**
     * TODO
     */
    private void puzzleComplete() {
        long elapsedTime = (System.nanoTime() - mStartTime);

        double seccondsPassed = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) / 1000f;

        //Updates the times completed
        mCurrentPuzzle.UpdateTimesCompleted(this);

        double score = calculateScore(seccondsPassed, mNumberOfMoves);
        score = Math.round(score);

        int previous_highscore = Integer.parseInt(mCurrentPuzzle.getHighscore(this));
        mCurrentPuzzle.UpdateHighscore(this, (int) score);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.puzzleWin_Title);

        //Menu items
        String highScoreString = getString(R.string.puzzleWin_Score) + score;
        String timeString = String.format(getString(R.string.puzzleWin_TimeTaken), seccondsPassed);
        String moveString = String.format(getString(R.string.puzzleWin_MovesTaken), String.valueOf(mNumberOfMoves));

        String[] scoreArray = new String[]{
                highScoreString,
                timeString,
                moveString};

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ResetActivity();
            }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ResetActivity();
            }
        });

        alert.setItems(scoreArray, null).show();
    }

    /**
     * TODO:
     *
     * @return
     */
    private double calculateScore(double pTimeTaken, int pMovesPerformed) {
        //A percentage reduction of the score
        int deduction = MAX_SCORE / 1000;
        double score = MAX_SCORE - ((pTimeTaken * deduction) + (pMovesPerformed * deduction));

        //Check to make sure you can't have a negative score
        if (score < 0) {
            score = 0;
        }

        return score;
    }

    //------------------- Tile Movement Code -------------------------- //

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

            private final int SLIDE_SPEED = 100;

            /**
             * Class that is used to hold X and Y values for a position. It also contains
             * functionality to move values in a grid direction
             */
            class Position {

                public Position(int pX, int pY) {
                    x = pX;
                    y = pY;
                }

                /**
                 * Returns a Position after applying a move transformation
                 * @param pDirection - The intended transformation
                 * @return - The translated positon
                 */
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
             * Method that deals with all types of movement. It either moves a single tile or
             * if that tile cannot be moved straight away recursively checks the whole row to see
             * If that row can be moved
             * @param pDirectionID  - Direction of movement
             * @param pTile         - The tile to be moved
             * @return              - Returns a bool up the recursive stack to tell other instances
             *                        to move tiles
             */
            private boolean Movement(int pDirectionID, View pTile) {

                //Grabs the current and intended positions
                Position currentPosition = getPosition(pTile);
                Position destinationPosition = currentPosition.Move(pDirectionID);

                //If the tile is next to a blank spot
                if (checkIfValidMove(destinationPosition)) {
                    MoveTile(pDirectionID, pTile);

                    //reset
                    return true;
                }

                //If it's next to another tile it moves to that tile to check if that tile
                //is next to a blank spot until it reaches the end of the row
                else {
                    int sizeY = mGridElements.length;
                    int sizeX = mGridElements[0].length;

                    //Find the direction of movement
                    int deltaX = destinationPosition.x - currentPosition.x;
                    int deltaY = destinationPosition.y - currentPosition.y;

                    //Check to see if the values are at the edge of the board
                    if ((destinationPosition.y + deltaY < 0) || (destinationPosition.y + deltaY > sizeY)) {
                        return false;
                    }
                    if ((destinationPosition.x + deltaX < 0) || (destinationPosition.x + deltaX > sizeX)) {
                        return false;
                    }

                    View nextTile = mGridElements[destinationPosition.y][destinationPosition.x];

                    //Recursively follows the row or column
                    if (Movement(pDirectionID, nextTile)) {
                        MoveTile(pDirectionID, pTile);
                        return true;
                    }
                }
                return false;
            }

            /**
             * This method modes the tile in the specified direction.
             * @param pDirectionID - Specifies the direction by using of the defined variables at
             *                     the top
             * @param pTile         - The tile to be moved
             */
            private void MoveTile(int pDirectionID, View pTile) {


                //Saves the view in this instance of this method for the
                // OnAnimationEnd to reference
                final View tile = pTile;

                Position currentPosition = getPosition(pTile);
                Position destinationPosition = currentPosition.Move(pDirectionID);

                //If the blank spot is to the left of the current position
                if (checkIfValidMove(destinationPosition)) {

                    mNumberOfMoves++;

                    mDeltaX = (destinationPosition.x - currentPosition.x) * mTileSize;
                    mDeltaY = (destinationPosition.y - currentPosition.y) * mTileSize;

                    TranslateAnimation animation =
                            new TranslateAnimation(0, mDeltaX, 0, mDeltaY);

                    animation.setDuration(SLIDE_SPEED);
                    animation.setRepeatCount(0);

                    //Sets what happens when the animation ends
                    animation.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            mTileCurrentlyMoving = false;

                            //Updates the position of the grid
                            tile.setX(tile.getX() + mDeltaX);
                            tile.setY(tile.getY() + mDeltaY);

                            //Clears the animation
                            tile.startAnimation(new TranslateAnimation(0f, 0f, 0f, 0f));


                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    //Sets the bool that informs all other tiles another is moving
                    mTileCurrentlyMoving = true;
                    pTile.startAnimation(animation);
                    moveOperation(currentPosition, destinationPosition);

                    //Sees if the puzzle has been completed
                    if (checkForWin()) {
                        puzzleComplete();
                    }
                }
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
             * TODO
             */
            private Boolean checkForWin() {
                return Arrays.deepEquals(mCurrentLayout, mWinningLayout);
            }

            /**
             * Method that updates the 2D array with the new positions
             * @param pCurrentPosition - The position of the tile being moved
             * @param pDestinationPosition - The position the tile is moving to
             */
            private void moveOperation(Position pCurrentPosition, Position pDestinationPosition) {

                int Dx = pDestinationPosition.x;
                int Dy = pDestinationPosition.y;

                int Cx = pCurrentPosition.x;
                int Cy = pCurrentPosition.y;

                mGridElements[Dy][Dx] = mGridElements[Cy][Cx];
                mGridElements[Cy][Cx] = null;

                mCurrentLayout[Dy][Dx] = mCurrentLayout[Cy][Cx];
                mCurrentLayout[Cy][Cx] = "empty";
            }

            @Override
            public void OnSwipeLeft() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_LEFT, mCurrentTile);
                }
            }

            @Override
            public void OnSwipeRight() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_RIGHT, mCurrentTile);
                }
            }

            @Override
            public void OnSwipeUp() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_UP, mCurrentTile);
                }
            }

            @Override
            public void OnSwipeDown() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_DOWN, mCurrentTile);
                }
            }
        };
    }

    /**
     * Listener that links to a OnTouchListener that is setup to look for horizontal and vertical
     * swipes
     */
    class OnSwipeListener implements View.OnTouchListener {

        //TODO: Getters and setters
        private final Context mContext;
        private final GestureDetector mGestureDetector;
        public View mCurrentTile;
        public int mTileSize;

        public float mDeltaX;
        public float mDeltaY;

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

        /**
         * Gesture object used to calcualte the direction of the swipe
         */
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
