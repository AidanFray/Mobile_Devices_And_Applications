package mobile.labs.acw;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mobile.labs.acw.ExceptionHandling.Logging;
import mobile.labs.acw.Puzzle_Class.Puzzle;
import mobile.labs.acw.Puzzle_Class.Row;

public class PuzzleGridFragment extends Fragment {

    //The max score the puzzle can have
    private int MAX_SCORE = 100000;

    //Puzzle State
    Bitmap[][] mImages;
    ImageView[][] mGridElements;
    String[][] mCurrentLayout;
    String[][] mWinningLayout;
    Puzzle mCurrentPuzzle;

    float tileWidth;

    float mPadding;
    int mTileSizeX;
    int mTileSizeY;

    //Controls
    private RelativeLayout mGridLayout;

    //Scoring
    int mNumberOfMoves;
    long mStartTime;
    double mPreviousTimeBeforePause;

    private static boolean mTileCurrentlyMoving = false;

    //Time update
    private int TIME_REFRESH_PERIOD = 100; //ms
    private Thread mCurrentTimeUpdateThread;
    boolean mTimeStarted = false;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void ResetPuzzle();

        void UpdateTime(double pValue);

        void UpdateScore(double pValue);

        void ResetTimeAndScore();
    }

    public PuzzleGridFragment() {
        // Required empty public constructor
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_puzzle_grid, container, false);

        mGridLayout = (RelativeLayout) view.findViewById(R.id.gridLayout);

        mGridLayout.post(new Runnable() {
            @Override
            public void run() {

                mGridLayout.getLayoutParams().height = mGridLayout.getWidth();
                mPadding = (getResources().getDimension(R.dimen.gridCustomBorder) * 2);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Resets the encasing Activity
     */
    private void Reset() {
        stopTimeUpdate();
        mListener.ResetPuzzle();
        mTileCurrentlyMoving = false;
        mPreviousTimeBeforePause = 0;
    }

    /**
     * Method called from the encasing Activity when a puzzle is selected from the spinner
     *
     * @param pView - The spinner view that was clicked
     */
    public void onPuzzleSelection(View pView) {
        mListener.ResetTimeAndScore();

        TextView textView = (TextView) pView;
        String textViewContent = textView.getText().toString();

        mGridLayout.removeAllViews();

        boolean samePuzzle = false;
        if (mCurrentPuzzle != null) {
            samePuzzle = textViewContent.equals(mCurrentPuzzle.getName());
        }

        //Stops creating the view if it's the default menu or it was the same puzzle as before
        if (!textViewContent.equals(getString(R.string.NoPuzzleSelected)) &&
                (!samePuzzle)) {
            String puzzleName = textViewContent;

            //Loads the puzzle
            mCurrentPuzzle = new Puzzle(getContext(), puzzleName);

            int puzzleSizeX = mCurrentPuzzle.getPuzzleSizeX();
            int puzzleSizeY = mCurrentPuzzle.getPuzzleSizeY();

            //Puts the image into a linear list
            mImages = new Bitmap[puzzleSizeY][puzzleSizeX];

            for (int y = 0; y < mCurrentPuzzle.getPuzzlesImages().size(); y++) {
                Row row = mCurrentPuzzle.getPuzzlesImages().get(y);
                for (int x = 0; x < row.getElements().size(); x++) {
                    mImages[y][x] = (Bitmap) row.getElements().get(x);
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
            mCurrentPuzzle.UpdateTimesPlayed(getContext());

            generateGrid(puzzleSizeX, puzzleSizeY, mImages);

            //Reset of scoring
            mStartTime = System.nanoTime();
            mNumberOfMoves = 0;
        } else {
            if (samePuzzle) {
                reloadGrid();
            }
        }
    }

    /**
     * Method that generates a grid of a specific size. Each element is an image view
     *
     * @param sizeX  - Number of tiles in the X axis
     * @param sizeY  - Number of tiles in the y axis
     * @param images - Image list
     */
    private void generateGrid(int sizeX, int sizeY, Bitmap[][] images) {

        generateWinningLayout(sizeX, sizeY);

        //Re-sizes the grids height
        mGridElements = new ImageView[sizeY][sizeX];

        //Grabs the dimensions of each grid
        mTileSizeX = ((mGridLayout.getWidth() - (int) mPadding) / sizeX);
        mTileSizeY = ((mGridLayout.getHeight() - (int) mPadding) / sizeY);

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {

                ImageView view = null;
                Bitmap bmp = images[y][x];

                if (bmp != null) {
                    view = new ImageView(getContext());
                    view.setImageBitmap(bmp);

                    view.setMaxWidth(mTileSizeX);
                    view.setMinimumWidth(mTileSizeX);

                    view.setMaxHeight(mTileSizeY);
                    view.setMinimumHeight(mTileSizeY);

                    view.setAdjustViewBounds(false);
                    view.setScaleType(ImageView.ScaleType.FIT_XY);

                    mGridLayout.addView(view);

                    //Changes position
                    RelativeLayout.LayoutParams param
                            = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    param.leftMargin = (x * mTileSizeX);
                    param.topMargin = (y * mTileSizeY);
                    param.bottomMargin = 0;
                    param.rightMargin = 0;

                    view.setOnTouchListener(createTileMovementCode());
                }
                mGridElements[y][x] = view;
            }
        }

        //Puts the height into a sort of dynamic wrap content mode
        //mGridLayout.getLayoutParams().height = -5;
        mGridLayout.invalidate();
    }

    /**
     * Recreates the grid with the values already stored in the fragment
     */
    private void reloadGrid() {
        generateGrid(mCurrentPuzzle.getPuzzleSizeX(), mCurrentPuzzle.getPuzzleSizeY(), mImages);
    }

    /**
     * Generates the winning layout dynamically for a grid
     *
     * @param sizeX - Size of the x side of the puzzle
     * @param sizeY - Size of the y side of the puzzle
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
     * Runs when the winning conditions have been matched for the puzzle
     */
    private void puzzleComplete() {

        stopTimeUpdate();

        double timeElasped = calculateTime();
        double score = calculateScore(timeElasped, mNumberOfMoves);
        score = Math.round(score);

        mListener.UpdateTime(timeElasped);
        mListener.UpdateScore(score);

        //Updates the times completed
        mCurrentPuzzle.UpdateTimesCompleted(getContext());

        int previous_highscore = Integer.parseInt(mCurrentPuzzle.getHighscore(getContext()));
        mCurrentPuzzle.UpdateHighscore(getContext(), (int) score);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle(R.string.puzzleWin_Title);

        //Menu items
        String highScoreString = String.format(getString(R.string.puzzleWin_Score), String.valueOf(score));
        String timeString = String.format(getString(R.string.puzzleWin_TimeTaken), timeElasped);
        String moveString = String.format(getString(R.string.puzzleWin_MovesTaken), String.valueOf(mNumberOfMoves));

        String[] scoreArray = new String[]{
                highScoreString,
                timeString,
                moveString};

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Reset();
            }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Reset();
            }
        });

        alert.setItems(scoreArray, null).show();
    }

    /**
     * TODO
     */
    private void startTimeUpdate() {

        //Stops the previous thread
        if (mCurrentTimeUpdateThread != null) {
            mCurrentTimeUpdateThread.interrupt();
        }

        Thread t = new Thread() {
            @Override
            public void run() {

                try {
                    while (!isInterrupted()) {
                        Thread.sleep(TIME_REFRESH_PERIOD);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    double time = calculateTime();

                                    mListener.UpdateTime(time);
                                    mListener.UpdateScore(calculateScore(time, mNumberOfMoves));
                                }
                            });
                        }

                    }
                } catch (InterruptedException e) {
                    Logging.Exception(e);
                }
            }
        };
        mCurrentTimeUpdateThread = t;
        t.start();
    }

    /**
     * TODO
     */
    private void stopTimeUpdate() {
        if (mCurrentTimeUpdateThread != null) {
            mCurrentTimeUpdateThread.interrupt();
            mTimeStarted = false;
            mListener.ResetTimeAndScore();
        }
    }

    /**
     * Method that calculates the score for the puzzle. The score is calculated by taking
     * a small percentage of the MAX_SCORE after either a whole second has passed or a move has
     * been performed
     *
     * @return - The calculated score
     */
    private double calculateScore(double pTimeTaken, int pMovesPerformed) {
        //A percentage reduction of the score
        int deduction = MAX_SCORE / 500;
        double score = MAX_SCORE - ((pTimeTaken * deduction) + (pMovesPerformed * deduction));

        //Check to make sure you can't have a negative score
        if (score < 0) {
            score = 0;
        }

        return score;
    }

    /**
     * TODO
     */
    private float calculateTime() {
        long elapsedTime = (System.nanoTime() - mStartTime);
        float seccondsPassed = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) / 1000f;
        return seccondsPassed;
    }

    /**
     * Method that codes the listener
     */
    private OnSwipeListener createTileMovementCode() {
        return new OnSwipeListener(getContext()) {

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

                //Starts it when a tile moves
                if (!mTimeStarted) {
                    startTimeUpdate();
                }

                //Saves the view in this instance of this method for the
                // OnAnimationEnd to reference
                final View tile = pTile;

                Position currentPosition = getPosition(pTile);
                Position destinationPosition = currentPosition.Move(pDirectionID);

                //If the blank spot is to the left of the current position
                if (checkIfValidMove(destinationPosition)) {

                    mNumberOfMoves++;

                    mDeltaX = (destinationPosition.x - currentPosition.x) * mTileSizeX;
                    mDeltaY = (destinationPosition.y - currentPosition.y) * mTileSizeY;

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
             * Checks the current state against the winning state to see if they match
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

                mImages[Dy][Dx] = mImages[Cy][Cx];
                mImages[Cy][Cx] = null;
            }

            @Override
            public void OnSwipeLeft() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_LEFT, getCurrentTile());
                }
            }

            @Override
            public void OnSwipeRight() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_RIGHT, getCurrentTile());
                }
            }

            @Override
            public void OnSwipeUp() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_UP, getCurrentTile());
                }
            }

            @Override
            public void OnSwipeDown() {
                if (!mTileCurrentlyMoving) {
                    Movement(MOVE_DOWN, getCurrentTile());
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

        private View mCurrentTile;

        public View getCurrentTile() {
            return mCurrentTile;
        }

        //Movement speeds
        public float mDeltaX;
        public float mDeltaY;

        public OnSwipeListener(Context context) {
            mContext = context;
            mGestureDetector = new GestureDetector(context, new SwipeGesture());
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCurrentTile = view;
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
