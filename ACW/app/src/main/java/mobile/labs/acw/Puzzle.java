package mobile.labs.acw;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class Puzzle {
    public String mName;
    public List<Row> mInitialPositions;
    public List<Row> mPuzzlesImages;

    public Puzzle(String pName, List<Row> pInitialPositions, List<Row> pPuzzleImages) {
        mName = pName;
        mInitialPositions = pInitialPositions;
        mPuzzlesImages = pPuzzleImages;
    }

    public Puzzle(Context context, String pName) {
        Load(context, pName);
    }

    private final String mInitialPosFileName = "initial_positions.dat";
    private final String mImagesFileName = "imagesList.dat";

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
            //TODO: Images are being saved in the same file??
            File imageList = new File(imageDir.getAbsolutePath() + "/" + mImagesFileName);
            FileOutputStream stream = new FileOutputStream(imageList.getAbsolutePath());

            for (int i = 0; i < mPuzzlesImages.size(); i++) {
                Row row = mPuzzlesImages.get(i);
                for (int j = 0; j < row.mElements.size(); j++) {
                    Bitmap bmp = (Bitmap)row.mElements.get(j);

                   if (bmp != null) {
                       bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                   }

                }
            }
            stream.close();

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
        File imageList = new File(imageDir.getAbsolutePath() + "/" + mImagesFileName);

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    new FileInputStream(initial_positions.getAbsolutePath())
            );

            mInitialPositions = (List<Row>)objectInputStream.readObject();
            objectInputStream.close();


            //TODO: Load images

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}