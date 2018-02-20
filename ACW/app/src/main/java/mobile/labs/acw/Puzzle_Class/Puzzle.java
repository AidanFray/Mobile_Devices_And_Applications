package mobile.labs.acw.Puzzle_Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Puzzle {
    public String mName;
    public List<Row> mInitialPositions;
    public List<Row> mPuzzlesImages;
    public Bitmap mPuzzleThumbnail;

    private final String mInitialPosFileName = "initial_positions.dat";
    private final String mImagesFileName = "imagesList.dat";

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
    private Bitmap createThumbnail() {

        try {
            int noOfTiles = mPuzzlesImages.get(0).mElements.size();

            //Square so height and width are the same
            int length = 1;

            List<Bitmap> row = mPuzzlesImages.get(0).mElements;
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

                List<Bitmap> images = mPuzzlesImages.get(y).mElements;

                for (int x = 0; x < images.size(); x++) {
                    Bitmap bmp = images.get(x);

                    //Skips the empty square
                    if (bmp != null) {
                        float width = x * length;
                        float height = y * length;

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

            canvas.drawLine(0, 0, max_size, 0, paint);                      //Top line
            canvas.drawLine(0, 0, 0, max_size, paint);                      //Left line
            canvas.drawLine(max_size, 0, max_size, max_size, paint);        //Right line
            canvas.drawLine(0, max_size, max_size, max_size, paint);        //Bottom Line

            return cs;
        } catch (Exception e) {
            Log.i("CONSOLE", "Error [Puzzle.createThumbnail]:" + e.getMessage());
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

                for (int j = 0; j < row.mElements.size(); j++) {
                    String fileName = (String) posRow.mElements.get(j);
                    Bitmap bmp = (Bitmap) row.mElements.get(j);

                    //Saves all the images
                    if (bmp != null && !fileName.equals("empty")) {
                        String filePath = imageDir.getAbsolutePath() + "/" + fileName + ".png";
                        SaveImage(filePath, bmp);
                    }
                }
            }

            //Saves the thumbnail
            Bitmap bmp = createThumbnail();
            SaveImage(imageDir.getAbsolutePath() + "/" + "Thumbnail.png", bmp);
            mPuzzleThumbnail = bmp;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void SaveImage(String filePath, Bitmap image) {

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(filePath);
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            Log.i("CONSOLE", "Error: Puzzle.createThumbnail" + e);
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
            Log.i("CONSOLE", "Error[Puzzle.SaveObject]: " + e.getMessage());
        }

    }

    private void Load(Context context, String pPuzzleName) {

        mName = pPuzzleName;
        File puzzleDir = context.getDir(mName, Context.MODE_PRIVATE);
        File layoutDir = new File(puzzleDir, "Layout");
        File imageDir = new File(puzzleDir, "Images");

        try {
            //Grabs the initial positions as an object
            mInitialPositions = (List<Row>)LoadObject(layoutDir.getAbsolutePath() + "/" + mInitialPosFileName);

            //Loops round the positions to grab the images
            mPuzzlesImages = new ArrayList<>();
            for (int i = 0; i < mInitialPositions.size(); i++) {

                Row row = mInitialPositions.get(i);
                Row<Bitmap> imageRow = new Row<>();

                //Loops round each element in the rows
                for (int j = 0; j < row.mElements.size(); j++) {

                    String rowValue = (String) row.mElements.get(j);

                    if (!rowValue.equals("empty")) {
                        String filePath = imageDir.getAbsolutePath() + "/" + rowValue + ".png";
                        imageRow.Add(LoadImage(filePath));
                    } else {
                        imageRow.Add(null);
                    }
                }
                mPuzzlesImages.add(imageRow);
            }

            //Loads the thumbnail
            mPuzzleThumbnail = LoadImage(imageDir.getAbsolutePath() + "/Thumbnail.png");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Bitmap LoadImage(String filePath) {

        try {
            FileInputStream fileStream = new FileInputStream(filePath);
            return BitmapFactory.decodeStream(fileStream);
        } catch (Exception e) {
            Log.i("CONSOLE", "Error [Puzzle.LoadImage]: " + e.getMessage());
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
                Log.i("CONSOLE", "Error [Puzzle.LoadObject]: " + e.getMessage());
            } finally {
                objectInputStream.close();
            }
        } catch (IOException e) {
            Log.i("CONSOLE", "Error [Puzzle.LoadObject]: " + e.getMessage());
        }
        return null;
    }
}
