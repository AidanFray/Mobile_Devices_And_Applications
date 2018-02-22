package mobile.labs.acw.Puzzle_Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.Logging;

//TODO: MetaData needs to be saved as well so puzzles can be sorted and filtered
// This can be done with a database and it will be easy to store and retrieve data

public class Puzzle {

    private final int compression_percentage = 100;

    //Member variables
    private String mName;
    private List<Row> mInitialPositions;
    private List<Row> mPuzzlesImages;
    private Bitmap mPuzzleThumbnail;
    private int mPuzzleSize;

    //Accessors
    public String getName() {
        return mName;
    }

    public List<Row> getInitialPositions() {
        return mInitialPositions;
    }

    public List<Row> getPuzzlesImages() {
        return mPuzzlesImages;
    }

    public Bitmap getPuzzleThumbnail() {
        return mPuzzleThumbnail;
    }

    public int getPuzzleSize() {
        return mPuzzleSize;
    }

    //Literals
    private final String mInitialPosFileName = "initial_positions.dat";

    //Custom constructor
    public Puzzle(String pName, List<Row> pInitialPositions, List<Row> pPuzzleImages) {
        mName = pName;
        mInitialPositions = pInitialPositions;
        mPuzzlesImages = pPuzzleImages;
    }

    //Load constructor
    public Puzzle(Context context, String pName) {
        Load(context, pName);
    }

    //Used to patch together all images into a thumbnail
    private Bitmap createThumbnail(Context context) {
        try {
            int noOfTiles = mPuzzlesImages.get(0).getElements().size();

            //Square so height and width are the same
            int length = 1;

            List<Bitmap> row = mPuzzlesImages.get(0).getElements();
            for (int i = 0; i < row.size(); i++) {

                Bitmap image = row.get(i);

                //Looks for an image in the row
                if (image != null) {
                    length *= image.getWidth();
                    break;
                }
            }

            //Creates the canvas that while house the images
            Bitmap cs = Bitmap.createBitmap(length * noOfTiles, length * noOfTiles, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(cs);

            for (int y = 0; y < mPuzzlesImages.size(); y++) {

                List<Bitmap> images = mPuzzlesImages.get(y).getElements();

                for (int x = 0; x < images.size(); x++) {
                    Bitmap bmp = images.get(x);

                    float width = x * length;
                    float height = y * length;

                    //Skips the empty square
                    if (bmp != null) {
                        canvas.drawBitmap(bmp, width, height, null);
                    } else {
                        //TODO put blank value here
                    }
                }
            }

            float max_size = length * noOfTiles;
            float stroke_weight = 15f; //Smaller value means thicker bar

            //Stops larger images from having thinner bars and dynamically creates it
            stroke_weight = max_size / (stroke_weight);

            //Adds a border
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(stroke_weight);
            paint.setAntiAlias(true);

            canvas.drawLine(0, 0, max_size, 0, paint);                      //Top line
            canvas.drawLine(0, 0, 0, max_size, paint);                      //Left line
            canvas.drawLine(max_size, 0, max_size, max_size, paint);        //Right line
            canvas.drawLine(0, max_size, max_size, max_size, paint);        //Bottom Line

            System.gc();

            return cs;
        } catch (Exception e) {
            Logging.Exception(e);
        }
        return null;
    }

    public void Save(Context context) {

        try {
            //Creates a directory with the puzzles name
            File puzzleDir = context.getDir(mName, Context.MODE_PRIVATE);
            File layoutDir = new File(puzzleDir, "Layout");
            layoutDir.mkdir();
            File imageDir = new File(puzzleDir, "Images");
            imageDir.mkdir();

            //Saves the initial positions
            SaveObject(layoutDir.getAbsolutePath() + "/" + mInitialPosFileName, mInitialPositions);

            //Saves all the images
            for (int i = 0; i < mPuzzlesImages.size(); i++) {
                Row row = mPuzzlesImages.get(i);
                Row posRow = mInitialPositions.get(i);

                for (int j = 0; j < row.getElements().size(); j++) {
                    String fileName = (String) posRow.getElements().get(j);
                    Bitmap bmp = (Bitmap) row.getElements().get(j);

                    //Saves all the images
                    if (bmp != null && !fileName.equals("empty")) {
                        String filePath = imageDir.getAbsolutePath() + "/" + fileName + ".png";
                        SaveImage(filePath, bmp);
                    }
                }
            }

            //Saves the thumbnail
            Bitmap bmp = createThumbnail(context);
            SaveImage(imageDir.getAbsolutePath() + "/" + "Thumbnail.png", bmp);
            mPuzzleThumbnail = bmp;

        } catch (Exception e) {
            Logging.Exception(e);
        }
    }

    private void SaveImage(String filePath, Bitmap image) {

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(filePath);
            image.compress(Bitmap.CompressFormat.PNG, compression_percentage, stream);
            stream.close();

        } catch (IOException e) {
            Logging.Exception(e);
        }
    }

    private void SaveObject(String filePath, Object object) {

        try {
            File file = new File(filePath);
            ObjectOutputStream objStream =
                    new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
            objStream.writeObject(object);
            objStream.close();
        } catch (IOException e) {
            Logging.Exception(e);
        }

    }

    private void Load(Context context, String pPuzzleName) {

        mName = pPuzzleName;
        File puzzleDir = context.getDir(mName, Context.MODE_PRIVATE);
        File layoutDir = new File(puzzleDir, "Layout");
        File imageDir = new File(puzzleDir, "Images");

        try {
            //Grabs the initial positions as an object
            mInitialPositions = (List<Row>) LoadObject(layoutDir.getAbsolutePath() + "/" + mInitialPosFileName);

            //Loops round the positions to grab the images
            mPuzzlesImages = new ArrayList<>();
            for (int i = 0; i < mInitialPositions.size(); i++) {

                Row row = mInitialPositions.get(i);
                Row<Bitmap> imageRow = new Row<>();

                //Loops round each element in the rows
                for (int j = 0; j < row.getElements().size(); j++) {

                    String rowValue = (String) row.getElements().get(j);

                    if (!rowValue.equals("empty")) {
                        String filePath = imageDir.getAbsolutePath() + "/" + rowValue + ".png";
                        imageRow.add(LoadImage(filePath));
                    } else {
                        imageRow.add(null);
                    }
                }
                mPuzzlesImages.add(imageRow);
            }

            //Loads the thumbnail
            mPuzzleThumbnail = LoadImage(imageDir.getAbsolutePath() + "/Thumbnail.png");

        } catch (Exception e) {
            Logging.Exception(e);
        }

        //Grabs the size of the puzzle
        mPuzzleSize =  mPuzzlesImages.get(0).getElements().size();
    }

    private Bitmap LoadImage(String filePath) {

        try {
            FileInputStream fileStream = new FileInputStream(filePath);
            return BitmapFactory.decodeStream(fileStream);
        } catch (Exception e) {
            Logging.Exception(e);
        }
        return null;
    }

    private Object LoadObject(String filePath) {
        try {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(filePath));
                Object obj = objectInputStream.readObject();
                objectInputStream.close();
                return obj;
            } catch (Exception e) {
                Logging.Exception(e);
            } finally {
                objectInputStream.close();
            }
        } catch (IOException e) {
            Logging.Exception(e);
        }
        return null;
    }
}
