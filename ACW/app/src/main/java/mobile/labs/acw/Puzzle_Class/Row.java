package mobile.labs.acw.Puzzle_Class;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.ExceptionHandling.Logging;

//Class used to hold the information on the puzzle layout
//It supports string and images
public class Row<T> implements Serializable{

    private List<T> mElements = new ArrayList<>();
    public List<T> getElements() {
        return mElements;
    }

    public Row(T... args) {

        //Dynamically adds values so the rows can be dynamically sized
        for (int i = 0; i < args.length; i++) {
            mElements.add(args[i]);
        }
    }

    public void add(T value) {
        mElements.add(value);
    }

    //Methods that allow the object to be saved and loaded using Object Reader/Writer
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
    private void readObjectNoData() {

    }
}



