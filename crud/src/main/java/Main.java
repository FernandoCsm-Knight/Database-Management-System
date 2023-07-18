import java.util.Arrays;

import crud.indexes.query.InvertedIndex;
import logic.SystemSpecification;

public class Main implements SystemSpecification {
    
    public static void main(String[] args) throws Exception {
        InvertedIndex index = new InvertedIndex("inverted.db");
        index.reset();

        index.insert("Fernando Campos Silva Dal Maria", 1L);
        index.insert("Fernando Soares Augusto Nobrega", 10L);
        index.insert("Augusto Pericles Campos", 100L);
        index.insert("Henrique Silva Augusto", 1000L);
        index.insert("Fernando Campos David", 10000L);

        index.delete("Fernando Campos David", 10000L);

        System.out.println(Arrays.toString(index.search("Campos")));
        System.out.println(Arrays.toString(index.search("Fernando")));
        System.out.println(Arrays.toString(index.search("David")));
    }

}
