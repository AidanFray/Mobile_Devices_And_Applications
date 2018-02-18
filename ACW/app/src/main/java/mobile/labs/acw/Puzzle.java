package mobile.labs.acw;

import java.util.List;

public class Puzzle {
    public String mName;
    public List<Row> mInitalPositions;
    public List<Row> mPuzzlesImages;

    public Puzzle(String pName, List<Row> pInitialPositions, List<Row> pPuzzleImages) {
        mName = pName;
        mInitalPositions = pInitialPositions;
        mPuzzlesImages = pPuzzleImages;
    }
}