// For conditions of distribution and use, see copyright notice in CausationLearning.java

// Causation.

package mona.causation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Causation
{
	// Parameters.
	   public static int                NUM_EVENT_TYPES                      = 10;
	   public static int                NUM_CAUSE_EVENT_TYPES                = 5;
	   public static int                EFFECT_EVENT_TYPE                    = NUM_EVENT_TYPES;
	   public static int                MAX_CAUSE_EVENTS                     = 2;
	   public static int                MAX_INTERVENING_EVENTS               = 2;
	   public static int                MAX_VALID_INTERVENING_EVENTS         = 1;
	   public static int                CAUSATION_INSTANCE_LENGTH            = (MAX_CAUSE_EVENTS + 1) * (MAX_INTERVENING_EVENTS + 1);
   
      public ArrayList<Integer> causeEvents;
      
      public Causation(Random random)
      {
         causeEvents = new ArrayList<Integer>();
            int n = random.nextInt(Causation.MAX_CAUSE_EVENTS) + 1;
            for (int i = 0; i < n; i++)
            {
               causeEvents.add(random.nextInt(Causation.NUM_CAUSE_EVENT_TYPES));
            }
            Collections.sort(causeEvents);
      }

      public void print()
      {
         System.out.print("causes: { ");
         for (int i : causeEvents)
         {
            System.out.print(i + " ");
         }
         System.out.println("}, effect: " + EFFECT_EVENT_TYPE);
      }
}
