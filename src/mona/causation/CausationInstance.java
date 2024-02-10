// For conditions of distribution and use, see copyright notice in CausationLearning.java

// Causation instance.

package mona.causation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

// Causation instances.
public class CausationInstance
{
   public int[]              events;
   public ArrayList<Integer> causationIDs;

   // Constructors.
   public CausationInstance(Random random)
   {
      events = new int[Causation.CAUSATION_INSTANCE_LENGTH];
      for (int i = 0; i < Causation.CAUSATION_INSTANCE_LENGTH; i++)
      {
         events[i] = random.nextInt(Causation.NUM_EVENT_TYPES);
      }
      causationIDs = new ArrayList<Integer>();
   }


   // Instance of causation ID?
   public boolean instanceOf(int causationID)
   {
      for (int i = 0, j = causationIDs.size(); i < j; i++)
      {
         if (causationIDs.get(i) == causationID)
         {
            return(true);
         }
      }
      return(false);
   }


   // JSON.
   public void toJSON(PrintWriter printWriter)
   {
      printWriter.println("{");
      printWriter.print("  \"Events\": [ ");
      for (int i = 0, j = events.length; i < j; i++)
      {
         printWriter.print("\"" + events[i] + "\"");
         if (i < j - 1)
         {
            printWriter.print(",");
         }
         printWriter.print(" ");
      }
      printWriter.println("],");
      printWriter.print("  \"Causation IDs\": [ ");
      for (int i = 0, j = causationIDs.size(); i < j; i++)
      {
         printWriter.print("\"" + causationIDs.get(i) + "\"");
         if (i < j - 1)
         {
            printWriter.print(",");
         }
         printWriter.print(" ");
      }
      printWriter.println("]");
      printWriter.print("}");
   }


   // Print.
   public void print()
   {
      System.out.print("Events: { ");
      for (int i : events)
      {
         System.out.print(i + " ");
      }
      System.out.print("}");
      System.out.print(" Causation IDs: { ");
      for (int i : causationIDs)
      {
         System.out.print(i + " ");
      }
      System.out.println("}");
   }
}
