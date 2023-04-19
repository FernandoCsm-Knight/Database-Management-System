package utils;

/**
 * The class {@code WatchTime} represents a stopwatch.
 * 
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 */
public final class WatchTime {
   
   private final String label; // label for the stopwatch
   private long begin = 0; // start time
   private long end = 0; // end time

   /**
    * Constructs a new {@code WatchTime} with no label.
    */
   public WatchTime() {
      this(null);
   }

   /**
    * Constructs a new {@code WatchTime} with the specified label.
    * @param label the label for the stopwatch.
    */
   public WatchTime(String label) {
      this.label = label;
   }

   /**
    * Returns the label for the stopwatch.
    * @return the label for the stopwatch.
    */
   public String getLabel() {
      return label;
   }

   /**
    * Returns the start time.
    * @return the start time.
    */
   public void start() {
      if(begin == 0) {
         begin = System.currentTimeMillis();
      } else {
         begin += System.currentTimeMillis() - end;
      }
   }

   /**
    * Returns the end time.
    * @return the end time.
    */
   public void suspend() {
      end = System.currentTimeMillis();
   }
 
   /**
    * Returns the elapsed time.
    * @return the elapsed time.
    */
   public void reset() {
      begin = end = 0;
   }

   /**
    * Returns the elapsed time.
    * @return the elapsed time.
    */
   public long stop() {
      end = System.currentTimeMillis();
      return end - begin;
   }

   /**
    * Returns the elapsed time.
    * @return the elapsed time.
    */
   public long elapsed() {
      return (end != 0) ? end - begin : System.currentTimeMillis() - begin;
   }

   /**
    * Returns a string representation of the stopwatch.
    * @return a string representation of the stopwatch.
    */
   @Override 
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(this.elapsed());
      sb.append("ms");
      return sb.toString();
   }

}
