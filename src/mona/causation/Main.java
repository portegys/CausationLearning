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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class Main
{
   // Parameters.
   public static int NUM_EVENT_TYPES           = 10;
   public static int NUM_CAUSE_EVENT_TYPES     = 5;
   public static int EFFECT_EVENT_TYPE         = NUM_EVENT_TYPES;
   public static int NUM_CAUSATIONS            = 2;
   public static int MAX_CAUSE_EVENTS          = 2;
   public static int MAX_INTERVENING_EVENTS    = 1;
   public static int CAUSATION_INSTANCE_LENGTH = (MAX_CAUSE_EVENTS + 1) * (MAX_INTERVENING_EVENTS + 1);
   public static int NUM_CAUSATION_INSTANCES   = 10;
   public static int NUM_NEURONS = 128;
   public static int NUM_EPOCHS = 500;
   
   // Random numbers.
   public static final int DEFAULT_RANDOM_SEED = 4517;
   public static int       RANDOM_SEED          = DEFAULT_RANDOM_SEED;
   public static Random    random;
   public final static int MAX_TRIES = 100;
   
   // RNN.
   public static final String RNN_DATASET_FILENAME = "causation_rnn_dataset.py";
   public static final String LSTM_FILENAME         = "causation_lstm.py";
   public static final String RNN_RESULTS_FILENAME = "causation_rnn_results.json";
   
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


      public void print()
      {
         System.out.print("causes: { ");
         for (int i : causeEvents)
         {
            System.out.print(i + " ");
         }
         System.out.println("}, effect: " + EFFECT_EVENT_TYPE);
      }
   };
   public static ArrayList<Causation> Causations;

   // Causation instances.
   public static class CausationInstance
   {
      public int[] events;
      public int   causationIndex;
      public int   effectEventIndex;
      public CausationInstance()
      {
         events         = new int[CAUSATION_INSTANCE_LENGTH];
         causationIndex = random.nextInt(NUM_CAUSATIONS);
         Causation causation = Causations.get(causationIndex);
         List < List < Integer >> eventPermutations = permuteList(causation.causeEvents);
         List<Integer> permutation = eventPermutations.get(random.nextInt(eventPermutations.size()));
         int           j           = 0;
         for (int k : permutation)
         {
            if (MAX_INTERVENING_EVENTS > 0)
            {
               int n = random.nextInt(MAX_INTERVENING_EVENTS + 1);
               for (int q = 0; q < n; q++)
               {
                  events[j] = NUM_CAUSE_EVENT_TYPES + random.nextInt(NUM_EVENT_TYPES - NUM_CAUSE_EVENT_TYPES);
                  j++;
               }
            }
            events[j] = k;
            j++;            
         }
         if (MAX_INTERVENING_EVENTS > 0)
         {
            int n = random.nextInt(MAX_INTERVENING_EVENTS + 1);
            for (int q = 0; q < n; q++)
            {
               events[j] = NUM_CAUSE_EVENT_TYPES + random.nextInt(NUM_EVENT_TYPES - NUM_CAUSE_EVENT_TYPES);
               j++;
            }
         }         
         events[j]        = EFFECT_EVENT_TYPE;
         effectEventIndex = j;
         j++;
         for ( ; j < CAUSATION_INSTANCE_LENGTH; j++)
         {
            events[j] = NUM_CAUSE_EVENT_TYPES + random.nextInt(NUM_EVENT_TYPES - NUM_CAUSE_EVENT_TYPES);
         }
      }

      public void print()
      {
         System.out.print("events: { ");
         for (int i : events)
         {
            System.out.print(i + " ");
         }
         System.out.println("}, effect event index=" + effectEventIndex);
      }
   };
   public static ArrayList<CausationInstance> CausationTrainingInstances;
   public static ArrayList<CausationInstance> CausationTestingInstances;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java mona.causation.Main\n" +
      "        [-numEventTypes <quantity> (default=" + NUM_EVENT_TYPES + ")]\n" +
      "        [-numCauseEventTypes <quantity> (default=" + NUM_CAUSE_EVENT_TYPES + ")]\n" +
      "        [-numCausations <quantity> (default=" + NUM_CAUSATIONS + ")]\n" +
      "        [-maxCauseEvents <quantity> (default=" + MAX_CAUSE_EVENTS + ")]\n" +
      "        [-maxInterveningEvents <quantity> (default=" + MAX_INTERVENING_EVENTS + ")]\n" +
      "        [-causationInstanceLength <length> (default=" + CAUSATION_INSTANCE_LENGTH + ")]\n" +
      "        [-numCausationInstances <quantity> (default=" + NUM_CAUSATION_INSTANCES + ")]\n" +
      "        [-numNeurons <quantity> (default=" + NUM_NEURONS + ")]\n" +
      "        [-numEpochs <quantity> (default=" + NUM_EPOCHS + ")]\n" +      
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
         if (args[i].equals("-numEventTypes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_EVENT_TYPES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_EVENT_TYPES <= 0)
            {
               System.err.println("Invalid numEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
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
         if (args[i].equals("-causationInstanceLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid causationInstanceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               CAUSATION_INSTANCE_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid causationInstanceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (CAUSATION_INSTANCE_LENGTH < 0)
            {
               System.err.println("Invalid causationInstanceLength option");
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
         if (args[i].equals("-numNeurons"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_NEURONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_NEURONS <= 0)
            {
               System.err.println("Invalid numNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numEpochs"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numEpochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_EPOCHS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numEpochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_EPOCHS < 0)
            {
               System.err.println("Invalid numEpochs option");
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
               RANDOM_SEED = Integer.parseInt(args[i]);
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
      if (NUM_CAUSE_EVENT_TYPES > NUM_EVENT_TYPES)
      {
         System.err.println("number of cause event types cannot be greater than number of event types");
         System.err.println(Usage);
         System.exit(1);
      }
      if ((MAX_INTERVENING_EVENTS > 0) && (NUM_CAUSE_EVENT_TYPES == NUM_EVENT_TYPES))
      {
         System.err.println("number of cause event types cannot be equal to the number of event types");
         System.err.println(Usage);
         System.exit(1);
      }

      // Initialize random numbers.
      random = new Random();
      random.setSeed(RANDOM_SEED);

      // Generate causations.
      Causations = new ArrayList<Causation>();
      for (int i = 0; i < NUM_CAUSATIONS; i++)
      {
         Causation causation = new Causation();
         int       t        = 0;
         for ( ; t < MAX_TRIES; t++)
         {
            causation.causeEvents.clear();
            int n = random.nextInt(MAX_CAUSE_EVENTS) + 1;
            for (int j = 0; j < n; j++)
            {
               causation.causeEvents.add(random.nextInt(NUM_CAUSE_EVENT_TYPES));            	
            }
            Collections.sort(causation.causeEvents);
            boolean duplicate = false;
            for (Causation c : Causations)
            {
               if (c.causeEvents.size() == causation.causeEvents.size())
               {
            	   duplicate = true;
	               for (int j = 0, k = causation.causeEvents.size(); j < k; j++)
	               {
	                  if (c.causeEvents.get(j) != causation.causeEvents.get(j))
	                  {
	                     duplicate = false;
	                     break;
	                  }
	               }
	               if (duplicate)
	               {
	                  break;
	               }
               }
            }
            if (!duplicate)
            {
               Causations.add(causation);
               break;
            }
         }
         if (t == MAX_TRIES)
         {
            System.err.println("Cannot generate causation");
            System.exit(1);
         }
      }

      // Generate causation instances.
      CausationTrainingInstances = new ArrayList<CausationInstance>();
      for (int i = 0; i < NUM_CAUSATION_INSTANCES; i++)
      {
         CausationTrainingInstances.add(new CausationInstance());
      }
      CausationTestingInstances = new ArrayList<CausationInstance>();
      for (int i = 0; i < NUM_CAUSATION_INSTANCES; i++)
      {
         CausationTestingInstances.add(new CausationInstance());
      }

      // Print causations.
      System.out.println("Parameters:");
      printParameters();
      System.out.println("Causations:");
      for (int i = 0; i < Causations.size(); i++)
      {
         Causation causation = Causations.get(i);
         System.out.print("[" + i + "] ");
         causation.print();
      }
      System.out.println("Causation training instances:");
      for (int i = 0; i < CausationTrainingInstances.size(); i++)
      {
         CausationInstance instance = CausationTrainingInstances.get(i);
         System.out.print("[" + i + "] causation number=" + instance.causationIndex + ", ");
         instance.print();
      }
      System.out.println("Causation testing instances:");
      for (int i = 0; i < CausationTestingInstances.size(); i++)
      {
         CausationInstance instance = CausationTestingInstances.get(i);
         System.out.print("[" + i + "] causation number=" + instance.causationIndex + ", ");
         instance.print();
      }

      // Learn and evaluate performance.
      try
      {
         FileWriter  fileWriter  = new FileWriter(RNN_DATASET_FILENAME);
         PrintWriter printWriter = new PrintWriter(fileWriter);
         printWriter.println("X_train_shape = [ " + NUM_CAUSATION_INSTANCES + ", " +
                             CAUSATION_INSTANCE_LENGTH + ", " + (NUM_EVENT_TYPES + 1) + " ]");
         printWriter.print("X_train_seq = [");
         String X_train = "";
         for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
         {
            X_train += "\n";
            CausationInstance instance = CausationTrainingInstances.get(i);
            for (int k = 0; k < instance.events.length; k++)
            {
               X_train += oneHot(instance.events[k], (NUM_EVENT_TYPES + 1));
               X_train += ",";
            }
         }
         if (X_train.endsWith(","))
         {
            X_train = X_train.substring(0, X_train.length() - 1);
         }
         printWriter.println(X_train);
         printWriter.println("]");
         printWriter.println("y_train_shape = [ " + NUM_CAUSATION_INSTANCES + ", " +
                             CAUSATION_INSTANCE_LENGTH + ", " + (NUM_CAUSATIONS + 1) + " ]");
         printWriter.print("y_train_seq = [");
         String y_train = "";
         for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
         {
            y_train += "\n";
            CausationInstance instance = CausationTrainingInstances.get(i);
            for (int k = 0; k < instance.events.length; k++)
            {
               if (instance.effectEventIndex == k)
               {
                  y_train += oneHot(instance.causationIndex, NUM_CAUSATIONS + 1);
               }
               else
               {
                  y_train += oneHot(NUM_CAUSATIONS, NUM_CAUSATIONS + 1);
               }
               y_train += ",";
            }
         }
         if (y_train.endsWith(","))
         {
            y_train = y_train.substring(0, y_train.length() - 1);
         }
         printWriter.println(y_train);
         printWriter.println("]");
         printWriter.println("X_test_shape = [ " + NUM_CAUSATION_INSTANCES + ", " +
                             CAUSATION_INSTANCE_LENGTH + ", " + (NUM_EVENT_TYPES + 1) + " ]");
         printWriter.print("X_test_seq = [");
         String X_test = "";
         for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
         {
            X_test += "\n";
            CausationInstance instance = CausationTestingInstances.get(i);
            for (int k = 0; k < instance.events.length; k++)
            {
               X_test += oneHot(instance.events[k], (NUM_EVENT_TYPES + 1));
               X_test += ",";
            }
         }
         if (X_test.endsWith(","))
         {
            X_test = X_test.substring(0, X_test.length() - 1);
         }
         printWriter.println(X_test);
         printWriter.println("]");
         printWriter.println("y_test_shape = [ " + NUM_CAUSATION_INSTANCES + ", " +
                             CAUSATION_INSTANCE_LENGTH + ", " + (NUM_CAUSATIONS + 1) + " ]");
         printWriter.print("y_test_seq = [");
         String y_test = "";
         for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
         {
            y_test += "\n";
            CausationInstance instance = CausationTestingInstances.get(i);
            for (int k = 0; k < instance.events.length; k++)
            {
               if (instance.effectEventIndex == k)
               {
                  y_test += oneHot(instance.causationIndex, NUM_CAUSATIONS + 1);
               }
               else
               {
                  y_test += oneHot(NUM_CAUSATIONS, NUM_CAUSATIONS + 1);
               }
               y_test += ",";
            }
         }
         if (y_test.endsWith(","))
         {
            y_test = y_test.substring(0, y_test.length() - 1);
         }
         printWriter.println(y_test);
         printWriter.println("]");
         printWriter.close();
      }
      catch (IOException e)
      {
         System.err.println("Cannot write RNN dataset to file " + RNN_DATASET_FILENAME);
         System.exit(1);
      }
      
      // Run LSTM.
      try
      {
         InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(LSTM_FILENAME);
         if (in == null)
         {
            System.err.println("Cannot access " + LSTM_FILENAME);
            System.exit(1);
         }
         File             pythonScript = new File(LSTM_FILENAME);
         FileOutputStream out          = new FileOutputStream(pythonScript);
         byte[] buffer = new byte[1024];
         int bytesRead;
         while ((bytesRead = in.read(buffer)) != -1)
         {
            out.write(buffer, 0, bytesRead);
         }
         out.close();
      }
      catch (Exception e)
      {
         System.err.println("Cannot create " + LSTM_FILENAME);
         System.exit(1);
      }
      ProcessBuilder processBuilder = new ProcessBuilder("python", LSTM_FILENAME,
                                                         "-n", (NUM_NEURONS + ""), "-e", (NUM_EPOCHS + ""), "-q");
      processBuilder.inheritIO();
      try
      {
         Process process = processBuilder.start();    	  
         process.waitFor();
      }
      catch (IOException e)
      {
         System.err.println("Cannot run " + LSTM_FILENAME);
         System.exit(1);
      }      
	  catch (InterruptedException e) {}
      
      // Fetch the results.
      int train_prediction_errors = 0;
      int train_total_predictions = 0;
      float train_error_pct = 0.0f;
      int test_prediction_errors = 0;
      int test_total_predictions = 0;
      float test_error_pct = 0.0f;       
      try      
      {
         BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(RNN_RESULTS_FILENAME)));
         String         json;
         if ((json = br.readLine()) != null)
         {
            JSONObject jObj = null;
            try
            {
               jObj = new JSONObject(json);
            }
            catch (JSONException e)
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            String value = jObj.getString("train_prediction_errors");
            if ((value == null) || value.isEmpty())
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            train_prediction_errors = Integer.parseInt(value);            
            value = jObj.getString("train_total_predictions");
            if ((value == null) || value.isEmpty())
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            train_total_predictions = Integer.parseInt(value);            
            value = jObj.getString("train_error_pct");
            if ((value == null) || value.isEmpty())
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            train_error_pct = Float.parseFloat(value);           
            value = jObj.getString("test_prediction_errors");
            if ((value == null) || value.isEmpty())
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            test_prediction_errors = Integer.parseInt(value);            
            value = jObj.getString("test_total_predictions");
            if ((value == null) || value.isEmpty())
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            test_total_predictions = Integer.parseInt(value);             
            value = jObj.getString("test_error_pct");
            if ((value == null) || value.isEmpty())
            {
               System.err.println("Error parsing results file " + RNN_RESULTS_FILENAME);
               System.exit(1);
            }
            test_error_pct = Float.parseFloat(value);             
         }
         else
         {
            System.err.println("Cannot read results file " + RNN_RESULTS_FILENAME);
            System.exit(1);
         }
         br.close();
      }
      catch (Exception e)
      {
         System.err.println("Cannot read results file " + RNN_RESULTS_FILENAME + ":" + e.getMessage());
         System.exit(1);
      }
      System.out.println("Train correct paths/total = " + (train_total_predictions - train_prediction_errors) + 
    		  "/" + train_total_predictions + " (" + (100.0 - train_error_pct) + "%), prediction errors/total = " +
    		  train_prediction_errors + "/" + train_total_predictions + " (" + train_error_pct + "%)");
      System.out.println("Test correct paths/total = " + (test_total_predictions - test_prediction_errors) + 
    		  "/" + test_total_predictions + " (" + (100.0 - test_error_pct) + "%), prediction errors/total = " +
    		  test_prediction_errors + "/" + test_total_predictions + " (" + test_error_pct + "%)");      
      System.exit(0);
   }

   // One hot encoding of integer.
   public static String oneHot(int n, int numvals)
   {
      String encoding = "";

      for (int i = 0; i < numvals; i++)
      {
         if (i == n)
         {
            encoding += "1";
         }
         else
         {
            encoding += "0";
         }
         if (i < numvals - 1)
         {
            encoding += ",";
         }
      }
      return(encoding);
   }


   // Permute list of numbers.
   public static List < List < Integer >> permuteList(List<Integer> input)
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

   private static void permuteHelper(int[] num, int level, boolean[] used, List < List < Integer >> output, ArrayList<Integer> temp)
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
      System.out.println("NUM_EVENT_TYPES = " + NUM_EVENT_TYPES);
      System.out.println("NUM_CAUSE_EVENT_TYPES = " + NUM_CAUSE_EVENT_TYPES);
      System.out.println("EFFECT_EVENT_TYPE = " + EFFECT_EVENT_TYPE);
      System.out.println("NUM_CAUSATIONS = " + NUM_CAUSATIONS);
      System.out.println("MAX_CAUSE_EVENTS = " + MAX_CAUSE_EVENTS);
      System.out.println("MAX_INTERVENING_EVENTS = " + MAX_INTERVENING_EVENTS);
      System.out.println("CAUSATION_INSTANCE_LENGTH = " + CAUSATION_INSTANCE_LENGTH);
      System.out.println("NUM_CAUSATION_INSTANCES = " + NUM_CAUSATION_INSTANCES);
      System.out.println("CAUSATION_INSTANCE_LENGTH = " + CAUSATION_INSTANCE_LENGTH);
      System.out.println("NUM_NEURONS = " + NUM_NEURONS);
      System.out.println("NUM_EPOCHS = " + NUM_EPOCHS);
      System.out.println("RANDOM_SEED = " + RANDOM_SEED);      
   }
}
