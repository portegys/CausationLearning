// For conditions of distribution and use, see copyright notice in CausationLearning.java

/*
 * Event histogram algorithm for determining causation events.
 */

package mona.causation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EventHistogram
{
   // Causations.
   int NumCausations;
   Causation[] Causations;
   int[] MaxValidInterveningEvents;

   // Causation instances.
   ArrayList<CausationInstance> CausationInstances;

   // Event histogram counts.
   int[][] HistCounts;

   // Random numbers.
   Random Randomizer;

   // Log.
   public static boolean LOG = true;

   // Constructor.
   public EventHistogram(int numCausations, Random randomizer)
   {
      NumCausations = numCausations;
      Randomizer    = randomizer;
   }


   // Train.
   public void train(ArrayList<CausationInstance> causationTrainingInstances)
   {
      CausationInstances = causationTrainingInstances;

      log("Train");

      // Count event types.
      HistCounts = new int[NumCausations][];
      for (int i = 0; i < HistCounts.length; i++)
      {
         HistCounts[i] = new int[Causation.NUM_EVENT_TYPES];
         for (int j = 0; j < HistCounts[i].length; j++)
         {
            HistCounts[i][j] = -1;
         }
      }
      for (CausationInstance instance : CausationInstances)
      {
         for (int i = 0, j = instance.causationIDs.size(); i < j; i++)
         {
            int n = instance.causationIDs.get(i);
            for (int k = 0; k < Causation.NUM_EVENT_TYPES; k++)
            {
               int c = 0;
               for (int q = 0; q < instance.events.length; q++)
               {
                  if (instance.events[q] == k)
                  {
                     c++;
                  }
               }
               if ((HistCounts[n][k] == -1) || (c < HistCounts[n][k]))
               {
                  HistCounts[n][k] = c;
               }
            }
         }
      }

      // Create a causation with events that occur in every instance of a cause ID.
      Causations = new Causation[NumCausations];
      for (int i = 0; i < NumCausations; i++)
      {
         Causations[i] = null;
         Causation causation = new Causation();
         for (int j = 0; j < Causation.NUM_EVENT_TYPES; j++)
         {
            int n = HistCounts[i][j];
            for (int k = 0; k < n; k++)
            {
               causation.events.add(j);
            }
         }
         while (causation.events.size() > Causation.MAX_CAUSE_EVENTS)
         {
            causation.events.remove(Randomizer.nextInt(causation.events.size()));
         }
         if (causation.events.size() > 0)
         {
            Collections.sort(causation.events);
            Causations[i] = causation;
         }
      }

      // Measure maximum valid intervening events.
      MaxValidInterveningEvents = new int[NumCausations];
      for (int i = 0; i < NumCausations; i++)
      {
         MaxValidInterveningEvents[i] = 0;
      }
      for (CausationInstance instance : CausationInstances)
      {
         for (int i = 0, j = instance.causationIDs.size(); i < j; i++)
         {
            int n = instance.causationIDs.get(i);
            if (Causations[n] != null)
            {
               for (int k = 0; k < instance.events.length; k++)
               {
                  if (Causations[n].instanceOf(instance.events, k))
                  {
                     if (MaxValidInterveningEvents[n] < k)
                     {
                        MaxValidInterveningEvents[n] = k;
                     }
                     break;
                  }
               }
            }
         }
      }

      for (int i = 0; i < NumCausations; i++)
      {
         if (Causations[i] == null)
         {
            log("[" + i + "] null");
         }
         else
         {
            String s = "[" + i + "]";
            s += " events: { ";
            for (int e : Causations[i].events)
            {
               s += e + " ";
            }
            s += "}";
            s += ", maximum valid intervening events = " + MaxValidInterveningEvents[i];
            log(s);
         }
      }
   }


   // Test.
   public ArrayList<Boolean> test(ArrayList<CausationInstance> causationTestingInstances)
   {
      CausationInstances = causationTestingInstances;

      log("Test");

      ArrayList<Boolean> results = new ArrayList<Boolean>();
      for (CausationInstance instance : CausationInstances)
      {
         ArrayList<Integer> causationIDs = new ArrayList<Integer>();
         for (int i = 0; i < NumCausations; i++)
         {
            if ((Causations[i] != null) && Causations[i].instanceOf(instance.events, MaxValidInterveningEvents[i]))
            {
               causationIDs.add(i);
            }
         }
         boolean result = false;
         if (causationIDs.size() == instance.causationIDs.size())
         {
            result = true;
            for (int i = 0, j = causationIDs.size(); i < j; i++)
            {
               if (causationIDs.get(i) != instance.causationIDs.get(i))
               {
                  result = false;
                  break;
               }
            }
         }
         results.add(result);
         String message = "target: { ";
         boolean[] causationIndexes = new boolean[NumCausations];
         for (int i = 0; i < NumCausations; i++)
         {
            causationIndexes[i] = false;
         }
         for (int i = 0, j = instance.causationIDs.size(); i < j; i++)
         {
            int k = instance.causationIDs.get(i);
            causationIndexes[k] = true;
         }
         for (int i = 0; i < NumCausations; i++)
         {
            if (causationIndexes[i])
            {
               message += (i + " ");
            }
            else
            {
               message += "X ";
            }
         }
         message += "} predicted: { ";
         for (int i = 0; i < NumCausations; i++)
         {
            causationIndexes[i] = false;
         }
         for (int i = 0, j = causationIDs.size(); i < j; i++)
         {
            int k = instance.causationIDs.get(i);
            causationIndexes[k] = true;
         }
         for (int i = 0; i < NumCausations; i++)
         {
            if (causationIndexes[i])
            {
               message += (i + " ");
            }
            else
            {
               message += "X ";
            }
         }
         message += "}";
         if (result)
         {
            message += " OK";
         }
         else
         {
            message += " error";
         }
         log(message);
      }
      return(results);
   }


   // Logging.
   static void log(String message)
   {
      if (LOG)
      {
         System.out.println(message);
      }
   }
}
