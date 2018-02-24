package mobile.labs.acw.Puzzle_Class;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.ExceptionHandling.Logging;

/**
 * An object designed to represent a row of the puzzle. The mElement object will contain all the
 * values of a row
 * @param <T> - The type of object the row can hold. This means it can hold index positions,
 *           and bitmap for the corresponding index.
 *
 * Note:
 *           This class implements the "Serializable" interface to allow it to be written to
 *           file using ObjectWriter/ObjectReader.
 *           The class to perform this requires:
 *
 *              writeObject(ObjectOutputStream)
 *              readObject(ObjectInputStream)
 *              readObjectNoData()
 *
 */
public class Row<T> implements Serializable{

    private List<T> mElements = new ArrayList<>();
    public List<T> getElements() {
        return mElements;
    }

    /**
     * Dynamic constructor for creating a row in one line. It supports a dynamic amount of
     * parameters to allow this.
     * @param args
     */
    public Row(T... args) {

        //Dynamically adds values so the rows can be dynamically sized
        for (int i = 0; i < args.length; i++) {
            mElements.add(args[i]);
        }
    }

    public void add(T value) {
        mElements.add(value);
    }


    /**
     * Used when the Serializable object writes this class to file
     * @param out - The ObjectOutputStream used to save the class instance
     */
    private void writeObject(java.io.ObjectOutputStream out) {
        try{
            for (int i = 0; i < mElements.size(); i++) {
                out.writeObject(mElements.get(i));
            }

            //Stream EOF
            out.writeObject(new EOF());
            out.flush();

        } catch (IOException e) {
            Logging.Exception(e);
        }
    }

    /**
     * Used when the Serializable interface reads the class from file
     * @param in - The ObjectInputStream that is used to read in the class
     */
    private void readObject(java.io.ObjectInputStream in) {

        mElements = new ArrayList<>();

        try{
            Object val;
            do {
                val = in.readObject();

                if (val.getClass() != EOF.class) {
                    mElements.add((T)val);
                }
            } while(val.getClass() != EOF.class);


        } catch (Exception e){
            Logging.Exception(e);
        }

    }

    /**
     * Requires as part of the Serializable interface. In this class it performs no function
     */
    private void readObjectNoData() {

    }
}



