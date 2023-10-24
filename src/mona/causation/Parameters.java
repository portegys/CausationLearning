// For conditions of distribution and use, see copyright notice in Main.java

// Parameters.

package mona.causation;

public class Parameters
{
   public static int NUM_EVENT_TYPES       = 10;
   public static int NUM_CAUSATIONS        = 2;
   public static int MAX_CAUSE_EVENTS      = 2;
   public static int MAX_INTERVENING_EVENTS = 1;
   public static int EVENT_STREAM_LENGTH   = 100;
   
   // Print.
   public static void print()
   {
      System.out.println("NUM_EVENT_TYPES = " + NUM_EVENT_TYPES);
      System.out.println("NUM_CAUSATIONS = " + NUM_CAUSATIONS);
      System.out.println("MAX_CAUSE_EVENTS = " + MAX_CAUSE_EVENTS);
      System.out.println("MAX_INTERVENING_EVENTS = " + MAX_INTERVENING_EVENTS);
      System.out.println("EVENT_STREAM_LENGTH = " + EVENT_STREAM_LENGTH);
   }   
}
