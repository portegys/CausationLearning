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
	   public static float              EVENT_OMISSION_PROBABILITY           = 0.1f;
	   public static float              VALID_INTERVENING_EVENTS_PROBABILITY = 0.9f;
	   
	  public Causation causation;
	  public int causationID;
      public int[]   events;
      public int     effectEventIndex;
      public boolean valid;
      
      public CausationInstance(Causation causation, int causationID, Random random)
      {
    	  this.causation = causation;
    	  this.causationID = causationID;
         events         = new int[Causation.CAUSATION_INSTANCE_LENGTH];
         valid          = true;
         List < List < Integer >> eventPermutations = permuteList(causation.causeEvents);
         ArrayList<Integer> permutation = new ArrayList<Integer>();
         for (Integer i : eventPermutations.get(random.nextInt(eventPermutations.size())))
         {
        	 if (random.nextFloat() < EVENT_OMISSION_PROBABILITY)
        	 {
        		 valid = false;
        	 } else {
        		 permutation.add(i);
        	 }
         }
         int           j           = 0;
         for (int i : permutation)
         {
            int n = 0;
            if (random.nextFloat() < VALID_INTERVENING_EVENTS_PROBABILITY)
            {
               if (Causation.MAX_INTERVENING_EVENTS > 0)
               {
                  n = random.nextInt(Causation.MAX_VALID_INTERVENING_EVENTS + 1);
               }
            }
            else
            {
               if ((Causation.MAX_INTERVENING_EVENTS - Causation.MAX_VALID_INTERVENING_EVENTS) == 1)
               {
                  n = Causation.MAX_VALID_INTERVENING_EVENTS + 1;
               }
               else
               {
                  n = random.nextInt(Causation.MAX_INTERVENING_EVENTS - 
                      (Causation.MAX_VALID_INTERVENING_EVENTS + 1)) +
                      (Causation.MAX_VALID_INTERVENING_EVENTS + 1);
               }
               valid = false;
            }
            for (int k = 0; k < n; k++)
            {
               events[j] = Causation.NUM_CAUSE_EVENT_TYPES + 
            		   random.nextInt(Causation.NUM_EVENT_TYPES - Causation.NUM_CAUSE_EVENT_TYPES);
               j++;
            }
            events[j] = i;
            j++;
         }
         int n = 0;
         if (random.nextFloat() < VALID_INTERVENING_EVENTS_PROBABILITY)
         {
            if (Causation.MAX_INTERVENING_EVENTS > 0)
            {
               n = random.nextInt(Causation.MAX_VALID_INTERVENING_EVENTS + 1);
            }
         }
         else
         {
            if ((Causation.MAX_INTERVENING_EVENTS - Causation.MAX_VALID_INTERVENING_EVENTS) == 1)
            {
               n = Causation.MAX_VALID_INTERVENING_EVENTS + 1;
            }
            else
            {
               n = random.nextInt(Causation.MAX_INTERVENING_EVENTS - 
            	   (Causation.MAX_VALID_INTERVENING_EVENTS + 1)) +
                   (Causation.MAX_VALID_INTERVENING_EVENTS + 1);
            }
            valid = false;
         }
         for (int q = 0; q < n; q++)
         {
            events[j] = Causation.NUM_CAUSE_EVENT_TYPES + 
            		random.nextInt(Causation.NUM_EVENT_TYPES - Causation.NUM_CAUSE_EVENT_TYPES);
            j++;
         }
         events[j]        = Causation.EFFECT_EVENT_TYPE;
         effectEventIndex = j;
         j++;
         for ( ; j < Causation.CAUSATION_INSTANCE_LENGTH; j++)
         {
            events[j] = Causation.NUM_CAUSE_EVENT_TYPES + 
            		random.nextInt(Causation.NUM_EVENT_TYPES - Causation.NUM_CAUSE_EVENT_TYPES);
         }
      }

      // Permute list of numbers.
      public List < List < Integer >> permuteList(List<Integer> input)
      {
         int[] num      = new int[input.size()];
         boolean[] used = new boolean[num.length];
         for (int i = 0; i < used.length; i++)
         {
            num[i]  = input.get(i);
            used[i] = false;
         }
         List < List < Integer >> output = new ArrayList < List < Integer >> ();
         ArrayList<Integer> temp = new ArrayList<Integer>();
         permuteHelper(num, 0, used, output, temp);
         return(output);
      }

      private void permuteHelper(int[] num, int level, boolean[] used, List < List < Integer >> output, ArrayList<Integer> temp)
      {
         if (level == num.length)
         {
            output.add(new ArrayList<Integer>(temp));
         }
         else
         {
            for (int i = 0; i < num.length; i++)
            {
               if (!used[i])
               {
                  temp.add(num[i]);
                  used[i] = true;
                  permuteHelper(num, level + 1, used, output, temp);
                  used[i] = false;
                  temp.remove(temp.size() - 1);
               }
            }
         }
      }

      public void print()
      {
          System.out.print("causation ID=" + causationID);    	  
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
