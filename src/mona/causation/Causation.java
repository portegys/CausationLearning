// For conditions of distribution and use, see copyright notice in CausationLearning.java

// Causation.

package mona.causation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Causation
{
   // Parameters.
   public static int NUM_EVENT_TYPES              = 10;
   public static int NUM_CAUSE_EVENT_TYPES        = 5;
   public static int EFFECT_EVENT_TYPE            = NUM_EVENT_TYPES;
   public static int MAX_CAUSE_EVENTS             = 2;
   public static int MAX_INTERVENING_EVENTS       = 2;
   public static int MAX_VALID_INTERVENING_EVENTS = 1;
   public static int CAUSATION_INSTANCE_LENGTH    = (MAX_CAUSE_EVENTS + 1) * (MAX_INTERVENING_EVENTS + 1);
   public static void setCausationParms()
   {
      EFFECT_EVENT_TYPE         = NUM_EVENT_TYPES;
      CAUSATION_INSTANCE_LENGTH = (MAX_CAUSE_EVENTS + 1) * (MAX_INTERVENING_EVENTS + 1);
   }


   public int                ID;
   public ArrayList<Integer> causeEvents;

   public Causation(int ID, Random random)
   {
      this.ID     = ID;
      causeEvents = new ArrayList<Integer>();
      int n = 0;
      if (MAX_CAUSE_EVENTS > 0)
      {
         n = random.nextInt(MAX_CAUSE_EVENTS) + 1;
      }
      if (NUM_CAUSE_EVENT_TYPES > 0)
      {
         for (int i = 0; i < n; i++)
         {
            causeEvents.add(random.nextInt(NUM_CAUSE_EVENT_TYPES));
         }
         Collections.sort(causeEvents);
      }
   }


   public void print()
   {
      System.out.print("ID=" + ID);
      System.out.print(", cause events: { ");
      for (int i : causeEvents)
      {
         System.out.print(i + " ");
      }
      System.out.println("}, effect event: " + EFFECT_EVENT_TYPE);
   }
}
