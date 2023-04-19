import components.Show;
import crud.CRUD;

public class Main {
    
    public static void main(String args) throws Exception {
        CRUD<Show> crud = new CRUD<>("src/main/java/data/database.db", Show.class.getConstructor());
        crud.populateAll("src/main/java/data/bases/netflix_titles.csv");
        crud.toJsonFile();
    }

}
