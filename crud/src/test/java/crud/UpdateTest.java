package crud;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import components.Show;
import crud.interfaces.ShowInstance;

public class UpdateTest implements ShowInstance {
   
   @Test
   public void testUpdateParameterCorrectly() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();
      crud.create(show);
      crud.update(1, "title", "The Office With Steve Carell And Rainn Wilson");
      assertTrue(crud.read("id", 1).getTitle().equals("The Office With Steve Carell And Rainn Wilson"));
   }

   @Test
   public void testUpdateObjectCorrectly() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();
      crud.create(show);
      Show s = crud.read("id", 1);
      s.setTitle("The Office With Steve Carell And Rainn Wilson");
      crud.update(1, s);
      assertTrue(crud.read("id", 1).getTitle().equals("The Office With Steve Carell And Rainn Wilson"));
   }

   @Test
   public void testUpdateAnNonExistentObj() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();
      crud.create(show);
      Show s = crud.read("id", 1);
      s.setTitle("The Office With Steve Carell And Rainn Wilson");
      assertFalse(crud.update(2, s));
   }

}
