// For conditions of distribution and use, see copyright notice in CausationLearning.java

// Causation instance.

package mona.causation;

import java.util.ArrayList;
import java.util.Random;

// Causation instances.
public class CausationInstance
{
   public int[]     events;
   public ArrayList<Integer> causationIDs;

   // Constructors.
   public CausationInstance(Random random)
   {
      events         = new int[Causation.CAUSATION_INSTANCE_LENGTH];
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
			   return true;
		   }
	   }
	   return false;
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
