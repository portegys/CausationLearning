/*
 * Copyright (c) 2023-2024 Tom Portegys (portegys@gmail.com). All rights reserved.
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

// Causation learning.

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class CausationLearning
{
   // Parameters.
   public static int                NUM_CAUSATIONS = 2;
   public static int                NUM_TRAINING_CAUSATION_INSTANCES = 10;
   public static int                NUM_TESTING_CAUSATION_INSTANCES  = 10;
   public static String             LEARNER    = "LSTM";
   public static int                NUM_EPOCHS = 500;
   public static final int          DEFAULT_NUM_HIDDEN_NEURONS = 128;
   public static ArrayList<Integer> NUM_HIDDEN_NEURONS;
   static
   {
      NUM_HIDDEN_NEURONS = new ArrayList<Integer>();
      NUM_HIDDEN_NEURONS.add(DEFAULT_NUM_HIDDEN_NEURONS);
   }

   // Random numbers.
   public static final int DEFAULT_RANDOM_SEED = 4517;
   public static int       RANDOM_SEED         = DEFAULT_RANDOM_SEED;
   public static Random    random;
   public final static int MAX_TRIES = 100;

   // Files.
   public static final String RNN_DATASET_FILENAME       = "causation_rnn_dataset.py";
   public static final String RNN_FILENAME               = "causation_rnn.py";
   public static final String RNN_RESULTS_FILENAME       = "causation_rnn_results.json";
   public static final String ATTENTION_DATASET_FILENAME = "causation_attention_dataset.py";
   public static final String ATTENTION_FILENAME         = "causation_attention.py";
   public static final String ATTENTION_RESULTS_FILENAME = "causation_attention_results.json";
   public static final String NN_DATASET_FILENAME        = "causation_nn_dataset.py";
   public static final String NN_FILENAME         = "causation_nn.py";
   public static final String NN_RESULTS_FILENAME = "causation_nn_results.json";
   public static final String GA_RESULTS_FILENAME = "causation_ga_results.json";

   // Version.
   public static final String VERSION = "1.0";

   // Causations.
   public static ArrayList<Causation> Causations;

   // Causation instances.
   public static ArrayList<CausationInstance> CausationTrainingInstances;
   public static ArrayList<CausationInstance> CausationTestingInstances;

   // GA.
   public static EvolveCausations CausationsGA;

   // Verbosity.
   public static boolean Verbose = true;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  Run:\n" +
      "    java mona.causation.CausationLearning\n" +
      "        [-numEventTypes <quantity> (default=" + Causation.NUM_EVENT_TYPES + ")]\n" +
      "        [-numCauseEventTypes <quantity> (default=" + Causation.NUM_CAUSE_EVENT_TYPES + ")]\n" +
      "        [-numCausations <quantity> (default=" + NUM_CAUSATIONS + ")]\n" +
      "        [-maxCauseEvents <quantity> (default=" + Causation.MAX_CAUSE_EVENTS + ")]\n" +
      "        [-eventOmissionProbability <probability> (default=" + CausationInstance.EVENT_OMISSION_PROBABILITY + ")]\n" +
      "        [-maxInterveningEvents <quantity> (default=" + Causation.MAX_INTERVENING_EVENTS + ")]\n" +
      "        [-maxValidInterveningEvents <quantity> (default=" + Causation.MAX_VALID_INTERVENING_EVENTS + ")]\n" +
      "        [-validInterveningEventsProbability <probability> (default=" + CausationInstance.VALID_INTERVENING_EVENTS_PROBABILITY + ")]\n" +
      "        [-numTrainingCausationInstances <quantity> (default=" + NUM_TRAINING_CAUSATION_INSTANCES + ")]\n" +
      "        [-numTestingCausationInstances <quantity> (default=" + NUM_TESTING_CAUSATION_INSTANCES + ")]\n" +
      "        [-learner\n" +
      "           \"LSTM\" | \"SimpleRNN\" | \"Attention\" | \"NN\" |\n" +
      "             [-numHiddenNeurons <quantity> (default=" + DEFAULT_NUM_HIDDEN_NEURONS + ") (repeat for additional layers)]\n" +
      "             [-numEpochs <quantity> (default=" + NUM_EPOCHS + ")]\n" +
      "           \"GA\" (default=" + LEARNER + ")]\n" +
      "             [-generations <quantity> (default=" + EvolveCausations.GENERATIONS + ")]\n" +
      "             [-populationSize <quantity> (default=" + EvolveCausations.POPULATION_SIZE + ")]\n" +
      "             [-fitPopulationSize <quantity> (default=" + EvolveCausations.FIT_POPULATION_SIZE + ")]\n" +
      "             [-mutationRate <probability> (default=" + EvolveCausations.MUTATION_RATE + ")]\n" +
      "        [-randomSeed <random number seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +
      "        [-verbose \"true\" | \"false\" (default=" + Verbose + ")]\n" +
      "  Print parameters:\n" +
      "    java mona.causation.CausationLearning -printParameters\n" +
      "  Version:\n" +
      "    java mona.causation.CausationLearning -version\n" +
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
      boolean gotNumHidden         = false;
      boolean gotNumEpochs         = false;
      boolean gotGenerations       = false;
      boolean gotPopulationSize    = false;
      boolean gotFitPopulationSize = false;
      boolean gotMutationRate      = false;
      boolean firstHidden          = true;
      boolean printParms           = false;

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
               Causation.NUM_EVENT_TYPES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Causation.NUM_EVENT_TYPES < 0)
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
               Causation.NUM_CAUSE_EVENT_TYPES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numCauseEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Causation.NUM_CAUSE_EVENT_TYPES < 0)
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
               Causation.MAX_CAUSE_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxCauseEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Causation.MAX_CAUSE_EVENTS < 0)
            {
               System.err.println("Invalid maxCauseEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-eventOmissionProbability"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid eventOmissionProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               CausationInstance.EVENT_OMISSION_PROBABILITY = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid eventOmissionProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((CausationInstance.EVENT_OMISSION_PROBABILITY < 0.0f) ||
                (CausationInstance.EVENT_OMISSION_PROBABILITY > 1.0f))
            {
               System.err.println("Invalid eventOmissionProbability option");
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
               Causation.MAX_INTERVENING_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Causation.MAX_INTERVENING_EVENTS < 0)
            {
               System.err.println("Invalid maxInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxValidInterveningEvents"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxValidInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Causation.MAX_VALID_INTERVENING_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxValidInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Causation.MAX_VALID_INTERVENING_EVENTS < 0)
            {
               System.err.println("Invalid maxValidInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-validInterveningEventsProbability"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid validInterveningEventsProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               CausationInstance.VALID_INTERVENING_EVENTS_PROBABILITY = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid validInterveningEventsProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((CausationInstance.VALID_INTERVENING_EVENTS_PROBABILITY < 0.0f) ||
                (CausationInstance.VALID_INTERVENING_EVENTS_PROBABILITY > 1.0f))
            {
               System.err.println("Invalid validInterveningEventsProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numTrainingCausationInstances"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numTrainingCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_TRAINING_CAUSATION_INSTANCES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numTrainingCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_TRAINING_CAUSATION_INSTANCES < 0)
            {
               System.err.println("Invalid numTrainingCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numTestingCausationInstances"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numTestingCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_TESTING_CAUSATION_INSTANCES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numTestingCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_TESTING_CAUSATION_INSTANCES < 0)
            {
               System.err.println("Invalid numTestingCausationInstances option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-learner"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid learner option");
               System.err.println(Usage);
               System.exit(1);
            }
            LEARNER = args[i];
            if (!LEARNER.equals("LSTM") && !LEARNER.equals("SimpleRNN") &&
                !LEARNER.equals("Attention") && !LEARNER.equals("NN") && !LEARNER.equals("GA"))
            {
               System.err.println("Invalid learner option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numHiddenNeurons"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            int n = -1;
            try
            {
               n = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (n <= 0)
            {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (firstHidden)
            {
               firstHidden = false;
               NUM_HIDDEN_NEURONS.clear();
            }
            NUM_HIDDEN_NEURONS.add(n);
            gotNumHidden = true;
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
            gotNumEpochs = true;
            continue;
         }
         if (args[i].equals("-generations"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid generations option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EvolveCausations.GENERATIONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid generations option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EvolveCausations.GENERATIONS < 0)
            {
               System.err.println("Invalid generations option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotGenerations = true;
            continue;
         }
         if (args[i].equals("-populationSize"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid populationSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EvolveCausations.POPULATION_SIZE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid populationSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EvolveCausations.POPULATION_SIZE < 0)
            {
               System.err.println("Invalid populationSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotPopulationSize = true;
            continue;
         }
         if (args[i].equals("-fitPopulationSize"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid fitPopulationSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EvolveCausations.FIT_POPULATION_SIZE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid fitPopulationSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EvolveCausations.FIT_POPULATION_SIZE < 0)
            {
               System.err.println("Invalid fitPopulationSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotFitPopulationSize = true;
            continue;
         }
         if (args[i].equals("-mutationRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid mutationRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EvolveCausations.MUTATION_RATE = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid mutationRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((EvolveCausations.MUTATION_RATE < 0.0f) || (EvolveCausations.MUTATION_RATE > 1.0f))
            {
               System.err.println("Invalid mutationRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotMutationRate = true;
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
         if (args[i].equals("-verbose"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid verbose option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (args[i].equals("true"))
            {
               Verbose = true;
            }
            else if (args[i].equals("false"))
            {
               Verbose = false;
            }
            else
            {
               System.err.println("Invalid verbose option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-printParameters"))
         {
            printParms = true;
            continue;
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
      Causation.setCausationParms();
      if (printParms)
      {
         System.out.println("Parameters:");
         printParameters();
         System.exit(0);
      }
      if (Causation.NUM_CAUSE_EVENT_TYPES > Causation.NUM_EVENT_TYPES)
      {
         System.err.println("Number of cause event types cannot be greater than number of event types");
         System.err.println(Usage);
         System.exit(1);
      }
      if ((Causation.MAX_INTERVENING_EVENTS > 0) &&
          (Causation.NUM_CAUSE_EVENT_TYPES == Causation.NUM_EVENT_TYPES))
      {
         System.err.println("Number of cause event types cannot be equal to the number of event types");
         System.err.println(Usage);
         System.exit(1);
      }
      if (Causation.MAX_VALID_INTERVENING_EVENTS > Causation.MAX_INTERVENING_EVENTS)
      {
         System.err.println("Max valid intervening events cannot be greater than max intervening events");
         System.err.println(Usage);
         System.exit(1);
      }
      if ((CausationInstance.VALID_INTERVENING_EVENTS_PROBABILITY < 1.0f) &&
          ((Causation.MAX_INTERVENING_EVENTS == 0) ||
           (Causation.MAX_VALID_INTERVENING_EVENTS == Causation.MAX_INTERVENING_EVENTS)))
      {
         System.err.println("Not possible to have an invalid number of intervening events");
         System.err.println(Usage);
         System.exit(1);
      }
      if (LEARNER.equals("Attention") && (NUM_HIDDEN_NEURONS.size() > 1))
      {
         System.err.println("Attention network limited to single hidden layer");
         System.err.println(Usage);
         System.exit(1);
      }
      if (NUM_TRAINING_CAUSATION_INSTANCES < NUM_CAUSATIONS)
      {
         System.err.println("Number of training causation instances cannot be less than number of causations");
         System.err.println(Usage);
         System.exit(1);
      }
      if (LEARNER.equals("GA"))
      {
         if (gotNumHidden || gotNumEpochs)
         {
            System.err.println("Incompatible learner options");
            System.err.println(Usage);
            System.exit(1);
         }
         if (EvolveCausations.FIT_POPULATION_SIZE > EvolveCausations.POPULATION_SIZE)
         {
            System.err.println("Fit population size cannot exceed population size");
            System.err.println(Usage);
            System.exit(1);
         }
         if (EvolveCausations.POPULATION_SIZE < NUM_CAUSATIONS)
         {
            System.err.println("Population size cannot be less than number of causations");
            System.err.println(Usage);
            System.exit(1);
         }
         EvolveCausations.LOG = Verbose;
      }
      else
      {
         if (gotGenerations || gotPopulationSize || gotFitPopulationSize || gotMutationRate)
         {
            System.err.println("Incompatible learner options");
            System.err.println(Usage);
            System.exit(1);
         }
      }

      // Initialize random numbers.
      random = new Random();
      random.setSeed(RANDOM_SEED);

      // Generate causations.
      Causations = new ArrayList<Causation>();
      for (int i = 0; i < NUM_CAUSATIONS; i++)
      {
         int t = 0;
         for ( ; t < MAX_TRIES; t++)
         {
            Causation causation = new Causation(i, random);
            boolean   duplicate = false;
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
      for (int i = 0, j = random.nextInt(NUM_CAUSATIONS); i < NUM_TRAINING_CAUSATION_INSTANCES; i++, j = (j + 1) % NUM_CAUSATIONS)
      {
         Causation causation = null;
         if (NUM_CAUSATIONS > 0)
         {
            causation = Causations.get(j);
         }
         CausationTrainingInstances.add(new CausationInstance(causation, random));
      }
      CausationTestingInstances = new ArrayList<CausationInstance>();
      for (int i = 0, j = random.nextInt(NUM_CAUSATIONS); i < NUM_TESTING_CAUSATION_INSTANCES; i++, j = (j + 1) % NUM_CAUSATIONS)
      {
         Causation causation = null;
         if (NUM_CAUSATIONS > 0)
         {
            causation = Causations.get(j);
         }
         CausationTestingInstances.add(new CausationInstance(causation, random));
      }

      // Print parameters and causations.
      if (Verbose)
      {
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
            System.out.print("[" + i + "] ");
            instance.print();
         }
         System.out.println("Causation testing instances:");
         for (int i = 0; i < CausationTestingInstances.size(); i++)
         {
            CausationInstance instance = CausationTestingInstances.get(i);
            System.out.print("[" + i + "] ");
            instance.print();
         }
      }

      // Learn and evaluate performance.
      if (LEARNER.equals("NN"))
      {
         try
         {
            FileWriter  fileWriter  = new FileWriter(NN_DATASET_FILENAME);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("X_train_shape = [ " + NUM_TRAINING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH * (Causation.NUM_EVENT_TYPES + 1) + " ]");
            printWriter.print("X_train_seq = [");
            String X_train = "";
            for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
            {
               X_train += "\n";
               CausationInstance instance = CausationTrainingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  X_train += oneHot(instance.events[k], (Causation.NUM_EVENT_TYPES + 1));
                  X_train += ",";
               }
            }
            if (X_train.endsWith(","))
            {
               X_train = X_train.substring(0, X_train.length() - 1);
            }
            printWriter.println(X_train);
            printWriter.println("]");
            printWriter.println("y_train_shape = [ " + NUM_TRAINING_CAUSATION_INSTANCES + ", " +
                                (NUM_CAUSATIONS + 1) + " ]");
            printWriter.print("y_train_seq = [");
            String y_train = "";
            for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
            {
               y_train += "\n";
               CausationInstance instance = CausationTrainingInstances.get(i);
               if (instance.valid && (instance.causation != null))
               {
                  y_train += oneHot(instance.causation.ID, NUM_CAUSATIONS + 1) + ",";
               }
               else
               {
                  y_train += oneHot(NUM_CAUSATIONS, NUM_CAUSATIONS + 1) + ",";
               }
            }
            if (y_train.endsWith(","))
            {
               y_train = y_train.substring(0, y_train.length() - 1);
            }
            printWriter.println(y_train);
            printWriter.println("]");
            printWriter.println("X_test_shape = [ " + NUM_TESTING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH * (Causation.NUM_EVENT_TYPES + 1) + " ]");
            printWriter.print("X_test_seq = [");
            String X_test = "";
            for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
            {
               X_test += "\n";
               CausationInstance instance = CausationTestingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  X_test += oneHot(instance.events[k], (Causation.NUM_EVENT_TYPES + 1));
                  X_test += ",";
               }
            }
            if (X_test.endsWith(","))
            {
               X_test = X_test.substring(0, X_test.length() - 1);
            }
            printWriter.println(X_test);
            printWriter.println("]");
            printWriter.println("y_test_shape = [ " + NUM_TESTING_CAUSATION_INSTANCES + ", " +
                                (NUM_CAUSATIONS + 1) + " ]");
            printWriter.print("y_test_seq = [");
            String y_test = "";
            for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
            {
               y_test += "\n";
               CausationInstance instance = CausationTestingInstances.get(i);
               if (instance.valid && (instance.causation != null))
               {
                  y_test += oneHot(instance.causation.ID, NUM_CAUSATIONS + 1) + ",";
               }
               else
               {
                  y_test += oneHot(NUM_CAUSATIONS, NUM_CAUSATIONS + 1) + ",";
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
            System.err.println("Cannot write NN dataset to file " + NN_DATASET_FILENAME);
            System.exit(1);
         }
      }
      else if (LEARNER.equals("Attention"))
      {
         try
         {
            FileWriter  fileWriter  = new FileWriter(ATTENTION_DATASET_FILENAME);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("X_train_shape = [ " + NUM_TRAINING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH + ", " + (Causation.NUM_EVENT_TYPES + 1) + " ]");
            printWriter.print("X_train_seq = [");
            String X_train = "";
            for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
            {
               X_train += "\n";
               CausationInstance instance = CausationTrainingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  X_train += oneHot(instance.events[k], (Causation.NUM_EVENT_TYPES + 1));
                  X_train += ",";
               }
            }
            if (X_train.endsWith(","))
            {
               X_train = X_train.substring(0, X_train.length() - 1);
            }
            printWriter.println(X_train);
            printWriter.println("]");
            printWriter.println("y_train_shape = [ " + NUM_TRAINING_CAUSATION_INSTANCES + ", " +
                                (NUM_CAUSATIONS + 1) + " ]");
            printWriter.print("y_train_seq = [");
            String y_train = "";
            for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
            {
               y_train += "\n";
               CausationInstance instance = CausationTrainingInstances.get(i);
               if (instance.valid && (instance.causation != null))
               {
                  y_train += oneHot(instance.causation.ID, NUM_CAUSATIONS + 1) + ",";
               }
               else
               {
                  y_train += oneHot(NUM_CAUSATIONS, NUM_CAUSATIONS + 1) + ",";
               }
            }
            if (y_train.endsWith(","))
            {
               y_train = y_train.substring(0, y_train.length() - 1);
            }
            printWriter.println(y_train);
            printWriter.println("]");
            printWriter.println("X_test_shape = [ " + NUM_TESTING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH + ", " + (Causation.NUM_EVENT_TYPES + 1) + " ]");
            printWriter.print("X_test_seq = [");
            String X_test = "";
            for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
            {
               X_test += "\n";
               CausationInstance instance = CausationTestingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  X_test += oneHot(instance.events[k], (Causation.NUM_EVENT_TYPES + 1));
                  X_test += ",";
               }
            }
            if (X_test.endsWith(","))
            {
               X_test = X_test.substring(0, X_test.length() - 1);
            }
            printWriter.println(X_test);
            printWriter.println("]");
            printWriter.println("y_test_shape = [ " + NUM_TESTING_CAUSATION_INSTANCES + ", " +
                                (NUM_CAUSATIONS + 1) + " ]");
            printWriter.print("y_test_seq = [");
            String y_test = "";
            for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
            {
               y_test += "\n";
               CausationInstance instance = CausationTestingInstances.get(i);
               if (instance.valid && (instance.causation != null))
               {
                  y_test += oneHot(instance.causation.ID, NUM_CAUSATIONS + 1) + ",";
               }
               else
               {
                  y_test += oneHot(NUM_CAUSATIONS, NUM_CAUSATIONS + 1) + ",";
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
            System.err.println("Cannot write RNN dataset to file " + ATTENTION_DATASET_FILENAME);
            System.exit(1);
         }
      }
      else if (LEARNER.equals("LSTM") || LEARNER.equals("SimpleRNN"))
      {
         try
         {
            FileWriter  fileWriter  = new FileWriter(RNN_DATASET_FILENAME);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("X_train_shape = [ " + NUM_TRAINING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH + ", " + (Causation.NUM_EVENT_TYPES + 1) + " ]");
            printWriter.print("X_train_seq = [");
            String X_train = "";
            for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
            {
               X_train += "\n";
               CausationInstance instance = CausationTrainingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  X_train += oneHot(instance.events[k], (Causation.NUM_EVENT_TYPES + 1));
                  X_train += ",";
               }
            }
            if (X_train.endsWith(","))
            {
               X_train = X_train.substring(0, X_train.length() - 1);
            }
            printWriter.println(X_train);
            printWriter.println("]");
            printWriter.println("y_train_shape = [ " + NUM_TRAINING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH + ", " + (NUM_CAUSATIONS + 1) + " ]");
            printWriter.print("y_train_seq = [");
            String y_train = "";
            for (int i = 0, j = CausationTrainingInstances.size(); i < j; i++)
            {
               y_train += "\n";
               CausationInstance instance = CausationTrainingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  if ((instance.effectEventIndex == k) && instance.valid && (instance.causation != null))
                  {
                     y_train += oneHot(instance.causation.ID, NUM_CAUSATIONS + 1);
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
            printWriter.println("X_test_shape = [ " + NUM_TESTING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH + ", " + (Causation.NUM_EVENT_TYPES + 1) + " ]");
            printWriter.print("X_test_seq = [");
            String X_test = "";
            for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
            {
               X_test += "\n";
               CausationInstance instance = CausationTestingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  X_test += oneHot(instance.events[k], (Causation.NUM_EVENT_TYPES + 1));
                  X_test += ",";
               }
            }
            if (X_test.endsWith(","))
            {
               X_test = X_test.substring(0, X_test.length() - 1);
            }
            printWriter.println(X_test);
            printWriter.println("]");
            printWriter.println("y_test_shape = [ " + NUM_TESTING_CAUSATION_INSTANCES + ", " +
                                Causation.CAUSATION_INSTANCE_LENGTH + ", " + (NUM_CAUSATIONS + 1) + " ]");
            printWriter.print("y_test_seq = [");
            String y_test = "";
            for (int i = 0, j = CausationTestingInstances.size(); i < j; i++)
            {
               y_test += "\n";
               CausationInstance instance = CausationTestingInstances.get(i);
               for (int k = 0; k < instance.events.length; k++)
               {
                  if ((instance.effectEventIndex == k) && instance.valid && (instance.causation != null))
                  {
                     y_test += oneHot(instance.causation.ID, NUM_CAUSATIONS + 1);
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
      }
      else
      {
         // GA.
         CausationsGA = new EvolveCausations(Causations, CausationTrainingInstances, random);
      }

      // Run.
      if (LEARNER.equals("GA"))
      {
         // Train GA.
         CausationsGA.train();

         // Test GA.
         List<Boolean> results   = CausationsGA.test(CausationTestingInstances);
         int           testOK    = 0;
         int           testTotal = CausationTestingInstances.size();
         for (Boolean result : results)
         {
            if (result)
            {
               testOK++;
            }
         }
         float pct = 0.0f;
         if (testTotal > 0)
         {
            pct = ((float)testOK / (float)testTotal) * 100.0f;
         }
         DecimalFormat df = new DecimalFormat("0.0");
         if (Verbose)
         {
            System.out.println("Writing results to " + GA_RESULTS_FILENAME);
         }
         try (PrintWriter writer = new PrintWriter(GA_RESULTS_FILENAME))
            {
               writer.println("{\"test_correct_predictions\":\"" + testOK + "\",\"test_total_predictions\":\"" +
                              testTotal + "\",\"test_pct\":\"" + df.format(pct) + "\"}");
            }
            catch (IOException e)
            {
               System.err.println("Cannot write results to file " + GA_RESULTS_FILENAME + ": " + e.getMessage());
            }

         System.out.print("Test correct/total = " + testOK + "/" + testTotal);
         System.out.println(" (" + df.format(pct) + "%)");
      }
      else
      {
         // Run neural network.
         String pythonFilename = null;
         if (LEARNER.equals("LSTM") || LEARNER.equals("SimpleRNN"))
         {
            pythonFilename = RNN_FILENAME;
         }
         else if (LEARNER.equals("Attention"))
         {
            pythonFilename = ATTENTION_FILENAME;
         }
         else
         {
            pythonFilename = NN_FILENAME;
         }
         try
         {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(pythonFilename);
            if (in == null)
            {
               System.err.println("Cannot access " + pythonFilename);
               System.exit(1);
            }
            File             pythonScript = new File(pythonFilename);
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
            System.err.println("Cannot create " + pythonFilename);
            System.exit(1);
         }
         int n = 5;
         if (Verbose)
         {
            n = 4;
         }
         String[] opts = new String[n + (NUM_HIDDEN_NEURONS.size() * 2)];
         opts[0]       = "python";
         opts[1]       = pythonFilename;
         opts[2]       = "-e";
         opts[3]       = (NUM_EPOCHS + "");
         int k = 4;
         for (int i = 0, j = NUM_HIDDEN_NEURONS.size(); i < j; i++, k += 2)
         {
            opts[k]     = "-h";
            opts[k + 1] = (NUM_HIDDEN_NEURONS.get(i) + "");
         }
         if (!Verbose)
         {
            opts[k] = "-q";
         }
         ProcessBuilder processBuilder = new ProcessBuilder(opts);
         processBuilder.inheritIO();
         int exitCode = 0;
         try
         {
            Process process = processBuilder.start();
            exitCode = process.waitFor();
         }
         catch (IOException e)
         {
            System.err.println("Cannot run " + pythonFilename);
            System.exit(1);
         }
         catch (InterruptedException e) {}
         if (exitCode != 0)
         {
            System.err.println(pythonFilename + " exited with error code " + exitCode);
            System.exit(exitCode);
         }

         // Fetch the results.
         int    train_correct_predictions = 0;
         int    train_total_predictions   = 0;
         float  train_pct = 0.0f;
         int    test_correct_predictions = 0;
         int    test_total_predictions   = 0;
         float  test_pct        = 0.0f;
         String resultsFilename = null;
         if (LEARNER.equals("NN"))
         {
            resultsFilename = NN_RESULTS_FILENAME;
         }
         else if (LEARNER.equals("Attention"))
         {
            resultsFilename = ATTENTION_RESULTS_FILENAME;
         }
         else
         {
            resultsFilename = RNN_RESULTS_FILENAME;
         }
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(resultsFilename)));
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
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               String value = jObj.getString("train_correct_predictions");
               if ((value == null) || value.isEmpty())
               {
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               train_correct_predictions = Integer.parseInt(value);
               value = jObj.getString("train_total_predictions");
               if ((value == null) || value.isEmpty())
               {
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               train_total_predictions = Integer.parseInt(value);
               value = jObj.getString("train_pct");
               if ((value == null) || value.isEmpty())
               {
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               train_pct = Float.parseFloat(value);
               value     = jObj.getString("test_correct_predictions");
               if ((value == null) || value.isEmpty())
               {
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               test_correct_predictions = Integer.parseInt(value);
               value = jObj.getString("test_total_predictions");
               if ((value == null) || value.isEmpty())
               {
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               test_total_predictions = Integer.parseInt(value);
               value = jObj.getString("test_pct");
               if ((value == null) || value.isEmpty())
               {
                  System.err.println("Error parsing results file " + resultsFilename);
                  System.exit(1);
               }
               test_pct = Float.parseFloat(value);
            }
            else
            {
               System.err.println("Cannot read results file " + resultsFilename);
               System.exit(1);
            }
            br.close();
         }
         catch (Exception e)
         {
            System.err.println("Cannot read results file " + resultsFilename + ":" + e.getMessage());
            System.exit(1);
         }
         System.out.println("Train correct/total = " + train_correct_predictions +
                            "/" + train_total_predictions + " (" + train_pct + "%)");
         System.out.println("Test correct/total = " + test_correct_predictions +
                            "/" + test_total_predictions + " (" + test_pct + "%)");
      }
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

   static private void permuteHelper(int[] num, int level, boolean[] used, List < List < Integer >> output, ArrayList<Integer> temp)
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


   // Print parameters.
   public static void printParameters()
   {
      System.out.println("NUM_EVENT_TYPES = " + Causation.NUM_EVENT_TYPES);
      System.out.println("NUM_CAUSE_EVENT_TYPES = " + Causation.NUM_CAUSE_EVENT_TYPES);
      System.out.println("EFFECT_EVENT_TYPE = " + Causation.EFFECT_EVENT_TYPE);
      System.out.println("NUM_CAUSATIONS = " + NUM_CAUSATIONS);
      System.out.println("MAX_CAUSE_EVENTS = " + Causation.MAX_CAUSE_EVENTS);
      System.out.println("EVENT_OMISSION_PROBABILITY = " + CausationInstance.EVENT_OMISSION_PROBABILITY);
      System.out.println("MAX_INTERVENING_EVENTS = " + Causation.MAX_INTERVENING_EVENTS);
      System.out.println("MAX_VALID_INTERVENING_EVENTS = " + Causation.MAX_VALID_INTERVENING_EVENTS);
      System.out.println("VALID_INTERVENING_EVENTS_PROBABILITY = " + CausationInstance.VALID_INTERVENING_EVENTS_PROBABILITY);
      System.out.println("CAUSATION_INSTANCE_LENGTH = " + Causation.CAUSATION_INSTANCE_LENGTH);
      System.out.println("NUM_TRAINING_CAUSATION_INSTANCES = " + NUM_TRAINING_CAUSATION_INSTANCES);
      System.out.println("NUM_TESTING_CAUSATION_INSTANCES = " + NUM_TESTING_CAUSATION_INSTANCES);
      System.out.println("CAUSATION_INSTANCE_LENGTH = " + Causation.CAUSATION_INSTANCE_LENGTH);
      System.out.println("LEARNER = " + LEARNER);
      System.out.print("NUM_HIDDEN_NEURONS = {");
      for (int i = 0, j = NUM_HIDDEN_NEURONS.size(); i < j; i++)
      {
         System.out.print(NUM_HIDDEN_NEURONS.get(i));
         if (i < j - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.println("NUM_EPOCHS = " + NUM_EPOCHS);
      EvolveCausations.printParameters();
      System.out.println("RANDOM_SEED = " + RANDOM_SEED);
   }
}
