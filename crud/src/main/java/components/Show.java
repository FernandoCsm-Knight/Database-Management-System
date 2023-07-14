package components;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import components.interfaces.DateFormatter;
import components.interfaces.Register;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Show extends Register<Show> implements DateFormatter {

   // Constants
   
   public int MAX_REGISTER_SIZE = 500;

   public static Map<String, Comparator<Show>> properties = new HashMap<String, Comparator<Show>>(){{
      put("id", (Show s1, Show s2) -> s1.getId() - s2.getId());
      put("type", (Show s1, Show s2) -> s1.getType().compareTo(s2.getType()));
      put("title", (Show s1, Show s2) -> s1.getTitle().compareTo(s2.getTitle()));
      put("directors", (Show s1, Show s2) -> s1.getDirectors().compareTo(s2.getDirectors()));
      put("dateAdded", (Show s1, Show s2) -> s1.getDateAdded().compareTo(s2.getDateAdded()));
      put("releaseYear", (Show s1, Show s2) -> s1.getReleaseYear() - s2.getReleaseYear());
      put("duration", (Show s1, Show s2) -> s1.getDuration().compareTo(s2.getDuration()));
      put("listedIn", (Show s1, Show s2) -> s1.getListedIn().compareTo(s2.getListedIn()));
      put("description", (Show s1, Show s2) -> s1.getDescription().compareTo(s2.getDescription()));
   }};

   // Attributes

   private Integer id = -1; 
   private String type; 
   private String title; 
   private String directors; 
   private Date dateAdded;
   private Short releaseYear = -1; 
   private String duration; 
   private String listedIn; 
   private String description; 

   // Constructors

   public Show() {
      this("", "", "", new Date(), (short)-1, "", "", "");
   }

   public Show(String type, String title, String directors, Date dateAdded, short releaseYear, String duration, String listedIn, String description) {
      this.type = type;
      this.title = title;
      this.directors = directors;
      this.listedIn = listedIn;
      this.dateAdded = dateAdded;
      this.releaseYear = releaseYear;
      this.duration = duration;
      this.description = description;
   }

   // Getters

   @Override
   public int getId() {
      return id;
   }

   public String getType() {
      return type;
   }

   public String getTitle() {
      return title;
   }

   public String getDirectors() {
      return directors;
   }

   public String getListedIn() {
      return listedIn;
   }

   public Date getDateAdded() {
      return dateAdded;
   }

   public short getReleaseYear() {
      return releaseYear;
   }

   public String getDuration() {
      return duration;
   }

   public String getDescription() {
      return description;
   }

   // Setters

   @Override
   public void setId(int id) {
      this.id = id;
   }

   public void setType(String type) {
      this.type = type;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void setDirectors(String directors) {
      this.directors = directors;
   }

   public void setListedIn(String listedIn) {
      this.listedIn = listedIn;
   }

   public void setDateAdded(Date dateAdded) {
      this.dateAdded = dateAdded;
   }

   public void setReleaseYear(short releaseYear) {
      this.releaseYear = releaseYear;
   }

   public void setDuration(String duration) {
      this.duration = duration;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   // Methods

   @Override
   public void from(String... arr) {
      this.id = Integer.parseInt(arr[0].substring(1));
      this.type = arr[1];
      this.title = arr[2];
      this.directors = arr[3];
      this.dateAdded = this.dateParser(arr[4]);
      this.releaseYear = (short)Integer.parseInt(arr[5]);
      this.duration = arr[6];
      this.listedIn = arr[7];
      this.description = arr[8];
   }

   @Override
   public Date dateParser(String originalDate) {
      String str  = null;
      Date date = null;

      originalDate = originalDate.replaceAll(",", "").trim();
      String[] arr = originalDate.split(" ");

      try {
         if(arr.length <= 1)  {
             date = new Date(System.currentTimeMillis());
         } else {
            str = "";
            str += arr[1] + "-";
            str += months.get(arr[0]) + "-";
            str += arr[2];
     
            date = dateFormat.parse(str);
         }
     } catch(Exception e) {            
         System.err.println("The given string (" + str + ") does not match the pattern.");
         e.printStackTrace();
     }

      return date;
   }

   @Override
   public byte[] toByteArray() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      
      dos.writeInt(this.getId());
      dos.writeUTF(this.getType());
      dos.writeUTF(this.getTitle());
      dos.writeUTF(this.getDirectors());
      dos.writeLong(this.getDateAdded().getTime());
      dos.writeShort(this.getReleaseYear());
      dos.writeUTF(this.getDuration());
      dos.writeUTF(this.getListedIn());
      dos.writeUTF(this.getDescription());

      byte[] b = baos.toByteArray();
      dos.close();
      baos.close();
      
      return b;
   }

   @Override
   public void fromByteArray(byte[] b) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(b);
      DataInputStream dis = new DataInputStream(bais);

      this.setId(dis.readInt());
      this.setType(dis.readUTF());
      this.setTitle(dis.readUTF());
      this.setDirectors(dis.readUTF());
      this.setDateAdded(new Date(dis.readLong()));
      this.setReleaseYear(dis.readShort());
      this.setDuration(dis.readUTF());
      this.setListedIn(dis.readUTF());
      this.setDescription(dis.readUTF());

      dis.close();
      bais.close();
   }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer("{\n");
      sb.append("\t" + "\"id\": " + "\"" + this.getId() + "\"" + ",\n");
      sb.append("\t" + "\"type\": " + "\"" + this.getType() + "\"" + ",\n");
      sb.append("\t" + "\"title\": " + "\"" + this.getTitle().replaceAll("\"", "\'") + "\"" + ",\n");
      sb.append("\t" + "\"directors\": " + "\"" + this.getDirectors().replaceAll("\"", "\'") + "\"" + ",\n");
      sb.append("\t" + "\"dateAdded\": " + "\"" + this.getDateAdded() + "\"" + ",\n");
      sb.append("\t" + "\"releaseYear\": " + "\"" + this.getReleaseYear() + "\"" + ",\n");
      sb.append("\t" + "\"duration\": " + "\"" + this.getDuration() + "\"" + ",\n");
      sb.append("\t" + "\"listedIn\": " + "\"" + this.getListedIn() + "\"" + ",\n");
      sb.append("\t" + "\"description\": " + "\"" + this.getDescription().replaceAll("\"", "\'") + "\"" + "\n");
      return sb.append("}").toString();
   }

   @Override
   public void set(String key, Object value) {
      switch(key) {
         case "id": this.setId((int)value); break;
         case "type": this.setType((String)value); break;
         case "title": this.setTitle((String)value); break;
         case "directors": this.setDirectors((String)value); break;
         case "dateAdded": this.setDateAdded((Date)value); break;
         case "releaseYear": this.setReleaseYear((short)value); break;
         case "duration": this.setDuration((String)value); break;
         case "listedIn": this.setListedIn((String)value); break;
         case "description": this.setDescription((String)value); break;
         default: throw new IllegalArgumentException("Invalid key: " + key);
      }
   }

   @Override 
   public boolean equals(Object o) {
      return  this.getId() == ((Show)o).getId();
   }

   @Override 
   public Show clone() {
      Show show = new Show(
         this.getType(), 
         this.getTitle(), 
         this.getDirectors(), 
         this.getDateAdded(), 
         this.getReleaseYear(), 
         this.getDuration(), 
         this.getListedIn(), 
         this.getDescription()
      );
      show.setId(this.getId());
      return show;
   }
}
