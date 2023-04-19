package utils.csv;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

/**
 * A class that manages a CSV file.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link java.io.Closeable}
 */
public class CSVManager implements Closeable {
   private final String CSV_FILE_PATH; // The path of the CSV file.
   private final Character CSV_SEPARATOR; // The separator of the CSV file.
   private final CSVParser parser; // The parser of the CSV file.
   private final CSVReader reader; // The reader of the CSV file.
   private final String[] columns; // The columns of the CSV file.


   /**
    * Creates a new CSVManager object.
    * @param path The path of the CSV file.
    * @throws IOException If an I/O error occurs.
    * @throws CsvValidationException If the CSV file is invalid.
    */
   public CSVManager(String path) throws IOException, CsvValidationException {
      this(path, null);
   }

   /**
    * Creates a new CSVManager object.
    * @param path The path of the CSV file.
    * @param separator The separator of the CSV file.
    * @throws IOException If an I/O error occurs.
    * @throws CsvValidationException If the CSV file is invalid.
    */
   public CSVManager(String path, Character separator) throws IOException, CsvValidationException {
      if(path == null) 
         throw new NullPointerException();

      this.CSV_FILE_PATH = path;
      this.CSV_SEPARATOR = (separator == null) ? ',' : separator;

      this.parser = new CSVParserBuilder().withSeparator(this.CSV_SEPARATOR).build(); 
      this.reader = new CSVReaderBuilder(new FileReader(new File(path))).withCSVParser(this.parser).build();
      this.columns = this.readNext();
   }

   /**
    * Returns the path of the CSV file.
    * @return The path of the CSV file.
    */
   public String getFilePath() {
      return this.CSV_FILE_PATH;
   }

   /**
    * Returns the separator of the CSV file.
    * @return The separator of the CSV file.
    */
   public Character getFileSeparator() {
      return this.CSV_SEPARATOR;
   }

   /**
    * Returns the number of lines read.
    * @return The number of lines read.
    * @throws IOException If an I/O error occurs.
    */
   public long getLines() throws IOException {
      return this.reader.getLinesRead();
   }

   /**
    * Returns the columns of the CSV file.
    * @return The columns of the CSV file.
    */
   public String[] getColumns() {
      return this.columns;
   }

   /**
    * Reads the next line of the CSV file.
    * @return The next line of the CSV file.
    * @throws IOException If an I/O error occurs.
    * @throws CsvValidationException If the CSV file is invalid.
    */
   public String[] readNext() throws IOException, CsvValidationException {
      return this.reader.readNext();
   }

   /**
    * Closes the CSV file.
    * @throws IOException If an I/O error occurs.
    */
   @Override
   public void close() throws IOException {
      this.reader.close();
   }

}
