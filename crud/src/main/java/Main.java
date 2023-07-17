import components.Show;
import crud.CRUD;
import crud.indexes.hash.ExtensibleHash;
import crud.indexes.types.HNode;
import crud.sorts.SortedFileHeap;
import logic.SystemSpecification;
import utils.helpers.WatchTime;

public class Main implements SystemSpecification {
    
    public static void main(String[] args) throws Exception {
        CRUD<Show> crud = new CRUD<Show>("src/main/java/data/database.db", Show.class.getConstructor());
        crud.populateAll("src/main/java/data/bases/netflix_titles.csv");

        SortedFileHeap<Show> sortedFile = new SortedFileHeap<Show>("src/main/java/data/database.db", 500, Show.class.getConstructor()); 

        sortedFile.setComparator(Show.properties.get("dateAdded"));
        sortedFile.sort();

        crud.toJsonFile(1);
    }

}
