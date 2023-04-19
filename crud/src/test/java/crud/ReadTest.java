package crud;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import components.Show;
import crud.interfaces.ShowInstance;

public class ReadTest implements ShowInstance {

   private static final String basePath = "src/main/java/data/bases/dat.csv";

   @Test
   public void testReadCorrectly() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();

      crud.create(show);
      Show s = crud.read();

      boolean value = s.getTitle().equals(show.getTitle());
      value &= s.getType().equals(show.getType());
      value &= s.getDirectors().equals(show.getDirectors());
      value &= s.getDateAdded().equals(show.getDateAdded());
      value &= s.getReleaseYear() == show.getReleaseYear();
      value &= s.getDuration().equals(show.getDuration());
      value &= s.getListedIn().equals(show.getListedIn());
      value &= s.getDescription().equals(show.getDescription());

      assertTrue(value);
   }

   @Test
   public void testReadSequential() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.populateAll(basePath);

      Show[] shows = new Show[3];

      shows[0] = crud.read();
      shows[1] = crud.read();
      shows[2] = crud.read();

      boolean value = shows[0] != null;
      for(int i = 0; value && i < shows.length - 1; i++) 
         value = shows[i].getId() < shows[i + 1].getId();

      assertTrue(value);
   }

   @Test
   public void testReadInRange() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.populateAll(basePath);

      List<Show> shows = crud.read(1, 4);
      assertTrue(shows.size() == 4);
   }

   @Test
   public void testReadSpecific() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.populateAll(basePath);

      String title = "The Starling";

      Show show = crud.read("title", title);
      boolean value = show.getTitle().equals(title);

      show = crud.read("id", 56);
      value &= show.getId() == 56;

      Date date = formatter.parse("22/09/2021");
      show = crud.read("dateAdded", date);
      value &= show.getDateAdded().equals(date);

      assertTrue(value);
   }

   @Test 
   public void testReadAll() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.populateAll(basePath);

      Show[] shows = crud.readAllObj("type", "Movie");

      boolean value = shows.length == 10;
      for(int i = 0; value && i < shows.length; i++)
         value = shows[i].getType().equals("Movie");

      assertTrue(value);
   }

   @Test 
   public void testReadNonExistentRegister() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.populateAll(basePath);

      Show show = crud.read("id", 1000);
      assertTrue(show == null);
   }

}
