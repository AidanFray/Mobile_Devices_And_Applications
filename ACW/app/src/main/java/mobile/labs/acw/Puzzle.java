package mobile.labs.acw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

//TODO: Too much repeated code in Loading of images
// -- Very similar to JSON loading of images
public class Puzzle {
    public String mName;
    public List<Row> mInitialPositions;
    public List<Row> mPuzzlesImages;

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

    public Bitmap getMiddlePhoto() {


        //TODO: Does this work??
        Bitmap picture = null;

        int middleIndex = mPuzzlesImages.size() / 2;
        if (!(mPuzzlesImages.size() % 2 == 0)) {
            middleIndex++;
        }

        Row row = mPuzzlesImages.get(middleIndex);
        picture = (Bitmap)row.mElements.get(middleIndex);

        return picture;
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
            File initial_positions = new File(layoutDir.getAbsolutePath() + "/" + mInitialPosFileName);
            ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(initial_positions.getAbsolutePath()));
            objStream.writeObject(mInitialPositions);
            objStream.close();

            //Saves all the images
            for (int i = 0; i < mPuzzlesImages.size(); i++) {
                Row row = mPuzzlesImages.get(i);
                Row posRow = mInitialPositions.get(i);

                for (int j = 0; j < row.mElements.size(); j++) {
                    String fileName = (String) posRow.mElements.get(j);
                    Bitmap bmp = (Bitmap) row.mElements.get(j);

                    //Saves all the images
                    if (bmp != null && !fileName.equals("empty")) {
                        FileOutputStream stream = new FileOutputStream(imageDir.getAbsolutePath() + "/" + fileName + ".png");
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Load(Context context, String pPuzzleName) {

        mName = pPuzzleName;
        File puzzleDir = context.getDir(mName, Context.MODE_PRIVATE);
        File layoutDir = new File(puzzleDir, "Layout");
        File imageDir = new File(puzzleDir, "Images");

        File initial_positions = new File(layoutDir.getAbsolutePath() + "/" + mInitialPosFileName);

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    new FileInputStream(initial_positions.getAbsolutePath())
            );

            mInitialPositions = (List<Row>)objectInputStream.readObject();
            objectInputStream.close();

            //Loops round the positions to grab the images
            mPuzzlesImages = new ArrayList<>();
            for (int i = 0; i < mInitialPositions.size(); i++) {

                Row row = mInitialPositions.get(i);
                Row<Bitmap> imageRow = new Row<>();

                //Loops round each element in the rows
                for (int j = 0; j < row.mElements.size(); j++) {

                    String rowValue = (String)row.mElements.get(j);

                    if(!rowValue.equals("empty")) {
                        FileInputStream fileStream = new FileInputStream(imageDir.getAbsolutePath() + "/" + rowValue + ".png");
                        Bitmap bitmap = BitmapFactory.decodeStream(fileStream);
                        imageRow.Add(bitmap);
                    } else {
                        imageRow.Add(null);
                    }
                }
                mPuzzlesImages.add(imageRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}