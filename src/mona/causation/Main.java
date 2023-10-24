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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.UIManager;

public class Main
{
   // Version.
   public static final String VERSION = "1.0";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java mona.causation.Main\n" +
      "        [-numEventTypes <quantity> (default=" + Parameters.NUM_EVENT_TYPES + ")]\n" +
      "        [-numCausations <quantity> (default=" + Parameters.NUM_CAUSATIONS + ")]\n" +
      "        [-maxCauseEvents <quantity> (default=" + Parameters.MAX_CAUSE_EVENTS + ")]\n" +
      "        [-maxIntervening <quantity> (default=" + Parameters.MAX_INTERVENING_EVENTS + ")]\n" +
      "        [-eventStreamLength <length> (default=" + Parameters.EVENT_STREAM_LENGTH + ")]\n" +
      "        [-randomSeed <random number seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +      
      "  Print parameters:\n" +
      "    java mona.causation.Main -printParameters\n" +
      "  Version:\n" +
      "    java mona.causation.Main -version\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=error";

   // Random numbers.
   public static int    randomSeed = DEFAULT_RANDOM_SEED;
   public static Random random;

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
               Parameters.NUM_EVENT_TYPES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numEventTypes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_EVENT_TYPES < 0)
            {
               System.err.println("Invalid numEventTypes option");
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
               Parameters.NUM_CAUSATIONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numCausations option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_CAUSATIONS < 0)
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
               Parameters.MAX_CAUSE_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxCauseEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.MAX_CAUSE_EVENTS < 0)
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
               Parameters.MAX_INTERVENING_EVENTS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxInterveningEvents option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.MAX_INTERVENING_EVENTS < 0)
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
               Parameters.EVENT_STREAM_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid eventStreamLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.EVENT_STREAM_LENGTH < 0)
            {
               System.err.println("Invalid eventStreamLength option");
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
             Parameters.print();
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

      System.exit(0);
   }
}
