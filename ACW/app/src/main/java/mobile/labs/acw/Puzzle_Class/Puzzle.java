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

import mobile.labs.acw.ExceptionHandling.Logging;
import mobile.labs.acw.R;

//TODO: MetaData needs to be saved as well so puzzles can be sorted and filtered
// This can be done with a database and it will be easy to store and retrieve data

/**
 * Class that represents a downloaded puzzle. It contains all the information required to
 * use and manipulate the puzzles data
 */
public class Puzzle {

    private final int IMAGE_COMPRESSION_PERCENTAGE = 100;

    //Member variables
    private String mName;
    private List<Row> mInitialPositions;
    private List<Row> mPuzzlesImages;
    private Bitmap mPuzzleThumbnail;
    private int mPuzzleSizeX;
    private int mPuzzleSizeY;

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
    public int getPuzzleSizeX() {
        return mPuzzleSizeX;
    }
    public int getPuzzleSizeY() { return mPuzzleSizeY; }

    //Custom constructor
    public Puzzle(String pName,
                  List<Row> pInitialPositions,
                  List<Row> pPuzzleImages,
                  int pPuzzleSizeX,
                  int pPuzzleSizeY) {
        mName = pName;
        mInitialPositions = pInitialPositions;
        mPuzzlesImages = pPuzzleImages;
        mPuzzleSizeX = pPuzzleSizeX;
        mPuzzleSizeY = pPuzzleSizeY;
    }

    public Puzzle(Context pContext, String pName) {
        Load(pContext, pName);
    }

    /**
     * Method that patches all the tile photos together to form a thumbnail image. It uses a canvas
     * object a draws to that canvas object before saving it to a file
     * @param pContext - The calling context
     * @return - The resulting bitmap image
     */
    private Bitmap createThumbnail(Context pContext) {
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

    /**
     * Used to save the Puzzle to file. It saves the positions in a .dat file using the
     * ObjectOutputStream to serialize the data.
     * @param pContext - The entered context
     */
    public void Save(Context pContext) {

        try {
            //Creates a directory with the puzzles name
            File puzzleDir = pContext.getDir(mName, Context.MODE_PRIVATE);
            File layoutDir = new File(puzzleDir, "Layout");
            layoutDir.mkdir();
            File imageDir = new File(puzzleDir, "Images");
            imageDir.mkdir();

            //Saves the initial positions
            SaveObject(layoutDir.getAbsolutePath() + "/" + pContext.getString(R.string.initialPositionsFileName), mInitialPositions);

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
            Bitmap bmp = createThumbnail(pContext);
            SaveImage(imageDir.getAbsolutePath() + "/" + "Thumbnail.png", bmp);
            mPuzzleThumbnail = bmp;

        } catch (Exception e) {
            Logging.Exception(e);
        }
    }

    /**
     * Internal method that is used to save an individual image
     * @param pFilePath - Destination filepath
     * @param pImage - The bitmap object to be saved
     */
    private void SaveImage(String pFilePath, Bitmap pImage) {

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(pFilePath);
            pImage.compress(Bitmap.CompressFormat.PNG, IMAGE_COMPRESSION_PERCENTAGE, stream);
            stream.close();

        } catch (IOException e) {
            Logging.Exception(e);
        }
    }

    /**
     * Internal method that is used to save objects using the ObjectOutputStream
     * @param pFilePath - Destination file path
     * @param pObject  - The object to be serialized and saved to file
     */
    private void SaveObject(String pFilePath, Object pObject) {

        try {
            File file = new File(pFilePath);
            ObjectOutputStream objStream =
                    new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
            objStream.writeObject(pObject);
            objStream.close();
        } catch (IOException e) {
            Logging.Exception(e);
        }

    }

    /**
     * Method used to load a puzzle from file. It's an internal method that is called from a
     * special constructor
     * @param pContext - The calling context
     * @param pPuzzleName - The name of the puzzle to load
     */
    private void Load(Context pContext, String pPuzzleName) {

        mName = pPuzzleName;
        File puzzleDir = pContext.getDir(mName, Context.MODE_PRIVATE);
        File layoutDir = new File(puzzleDir, "Layout");
        File imageDir = new File(puzzleDir, "Images");

        try {
            //Grabs the initial positions as an object
            mInitialPositions = (List<Row>) LoadObject(
                    String.format("%s/%s", layoutDir.getAbsolutePath(), pContext.getString(R.string.initialPositionsFileName)));

            mPuzzleSizeY = mInitialPositions.size();
            mPuzzleSizeX = mInitialPositions.get(0).getElements().size();

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
    }

    /**
     * An internal method that is used to load an image from an entered filepath
     * @param pFilePath - Destination file path
     * @return - The retried bitmap object. Note: 'null' is returned if there is an issue
     */
    private Bitmap LoadImage(String pFilePath) {

        try {
            FileInputStream fileStream = new FileInputStream(pFilePath);
            return BitmapFactory.decodeStream(fileStream);
        } catch (Exception e) {
            Logging.Exception(e);
        }
        return null;
    }

    /**
     * Method used to load in an object using the ObjectInputStream.
     * @param pFilePath - The target file path
     * @return - The object that is retrieved. Null is return if there is an error
     */
    private Object LoadObject(String pFilePath) {
        try {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(pFilePath));
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
