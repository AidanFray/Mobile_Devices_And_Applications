package mobile.labs.acw.Puzzle_Class;

import java.io.Serializable;

/**
* Class that is solely used to signify a EOF when writing objects. It's just a stronger way
 * than specifying the EOF with a null that can mean other things.
 *
 * Methods are just required to write the object using ObjectWriter
* */
class EOF implements Serializable{
    private void writeObject(java.io.ObjectOutputStream out) {}
    private void readObject(java.io.ObjectInputStream in) {}
    private void readObjectNoData() {
    }
}
