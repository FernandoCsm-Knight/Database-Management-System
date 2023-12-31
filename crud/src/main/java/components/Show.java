/**
 * A class representing a Show object that extends the Register class
 * and implements the DateFormatter interface.
 * 
 * @see components.interfaces.DateFormatter
 * @see components.interfaces.Register
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

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
 * A class representing a Show object that extends the Register class
 * and implements the DateFormatter interface.
 */
public class Show extends Register<Show> implements DateFormatter {

   // Constants
   
   /**
    * The maximum size of the register.
    */
   public static int MAX_REGISTER_SIZE = 500;

   /**
    * A map of properties and their comparators for Show objects.
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

   private Integer id = -1; // -1 means that the id is not set
   private String type; // Movie or TV Show
   private String title; // The title of the show
   private String directors; // The directors of the show
   private Date dateAdded; // The date the show was added to Netflix
   private Short releaseYear = -1; // The release year of the show
   private String duration; // The duration of the show 
   private String listedIn; // The categories the show is listed in
   private String description; // The description of the show

   // Constructors

   /**
    * Default constructor for the Show class.
    * Initializes attributes with default values.
    */
   public Show() {
      this("", "", "", new Date(), (short)-1, "", "", "");
   }

   /**
    * Parameterized constructor for the Show class.
    * Initializes attributes with provided values.
    *
    * @param type        The type of the show.
    * @param title       The title of the show.
    * @param directors   The directors of the show.
    * @param dateAdded   The date the show was added.
    * @param releaseYear The release year of the show.
    * @param duration    The duration of the show.
    * @param listedIn    The categories the show is listed in.
    * @param description The description of the show.
    */
   public Show(String type, String title, String directors, Date dateAdded, short releaseYear, String duration, String listedIn, String description) {
      super();
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
    * Get the ID of the show.
    *
    * @return The ID of the show.
    */
   @Override
   public int getId() {
      return id;
   }

   /**
    * Get the type of the show.
    *
    * @return The type of the show.
    */
   public String getType() {
      return type;
   }

   /**
    * Get the title of the show.
    *
    * @return The title of the show.
    */
   public String getTitle() {
      return title;
   }

   /**
    * Get the directors of the show.
    *
    * @return The directors of the show.
    */
   public String getDirectors() {
      return directors;
   }

   /**
    * Get the categories in which the show is listed.
    *
    * @return The categories in which the show is listed.
    */
   public String getListedIn() {
      return listedIn;
   }

   /**
    * Get the date when the show was added.
    *
    * @return The date when the show was added.
    */
   public Date getDateAdded() {
      return dateAdded;
   }

   /**
    * Get the release year of the show.
    *
    * @return The release year of the show.
    */
   public short getReleaseYear() {
      return releaseYear;
   }

   /**
    * Get the duration of the show.
    *
    * @return The duration of the show.
    */
   public String getDuration() {
      return duration;
   }

   /**
    * Get the description of the show.
    *
    * @return The description of the show.
    */
   public String getDescription() {
      return description;
   }

   /**
    * Get the value associated with the specified key.
    *
    * @param key The key to look up.
    * @return The value associated with the key.
    * @throws IllegalArgumentException If an invalid key is provided.
    */
   @Override
   public Object get(String key) {
      switch(key) {
         case "id": return this.getId();
         case "type": return this.getType();
         case "title": return this.getTitle();
         case "directors": return this.getDirectors();
         case "dateAdded": return this.getDateAdded();
         case "releaseYear": return this.getReleaseYear();
         case "duration": return this.getDuration();
         case "listedIn": return this.getListedIn();
         case "description": return this.getDescription();
         default: throw new IllegalArgumentException("Invalid key: " + key);
      }
   }

   /**
    * Get a map of properties with their corresponding comparators for Show objects.
    *
    * @return A map of properties and their comparators.
    */
   @Override
   public Map<String, Comparator<Show>> getProperties() {
      return properties;
   }

   /**
    * Get the attribute used for B+ tree indexing.
    *
    * @return The attribute used for B+ tree indexing (in this case, "id").
    */
   @Override
   public String getBPlusTreeAttribute() {
      return "id";
   }

   /**
    * Get the attribute used for extensible hash indexing.
    *
    * @return The attribute used for extensible hash indexing (in this case, "id").
    */
   @Override
   public String getExtensibleHashAttribute() {
      return "id";
   }

   /**
    * Get an array of attributes used for inverted indexing.
    *
    * @return An array of attributes used for inverted indexing (in this case, "title", "type", "duration").
    */
   @Override
   public String[] getInvertedIndexAttributes() {
      return new String[] {"title", "type", "duration"};
   }

   // Setters

   /**
    * Set the ID of the show.
    *
    * @param id The ID to set.
    */
   @Override
   public void setId(int id) {
      this.id = id;
   }

   /**
    * Set the type of the show.
    *
    * @param type The type to set.
    */
   public void setType(String type) {
      this.type = type;
   }

   /**
    * Set the title of the show.
    *
    * @param title The title to set.
    */
   public void setTitle(String title) {
      this.title = title;
   }

   /**
    * Set the directors of the show.
    *
    * @param directors The directors to set.
    */
   public void setDirectors(String directors) {
      this.directors = directors;
   }

   /**
    * Set the categories in which the show is listed.
    *
    * @param listedIn The categories to set.
    */
   public void setListedIn(String listedIn) {
      this.listedIn = listedIn;
   }

   /**
    * Set the date when the show was added.
    *
    * @param dateAdded The date when the show was added.
    */
   public void setDateAdded(Date dateAdded) {
      this.dateAdded = dateAdded;
   }

   /**
    * Set the release year of the show.
    *
    * @param releaseYear The release year to set.
    */
   public void setReleaseYear(short releaseYear) {
      this.releaseYear = releaseYear;
   }

   /**
    * Set the duration of the show.
    *
    * @param duration The duration to set.
    */
   public void setDuration(String duration) {
      this.duration = duration;
   }

   /**
    * Set the description of the show.
    *
    * @param description The description to set.
    */
   public void setDescription(String description) {
      this.description = description;
   }

   // Methods

   /**
    * Populate the Show object from an array of strings.
    *
    * @param arr An array of strings containing the show's attributes.
    */
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

   /**
    * Parse a string representation of a date into a Date object.
    *
    * @param originalDate The original date string to parse.
    * @return The parsed Date object.
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
    * Convert the Show object to a byte array.
    *
    * @return A byte array representing the serialized Show object.
    * @throws IOException If an I/O error occurs during serialization.
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

      byte[] b = baos.toByteArray();
      dos.close();
      baos.close();
      
      return b;
   }

   /**
    * Populate the Show object from a byte array.
    *
    * @param b A byte array representing the serialized Show object.
    * @throws IOException If an I/O error occurs during deserialization.
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

      dis.close();
      bais.close();
   }

   /**
    * Convert the Show object to a JSON string representation.
    *
    * @return A JSON string representing the Show object.
    */
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

   /**
    * Set the value of an attribute in the Show object using a key.
    *
    * @param key The key corresponding to the attribute to set.
    * @param value The value to set for the attribute.
    */
   @Override
   public void set(String key, Object value) {
      switch(key) {
         case "id": this.setId((int)value); break;
         case "type": this.setType((String)value); break;
         case "title": this.setTitle(this.rsa.encrypt((String)value)); break;
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
    * Encrypt the Show object.
    * 
    * @return A new Show object with encrypted attributes.
    */
   @Override
   public Show encrypt() {
      Show encryptedShow = this.clone();
      encryptedShow.id = this.id; 
      encryptedShow.title = this.rsa.encrypt(this.title.replaceAll(",", "")); 
      
      return encryptedShow;
   }

   /**
    * Decrypt the Show object.
    *
    * @return A new Show object with decrypted attributes.
    */
   @Override
   public Show decript() {
      Show decryptedShow = this.clone();
      decryptedShow.id = this.id; 
      decryptedShow.title = this.rsa.decrypt(this.title);
      
      return decryptedShow;
   }

   

   /**
    * Check if the Show object is equal to another object.
    *
    * @param o The object to compare.
    * @return True if the objects are equal, false otherwise.
    */
   @Override 
   public boolean equals(Object o) {
      return  this.getId() == ((Show)o).getId();
   }

   /**
    * Create a clone of the Show object.
    *
    * @return A new Show object that is a clone of the current object.
    */
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
