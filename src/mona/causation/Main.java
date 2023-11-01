/*
 * Copyright (c) 2023 Tom Portegys (portegys@gmail.com). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY TOM PORTEGYS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Main.

package mona.causation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main
{
	// Parameters.
    public static int NUM_CAUSE_EVENT_TYPES       = 10;
    public static int EFFECT_EVENT_TYPE = NUM_CAUSE_EVENT_TYPES;
    public static int NUM_CAUSATIONS        = 2;
    public static int MAX_CAUSE_EVENTS      = 2;
    public static int MAX_INTERVENING_EVENTS = 1;
    public static int EVENT_STREAM_LENGTH   = 100;
    public static int NUM_CAUSATION_INSTANCES        = 10;
    
   // Random numbers.
   public static final int DEFAULT_RANDOM_SEED = 4517;
   public static int    randomSeed = DEFAULT_RANDOM_SEED;
   public static Random random;
   public final static  int MAX_TRIES = 100;
   
   // Version.
   public static final String VERSION = "1.0";
   
   // Causations.
   public static class Causation
   {
	   public ArrayList<Integer> causeEvents;
	   public Causation()
	   {
		   causeEvents = new ArrayList<Integer>();
	   }
   };   
   public static ArrayList<Causation> Causations;
   
   // Event stream.
   public static int[] EventStream;
   public static ArrayList<Integer>[] EffectCausations;
   
   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java mona.causation.Main\n" +
      "        [-numCauseEventTypes <quantity> (default=" + NUM_CAUSE_EVENT_TYPES + ")]\n" +
      "        [-numCausations <quantity> (default=" + NUM_CAUSATIONS + ")]\n" +
      "        [-maxCauseEvents <quantity> (default=" + MAX_CAUSE_EVENTS + ")]\n" +
      "        [-maxIntervening <quantity> (default=" + MAX_INTERVENING_EVENTS + ")]\n" +
      "        [-eventStreamLength <length> (default=" + EVENT_STREAM_LENGTH + ")]\n" +
      "        [-numCausationInstances <quantity> (default=" + NUM_CAUSATION_INSTANCES + ")]\n" +      
      "        [-randomSeed <random number seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +      
      "  Print parameters:\n" +
      "    java mona.causation.Main -printParameters\n" +
      "  Version:\n" +
      "    java mona.causation.Main -version\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=error";

   // Main.
   // Exit codes:
   // 0=success
   // 1=fail
   // 2=error
   public static void main(String[] args)
   {
      // Get options.
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-numCauseEventTypes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numCauseEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_CAUSE_EVENT_TYPES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numCauseEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_CAUSE_EVENT_TYPES <= 0)
            {
               System.err.println("Invalid numCauseEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numCausations"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numCausations option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_CAUSATIONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numCausations option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_CAUSATIONS < 0)
            {
               System.err.println("Invalid numCausations option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxCauseEvents"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxCauseEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MAX_CAUSE_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxCauseEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MAX_CAUSE_EVENTS <= 0)
            {
               System.err.println("Invalid maxCauseEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxInterveningEvents"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MAX_INTERVENING_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MAX_INTERVENING_EVENTS < 0)
            {
               System.err.println("Invalid maxInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         } 
         if (args[i].equals("-eventStreamLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid eventStreamLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EVENT_STREAM_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid eventStreamLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EVENT_STREAM_LENGTH < 0)
            {
               System.err.println("Invalid eventStreamLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numCausationInstances"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_CAUSATION_INSTANCES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_CAUSATION_INSTANCES < 0)
            {
               System.err.println("Invalid numCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }                             
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               randomSeed = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-printParameters"))
         {
             System.out.println("Parameters:");
             printParameters();
             System.exit(0);
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         if (args[i].equals("-version"))
         {
            System.out.println("Version = " + VERSION);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      // Initialize random numbers.
      random = new Random();
      random.setSeed(randomSeed);
      
      // Generate causations.
      // Constraints:
      // Unique events.
      // No subset causes.           
      Causations = new ArrayList<Causation>();
      for (int i = 0; i < NUM_CAUSATIONS; i++)
      {
    	  Causation causation = new Causation();
          int t0 = 0;    	  
		  for (; t0 < MAX_TRIES; t0++)
		  {
			  causation.causeEvents.clear();
			  
	    	  // Unique events.
	    	  int n = random.nextInt(MAX_CAUSE_EVENTS) + 1;
	    	  for (int j = 0; j < n; j++)
	    	  {
	    		  int c = -1;
	    		  int t1 = 0;
	    		  for (; t1 < MAX_TRIES; t1++)
	    		  {
	    			  c = random.nextInt(NUM_CAUSE_EVENT_TYPES);
	    			  int k = 0;
	    			  for (; k < j; k++)
	    			  {
	    				  if (c == causation.causeEvents.get(k))
	    				  {
	    					  break;
	    				  }
	    			  }
	    			  if (k == j) break;
	    		  }
	    		  if (c != -1 && t1 < MAX_TRIES)
	    		  {
		    		  causation.causeEvents.add(c);	    			  
	    		  } else {
	    			  System.err.println("Cannot generate unique cause event");
	    			  System.exit(1);
	    		  }
	    	  }
	    	  Collections.sort(causation.causeEvents);
	    		  
	    	  // Subset check.
			  boolean subset = false;
	    	  for (Causation c : Causations)
	    	  {
	    		  ArrayList<Integer> c1, c2;
	    		  if (c.causeEvents.size() <= causation.causeEvents.size()) 
	    		  {
	    			  c1 = c.causeEvents;
	    			  c2 = causation.causeEvents;
	    		  } else {
	    			  c1 = causation.causeEvents;
	    			  c2 = c.causeEvents;
	    		  }
	    		  int j = 0;	    		  
    			  for (int e1 : c1)
    			  {
    				  for (int e2 : c2)
    				  {
	    				  if (e1 == e2)
	    				  {
	    					  j++;
	    					  break;
	    				  }
    				  }
    			  }
    			  if (j == c1.size())
    			  {
    				 subset = true;
    				 break;
    			  }
	    	  }
	    	  if (!subset)
	    	  {
	    		  Causations.add(causation);	    		  
	    		  break;
	    	  }	    		  
		  }	
		  if (t0 == MAX_TRIES)
		  {
			  System.err.println("Cannot generate causation");
			  System.exit(1);
		  }
      } 
      
      // Generate event stream.
      EventStream = new int[EVENT_STREAM_LENGTH];
      EffectCausations = new ArrayList[EVENT_STREAM_LENGTH];      
      for (int i = 0; i < EVENT_STREAM_LENGTH; i++)
      {
    	  EventStream[i] = random.nextInt(NUM_CAUSE_EVENT_TYPES + 1);
    	  EffectCausations[i] = new ArrayList<Integer>();
      }
      
      // Map causations in stream.
      for (int i = 0; i < NUM_CAUSATIONS; i++)
      {
    	  Causation causation = Causations.get(i);
    	  List<List<Integer>> eventPermutations = permuteList(causation.causeEvents);
    	  for (List<Integer> eventPermutation : eventPermutations)
    	  {
        	  int startIndex = 0;
        	  while (startIndex != -1)
        	  {
        		  startIndex = mapCausation(i, eventPermutation, 0, -1, startIndex);
        	  }
    	  } 
      }
      
      // Learn and evaluate performance.

      System.exit(0);
   }
   
   // Map causations in stream.
   public static int mapCausation(int causationIndex, List<Integer> causeEvents, int eventIndex, int anchorIndex, int startIndex)
   {
	   if (eventIndex == 0)
	   {
		   for (int i = startIndex, j = EVENT_STREAM_LENGTH - causeEvents.size(); i < j; i++)
		   {
			   if (EventStream[i] == causeEvents.get(0))
			   {
				   return mapCausation(causationIndex, causeEvents, eventIndex + 1, i, i + 1);
			   }	   
		   }
		   return -1;
	   } else {
		   if (eventIndex < causeEvents.size())
		   {
			   for (int i = startIndex, j = EVENT_STREAM_LENGTH - (causeEvents.size() - eventIndex), k = 0; 
					   i < j && k <= MAX_INTERVENING_EVENTS; i++, k++)
			   {
				   if (EventStream[i] == causeEvents.get(eventIndex))
				   {
					   return mapCausation(causationIndex, causeEvents, eventIndex + 1, i, i + 1);
				   }	   
			   }
			   return anchorIndex + 1;			
		   } else {
			   for (int i = startIndex, j = 0; i < EVENT_STREAM_LENGTH && 
					   j <= MAX_INTERVENING_EVENTS; i++, j++)
			   {
				   if (EventStream[i] == EFFECT_EVENT_TYPE)
				   {
					   EffectCausations[i].add(causationIndex);
					   return i + 1;
				   }	   
			   }
			   return anchorIndex + 1;			   
		   }
	   }
   }
   
   // Permute list of numbers.
   public static List<List<Integer>> permuteList(List<Integer> input)
   {
	  int[] num = new int[input.size()];
      boolean[] used = new boolean[num.length];
      for (int i = 0; i < used.length; i++) 
      { 
    	  num[i] = input.get(i);
    	  used[i] = false; 
      }
      List<List<Integer>> output = new ArrayList<List<Integer>>();
      ArrayList<Integer> temp = new ArrayList<Integer>();
      permuteHelper(num, 0, used, output, temp);
      return(output);
   }

   private static void permuteHelper(int[] num, int level, boolean[] used, List<List<Integer>> output, ArrayList<Integer> temp)
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
   
   // Print.
   public static void printParameters()
   {
      System.out.println("NUM_CAUSE_EVENT_TYPES = " + NUM_CAUSE_EVENT_TYPES);
      System.out.println("EFFECT_EVENT_TYPE = " + EFFECT_EVENT_TYPE);     
      System.out.println("NUM_CAUSATIONS = " + NUM_CAUSATIONS);
      System.out.println("MAX_CAUSE_EVENTS = " + MAX_CAUSE_EVENTS);
      System.out.println("MAX_INTERVENING_EVENTS = " + MAX_INTERVENING_EVENTS);
      System.out.println("EVENT_STREAM_LENGTH = " + EVENT_STREAM_LENGTH);
      System.out.println("NUM_CAUSATION_INSTANCES = " + NUM_CAUSATION_INSTANCES);      
   }   	   
}
