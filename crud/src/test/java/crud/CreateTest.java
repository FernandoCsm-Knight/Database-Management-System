package crud;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import components.Show;
import crud.interfaces.ShowInstance;

public class CreateTest implements ShowInstance {

   public CreateTest() throws Exception {}

   @Test
   public void testShowIsCreateCorrectly() throws Exception {
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
   public void testShowsIDPattern() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();
      
      Show nextShow = new Show("Movie", "InuYasha the Movie 4: Fire on the Mystic Island", "Toshiya Shinohara", formatter.parse("15/09/2021"), (short)2004, "88 min","Action & Adventure, Anime Features, International Movies", "Ai, a young half-demon who has escaped from Horai Island to try to help her people, returns with potential saviors InuYasha, Sesshomaru and Kikyo.");
      
      nextShow.setId(10);
      
      crud.create(show);
      crud.create(nextShow);

      Show s = crud.read();
      Show showClone = crud.read();

      assertTrue(show.getId() != s.getId() && nextShow.getId() == showClone.getId());
   }

}
