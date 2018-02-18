package mobile.labs.acw;

import java.util.ArrayList;
import java.util.List;

//Class used to hold the information on the puzzle layout
//It supports string and images
public class Row<T> {

    public List<T> mElements = new ArrayList<>();

    public Row() {
    }

    public Row(T... args) {

        //Dynamically adds values so the rows can be dynamically sized
        for (int i = 0; i < args.length; i++) {
            mElements.add(args[i]);
        }
    }

    public void Add(T value) {
        mElements.add(value);
    }
}

