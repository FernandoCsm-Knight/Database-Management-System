package crud.interfaces;

import java.text.SimpleDateFormat;
import java.util.Date;

import components.Show;

public interface ShowInstance {
   static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
   static Show show = new Show("Movie", "InuYasha the Movie 4", "Toshiya Shinohara", new Date(1631674800000l), (short)2004, "88 min","Action & Adventure, Anime Features, International Movies", "Ai, a young half-demon who has escaped from Horai Island to try to help her people, returns with potential saviors InuYasha, Sesshomaru and Kikyo.");
}
