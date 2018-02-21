package mobile.labs.acw;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class PuzzleSolvingActivity extends Activity {

    //Layout's views
    private RelativeLayout gridLayout;
    List<ImageView> mGridElements = new ArrayList<>();

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

        gridLayout = (RelativeLayout)findViewById(R.id.gridLayout);

        gridLayout.post(new Runnable() {
            @Override
            public void run() {
                gridLayout.getLayoutParams().height = gridLayout.getWidth();
                layoutSideWidth = gridLayout.getWidth() - (getResources().getDimension(R.dimen.gridCustomBorder) * 2);
                controlSetup();
            }
        });
    }

    private void controlSetup(){
        generateGrid(5);
        loadDownloadedPuzzles();
    }

    /**
     * Method that generates a grid of a specific size. Each element is an image view
     * @param size - Specifies the grids width and height (Size X Size)
     */
    private void generateGrid(int size) {

        //Gets the total size of the grid
        float totalWidth = layoutSideWidth;

        //Grabs the dimensions of each grid
        int stepSize = (int)(totalWidth / size);

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

                gridLayout.addView(view);

                view.setOnTouchListener(createOnTouch());

                //Changes position
                RelativeLayout.LayoutParams param
                        = (RelativeLayout.LayoutParams) view.getLayoutParams();

                param.leftMargin = (int)(x * stepSize);
                param.topMargin = (int)(y * stepSize);
                param.bottomMargin = 0;
                param.rightMargin = 0;

                mGridElements.add(view);

                colour = !colour;
            }

            //Only alternates the colour is an even value
            if (size % 2 == 0) { colour = !colour;}
        }
        gridLayout.invalidate();
    }

    /**
     * Method that displays the downloaded puzzles in the spinner at the bottom of the page
     */
    private void loadDownloadedPuzzles() {
        //TODO:
    }

    /**
     * Method that returns a custom object that can be used to control the movement of the tiles
     * @return The custom OnTouchListener object
     */
    private View.OnTouchListener createOnTouch() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x =  motionEvent.getRawX() - view.getWidth() / 2;
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

                        layoutParams.leftMargin = (int)(x - deltaX);
                        layoutParams.topMargin = (int)(y - deltaY);
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

                gridLayout.invalidate();
                return true;
            }
        };
    }
}
