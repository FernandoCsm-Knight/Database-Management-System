package ShowTests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import components.Show;
import crud.interfaces.ShowInstance;

public class ShowTest implements ShowInstance {
   
   @Test
   public void testShowFromStatement() throws Exception {
      Show show1 = new Show();

      show1.from("s1","Movie", "Class Rank", "Eric Stoltz", "September 18, 2018", "2018", "103 min", "Comedies", "When her class rank threatens her college plans, an ambitious teen convinces a nerdy peer to run for the school board to abolish the ranking system.");

      boolean value = show1.getId() == 1;
      value &= show1.getTitle().equals("Class Rank");
      value &= show1.getType().equals("Movie");
      value &= show1.getDirectors().equals("Eric Stoltz");
      value &= show1.getDateAdded().equals(formatter.parse("18/09/2018"));
      value &= show1.getReleaseYear() == 2018;
      value &= show1.getDuration().equals("103 min");
      value &= show1.getListedIn().equals("Comedies");
      value &= show1.getDescription().equals("When her class rank threatens her college plans, an ambitious teen convinces a nerdy peer to run for the school board to abolish the ranking system.");

      assertTrue(value);
   }

   @Test
   public void testShowFromByteArray() throws IOException {
      Show s = new Show();
      s.fromByteArray(show.toByteArray());

      boolean value = s.getId() == -1;
      value &= s.getTitle().equals("InuYasha the Movie 4");
      value &= s.getType().equals("Movie");
      value &= s.getDirectors().equals("Toshiya Shinohara");
      value &= s.getDateAdded().equals(new Date(1631674800000l));
      value &= s.getReleaseYear() == 2004;
      value &= s.getDuration().equals("88 min");
      value &= s.getListedIn().equals("Action & Adventure, Anime Features, International Movies");
      value &= s.getDescription().equals("Ai, a young half-demon who has escaped from Horai Island to try to help her people, returns with potential saviors InuYasha, Sesshomaru and Kikyo.");

      assertTrue(value);
   }

   @Test
   public void testShowToByteArray() throws IOException {
      byte[] arr = show.toByteArray();

      assertTrue(arr != null);
   }

   @Test
   public void testShowSetStatement() {
      Show s = show.clone();
      s.set("title", "The Last Samurai");

      assertTrue(s.getTitle().equals("The Last Samurai"));
   }

   @Test 
   public void testShowCompare() {
      Show s = show.clone();
      s.set("title", "The Last Samurai");

      assertTrue(s.compare("title", "A Last Samurai") > 0);
   }

}
