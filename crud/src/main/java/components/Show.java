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

/**
 * The class {@code Show} represents a show register.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link components.interfaces.Register}
 * @see {@link components.interfaces.DateFormatter}
 */
public class Show implements Register<Show>, DateFormatter {

   // Constants

   public int MAX_REGISTER_SIZE = 500;

   /**
    * Map that corelates a property name with it`s comparator.
    */
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

   private int showId = -1; // Auto-incremented ID
   private String type; // Movie or TV Show
   private String title; // Show title
   private String directors; // Show directors
   private Date dateAdded; // Date added to Netflix
   private short releaseYear; // Show release year
   private String duration; // Show duration
   private String listedIn; // Show categories
   private String description; // Show description

   // Constructors

   /**
    * Creates a new Show object with default values.
    */
   public Show() {
      this("", "", "", new Date(), (short)-1, "", "", "");
   }

   /**
    * Creates a new Show object with the given values.
    * @param type show type
    * @param title show title
    * @param directors show directors
    * @param dateAdded date added to Netflix
    * @param releaseYear show release year
    * @param duration show duration
    * @param listedIn show categories
    * @param description show description
    */
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

   /**
    * Returns the show id.
    * @return the show id.
    */
   @Override
   public int getId() {
      return showId;
   }

   /**
    * Returns the show type.
    * @return the show type.
    */
   public String getType() {
      return type;
   }

   /**
    * Returns the show title.
    * @return the show title.
    */
   public String getTitle() {
      return title;
   }

   /**
    * Returns the show directors.
    * @return the show directors.
    */
   public String getDirectors() {
      return directors;
   }

   /**
    * Returns the show categories.
    * @return the show categories.
    */
   public String getListedIn() {
      return listedIn;
   }

   /**
    * Returns the date added to Netflix.
    * @return the date added to Netflix.
    */
   public Date getDateAdded() {
      return dateAdded;
   }

   /**
    * Returns the show release year.
    * @return the show release year.
    */
   public short getReleaseYear() {
      return releaseYear;
   }

   /**
    * Returns the show duration.
    * @return the show duration.
    */
   public String getDuration() {
      return duration;
   }

   /**
    * Returns the show description.
    * @return the show description.
    */
   public String getDescription() {
      return description;
   }

   // Setters

   /**
    * Sets the show id.
    * @param showId the show id.
    */
   @Override
   public void setId(int showId) {
      this.showId = showId;
   }

   /**
    * Sets the show type.
    * @param type the show type.
    */
   public void setType(String type) {
      this.type = type;
   }

   /**
    * Sets the show title.
    * @param title the show title.
    */
   public void setTitle(String title) {
      this.title = title;
   }

   /**
    * Sets the show directors.
    * @param directors the show directors.
    */
   public void setDirectors(String directors) {
      this.directors = directors;
   }

   /**
    * Sets the show categories.
    * @param listedIn the show categories.
    */
   public void setListedIn(String listedIn) {
      this.listedIn = listedIn;
   }

   /**
    * Sets the date added to Netflix.
    * @param dateAdded the date added to Netflix.
    */
   public void setDateAdded(Date dateAdded) {
      this.dateAdded = dateAdded;
   }

   /**
    * Sets the show release year.
    * @param releaseYear the show release year.
    */
   public void setReleaseYear(short releaseYear) {
      this.releaseYear = releaseYear;
   }

   /**
    * Sets the show duration.
    * @param duration the show duration.
    */
   public void setDuration(String duration) {
      this.duration = duration;
   }

   /**
    * Sets the show description.
    * @param description the show description.
    */
   public void setDescription(String description) {
      this.description = description;
   }

   // Methods

   /**
    * Populates the Show object with the given array values.
    * @param arr array of values to populate the Show object
    */
   @Override
   public void from(String... arr) {
      this.showId = Integer.parseInt(arr[0].substring(1));
      this.type = arr[1];
      this.title = arr[2];
      this.directors = arr[3];
      this.dateAdded = this.dateParser(arr[4]);
      this.releaseYear = (short)Integer.parseInt(arr[5]);
      this.duration = arr[6];
      this.listedIn = arr[7];
      this.description = arr[8];
   }

   /**
     * Returns a {@link Date} object from a given date string.
     * @param date the date string.
     * @return a {@link Date} object from a given date string.
     */
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

   /**
    * Returns the register as an array of {@code bytes}.
    * @return the register as an array of {@code bytes}.
    */
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
      return baos.toByteArray();
   }

   /**
    * Populates a register with the given array of {@code bytes}.
    * @param b array of {@code bytes} to populate the register.
    */
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
   }

   /**
    * Returns a JSON-formatted string representation of the Show object.
    * @return a JSON-formatted string representation of the Show object
    */
   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer("{\n");
      sb.append("\t" + "\"showId\": " + "\"" + this.getId() + "\"" + ",\n");
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

   /**
    * Sets the object especific attribute with the given value.
    * @param key the attribute name
    * @param value the attribute value
    */
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

   /**
    * Determines if this Show object is equal to the specified object.
    * @param o the object to compare
    * @return true if the objects are equal, false otherwise
    */
   @Override 
   public boolean equals(Object o) {
      return  this.getId() == ((Show)o).getId();
   }

   /**
    * Compare a Object with the specified show attribute.
    * @param key the show attribute to compare
    * @param obj the object to compare
    */
   @Override 
   public int compare(String key, Object obj) {
      switch(key) {
         case "id": return ((Integer)this.getId()).compareTo((Integer)obj);
         case "type": return this.getType().compareTo((String)obj);
         case "title": return this.getTitle().compareTo((String)obj);
         case "directors": return this.getDirectors().compareTo((String)obj);
         case "dateAdded": return this.getDateAdded().compareTo((Date)obj);
         case "releaseYear": return ((Short)this.getReleaseYear()).compareTo((Short)obj);
         case "duration": return this.getDuration().compareTo((String)obj);
         case "listedIn": return this.getListedIn().compareTo((String)obj);
         case "description": return this.getDescription().compareTo((String)obj);
         default: throw new IllegalArgumentException("Invalid key: " + key + " for Show class.");	
      }
   }

   /**
    * Returns a clone of this Show object.
    * @return a clone of this Show object
    */
   @Override 
   public Show clone() {
      Show show = new Show(this.getType(), this.getTitle(), this.getDirectors(), this.getDateAdded(), this.getReleaseYear(), this.getDuration(), this.getListedIn(), this.getDescription());
      show.setId(this.getId());
      return show;
   }
}
