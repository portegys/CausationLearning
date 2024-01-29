// For conditions of distribution and use, see copyright notice in CausationLearning.java

// Causation.

package mona.causation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Causation
{
   // Parameters.
   public static int NUM_EVENT_TYPES           = 10;
   public static int MAX_CAUSE_EVENTS          = 2;
   public static int MAX_INTERVENING_EVENTS    = 1;
   public static int CAUSATION_INSTANCE_LENGTH = ((MAX_CAUSE_EVENTS + 1) * MAX_INTERVENING_EVENTS) + MAX_CAUSE_EVENTS;
   public static void setCausationParms()
   {
      CAUSATION_INSTANCE_LENGTH = ((MAX_CAUSE_EVENTS + 1) * MAX_INTERVENING_EVENTS) + MAX_CAUSE_EVENTS;
   }


   public int                ID;
   public ArrayList<Integer> events;

   // Constructors.
   public Causation(int ID, Random random)
   {
      this.ID = ID;
      events  = new ArrayList<Integer>();
      int n = 0;
      if ((MAX_CAUSE_EVENTS > 0) && (NUM_EVENT_TYPES > 0))
      {
         n = random.nextInt(MAX_CAUSE_EVENTS) + 1;
         for (int i = 0; i < n; i++)
         {
            events.add(random.nextInt(NUM_EVENT_TYPES));
         }
         Collections.sort(events);
      }
   }


   public Causation()
   {
      ID     = -1;
      events = new ArrayList<Integer>();
   }


   // Is the stream of events a valid instance of causation?
   public boolean instanceOf(int[] eventStream, int maxInterveningEvents)
   {
      List < List < Integer >> permutations = permuteList(events);
      for (List<Integer> permutation : permutations)
      {
         int[] causeEvents = new int[permutation.size()];
         for (int i = 0; i < causeEvents.length; i++)
         {
            causeEvents[i] = permutation.get(i);
         }
         if (matchEventStream(eventStream, 0, causeEvents, maxInterveningEvents))
         {
            return(true);
         }
      }
      return(false);
   }


   // Permute list of numbers.
   static public List < List < Integer >> permuteList(List<Integer> input)
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

   static private void permuteHelper(int[] num, int level, boolean[] used,
                                     List < List < Integer >> output, ArrayList<Integer> temp)
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


   // Match cause event order in event stream.
   static public boolean matchEventStream(int[] eventStream, int startIndex,
                                          int[] orderedCauseEvents, int maxValidInterveningEvents)
   {
      if (orderedCauseEvents.length == 0)
      {
         return(true);
      }
      if (startIndex == eventStream.length)
      {
         return(false);
      }
      for (int i = 0, j = startIndex; i < orderedCauseEvents.length; i++)
      {
         int k = j;
         for ( ; k < eventStream.length; k++)
         {
            if (orderedCauseEvents[i] == eventStream[k])
            {
               if ((i == 0) || ((k - j) <= maxValidInterveningEvents))
               {
                  if (i == orderedCauseEvents.length - 1)
                  {
                     return(true);
                  }
                  else
                  {
                     j = k + 1;
                     break;
                  }
               }
            }
         }
         if (j <= k)
         {
            break;
         }
      }
      return(matchEventStream(eventStream, startIndex + 1,
                              orderedCauseEvents, maxValidInterveningEvents));
   }


   // Print.
   public void print()
   {
      System.out.print("ID=" + ID);
      System.out.print(", events: { ");
      for (int i : events)
      {
         System.out.print(i + " ");
      }
      System.out.println("}");
   }


   // Print parameters.
   public static void printParameters()
   {
      System.out.println("NUM_EVENT_TYPES = " + Causation.NUM_EVENT_TYPES);
      System.out.println("MAX_CAUSE_EVENTS = " + Causation.MAX_CAUSE_EVENTS);
      System.out.println("MAX_INTERVENING_EVENTS = " + Causation.MAX_INTERVENING_EVENTS);
      System.out.println("CAUSATION_INSTANCE_LENGTH = " + Causation.CAUSATION_INSTANCE_LENGTH);
   }
}
