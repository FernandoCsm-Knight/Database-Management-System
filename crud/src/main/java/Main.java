import java.util.Arrays;

import crud.indexes.query.InvertedIndex;
import logic.SystemSpecification;

public class Main implements SystemSpecification {

    public static void main(String[] args) throws Exception {
        InvertedIndex index = new InvertedIndex("inverted.db");
        index.reset();

        index.insert("Rafael Fleury Barcellos", 1L);
        index.insert("Fernando Campos Silva Dal Maria", 10L);
        index.insert("Augusto Campos Barcellos", 100L);

        System.out.println(Arrays.toString(index.search("Fernando")));
    }

}
