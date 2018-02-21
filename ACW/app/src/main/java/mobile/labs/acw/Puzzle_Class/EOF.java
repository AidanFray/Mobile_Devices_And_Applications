package mobile.labs.acw.Puzzle_Class;

import java.io.Serializable;

/**
* Class that is solely used to signify a EOF when writing objects. Methods are just required
 * To write the object using ObjectWriter
* */

class EOF implements Serializable{
    private void writeObject(java.io.ObjectOutputStream out) {}
    private void readObject(java.io.ObjectInputStream in) {}
    private void readObjectNoData() {

    }
}
