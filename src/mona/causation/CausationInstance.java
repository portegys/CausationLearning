// For conditions of distribution and use, see copyright notice in CausationLearning.java

// Causation instance.

package mona.causation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Causation instances.
public class CausationInstance
{
   // Parameters.
   public static float EVENT_OMISSION_PROBABILITY           = 0.1f;
   public static float VALID_INTERVENING_EVENTS_PROBABILITY = 0.9f;

   public Causation causation;
   public int[]     events;
   public int       effectEventIndex;
   public boolean   valid;

   public CausationInstance(Causation causation, Random random)
   {
      this.causation = causation;
      events         = new int[Causation.CAUSATION_INSTANCE_LENGTH];
      valid          = true;
      int n = 0;
      if ((causation != null) && (Causation.MAX_CAUSE_EVENTS > 0))
      {
         List < List < Integer >> eventPermutations = CausationLearning.permuteList(causation.causeEvents);
         ArrayList<Integer> permutation = new ArrayList<Integer>();
         for (Integer i : eventPermutations.get(random.nextInt(eventPermutations.size())))
         {
            if (random.nextFloat() < EVENT_OMISSION_PROBABILITY)
            {
               valid = false;
            }
            else
            {
               permutation.add(i);
            }
         }
         for (int i : permutation)
         {
            int j = 0;
            if (random.nextFloat() < VALID_INTERVENING_EVENTS_PROBABILITY)
            {
               if (Causation.MAX_INTERVENING_EVENTS > 0)
               {
                  j = random.nextInt(Causation.MAX_VALID_INTERVENING_EVENTS + 1);
               }
            }
            else
            {
               if ((Causation.MAX_INTERVENING_EVENTS - Causation.MAX_VALID_INTERVENING_EVENTS) == 1)
               {
                  j = Causation.MAX_VALID_INTERVENING_EVENTS + 1;
               }
               else
               {
                  j = random.nextInt(Causation.MAX_INTERVENING_EVENTS -
                                     (Causation.MAX_VALID_INTERVENING_EVENTS + 1)) +
                      (Causation.MAX_VALID_INTERVENING_EVENTS + 1);
               }
               valid = false;
            }
            for (int k = 0; k < j; k++)
            {
               events[n] = Causation.NUM_CAUSE_EVENT_TYPES +
                           random.nextInt(Causation.NUM_EVENT_TYPES - Causation.NUM_CAUSE_EVENT_TYPES);
               n++;
            }
            events[n] = i;
            n++;
         }
      }
      int i = 0;
      if (random.nextFloat() < VALID_INTERVENING_EVENTS_PROBABILITY)
      {
         if (Causation.MAX_INTERVENING_EVENTS > 0)
         {
            i = random.nextInt(Causation.MAX_VALID_INTERVENING_EVENTS + 1);
         }
      }
      else
      {
         if ((Causation.MAX_INTERVENING_EVENTS - Causation.MAX_VALID_INTERVENING_EVENTS) == 1)
         {
            i = Causation.MAX_VALID_INTERVENING_EVENTS + 1;
         }
         else
         {
            i = random.nextInt(Causation.MAX_INTERVENING_EVENTS -
                               (Causation.MAX_VALID_INTERVENING_EVENTS + 1)) +
                (Causation.MAX_VALID_INTERVENING_EVENTS + 1);
         }
         valid = false;
      }
      for (int k = 0; k < i; k++)
      {
         events[n] = Causation.NUM_CAUSE_EVENT_TYPES +
                     random.nextInt(Causation.NUM_EVENT_TYPES - Causation.NUM_CAUSE_EVENT_TYPES);
         n++;
      }
      events[n]        = Causation.EFFECT_EVENT_TYPE;
      effectEventIndex = n;
      n++;
      for ( ; n < Causation.CAUSATION_INSTANCE_LENGTH; n++)
      {
         events[n] = Causation.NUM_CAUSE_EVENT_TYPES +
                     random.nextInt(Causation.NUM_EVENT_TYPES - Causation.NUM_CAUSE_EVENT_TYPES);
      }
   }


   public void print()
   {
      if (causation != null)
      {
         System.out.print("causation ID=" + causation.ID);
      }
      else
      {
         System.out.print("causation ID=null");
      }
      System.out.print(", events: { ");
      for (int i : events)
      {
         System.out.print(i + " ");
      }
      System.out.print("}, effect event index=" + effectEventIndex);
      if (valid)
      {
         System.out.println(", valid=true");
      }
      else
      {
         System.out.println(", valid=false");
      }
   }
}
