package crud;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import components.Show;
import crud.interfaces.ShowInstance;

public class DeleteTest implements ShowInstance {

   @Test
   public void testDeleteCorrectly() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();
      crud.create(show);
      crud.delete(1);
      assertFalse(crud.contains("id", 1));
   }

   @Test
   public void testDeleteWhenRegisterDoesntExists() throws Exception {
      CRUD<Show> crud = new CRUD<Show>("src/test/java/data/arc.db", Show.class.getConstructor());
      crud.clear();
      crud.create(show);
      assertFalse(crud.delete(2));
   }

}
