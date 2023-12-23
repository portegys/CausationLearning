// For conditions of distribution and use, see copyright notice in CausationLearning.java

/*
 * Evolve causations.
 */

package mona.causation;

import java.util.*;
import java.io.*;

public class EvolveCausations
{
   // Parameters.
   public static int   GENERATIONS         = 10;
   public static int   POPULATION_SIZE     = 20;
   public static int   FIT_POPULATION_SIZE = 10;
   public static float MUTATION_RATE       = 0.25f;

   // Number of causations.
   int NumCausations;

   // Training and test causation instances.
   ArrayList<CausationInstance> CausationTrainingInstances;
   ArrayList<CausationInstance> CausationTestingInstances;

   // Causation populations.
   Population[] Populations;

   // Generations.
   int Generation;
   int Generations;

   // Log.
   public static boolean LOG = false;

   // Constructor.
   public EvolveCausations(int numCausations, ArrayList<CausationInstance> causationTrainingInstances,
                           ArrayList<CausationInstance> causationTestingInstances, Random randomizer)
   {
      NumCausations = numCausations;
      CausationTrainingInstances = causationTrainingInstances;
      CausationTestingInstances  = causationTestingInstances;
      Populations = new Population[NumCausations];
      for (int i = 0; i < numCausations; i++)
      {
         Populations[i] = new Population(randomizer);
      }
      Generation  = 0;
      Generations = -1;
   }


   // Run.
   public void run()
   {
      // Log run.
      log("Parameters:");
      log("  GENERATIONS=" + GENERATIONS);
      log("  POPULATION_SIZE=" + POPULATION_SIZE);
      log("  FIT_POPULATION_SIZE=" + FIT_POPULATION_SIZE);
      log("  MUTATION_RATE=" + MUTATION_RATE);

      // Evolution loop.
      log("Begin evolve:");
      for (Generation = 0; Generation < Generations; Generation++)
      {
         log("Generation=" + Generation);
         for (int i = 0; i < NumCausations; i++)
         {
            Populations[i].evolve(Generation);
         }
      }
      log("End evolve");
   }


   // Population.
   public class Population
   {
      ArrayList<Member> members;
      int               IDdispenser;

      public Population(Random randomizer)
      {
         members     = new ArrayList<Member>();
         IDdispenser = 0;
         for (int i = 0; i < POPULATION_SIZE; i++)
         {
            members.add(new Member(IDdispenser++, 0, randomizer));
         }
      }


      // Evolve.
      void evolve(int generation)
      {
         // Evaluate member fitness.
         evaluate(generation);

         // Prune unfit members.
         prune();

         // Create new members by mutation.
         mutate();
      }


      // Evaluate member fitness.
      void evaluate(int generation)
      {
         log("Evaluate:");
         for (int i = 0; i < POPULATION_SIZE; i++)
         {
            // eval fitness.
            log("    member=" + i + ", " + members.get(i).getInfo());
         }
      }


      // Prune unfit members.
      void prune()
      {
         log("Prune:");
         /*
         Member[] fitPopulation = new Member[FIT_POPULATION_SIZE];
         max = 0.0;
         for (int i = 0; i < FIT_POPULATION_SIZE; i++)
         {
            int m = -1;
            for (j = 0; j < POPULATION_SIZE; j++)
            {
               member = ForagerPopulation[j];
               if (member == null)
               {
                  continue;
               }
               if ((m == -1) || (member.fitness > max))
               {
                  m   = j;
                  max = member.fitness;
               }
            }
            member = ForagerPopulation[m];
            ForagerPopulation[m] = null;
            fitPopulation[i]     = member;
            log("    " + member.getInfo());
         }
         for (i = 0; i < EvolveCommon.FORAGER_POPULATION_SIZE; i++)
         {
            if (ForagerPopulation[i] != null)
            {
               ForagerPopulation[i].clear();
               ForagerPopulation[i] = null;
            }
         }
         d = 0.0;
         for (i = 0; i < EvolveCommon.FORAGER_FIT_POPULATION_SIZE; i++)
         {
            ForagerPopulation[i] = fitPopulation[i];
            fitPopulation[i]     = null;
            d += ForagerPopulation[i].fitness;
         }
         ForagerFittest[Generation] = ForagerPopulation[0].fitness;
         ForagerAverage[Generation] = d / (double)EvolveCommon.FORAGER_FIT_POPULATION_SIZE;
         */
      }


      // Mutate members.
      void mutate()
      {
         log("Mutate:");
         /*
         for (int i = 0; i < EvolveCommon.FORAGER_NUM_MUTANTS; i++)
         {
            // Select a fit member to mutate.
            j      = Randomizer.nextInt(EvolveCommon.FORAGER_FIT_POPULATION_SIZE);
            member = ForagerPopulation[j];

            // Create mutant member.
            mutant = new EvolveCommon.Member(member, member.generation + 1, Randomizer);
            ForagerPopulation[EvolveCommon.FORAGER_FIT_POPULATION_SIZE + i] = mutant;
            log("    member=" + j + ", " + member.getInfo() +
                " -> member=" + (EvolveCommon.FORAGER_FIT_POPULATION_SIZE + i) +
                ", " + mutant.getInfo());
         }
         */
      }

      // Print population properties.
      void printProperties()
      {
         System.out.println("Population properties:");
         printProperties();
      }

      // Print evolution statistics.
      void printStatistics()
      {
    	  /*
         System.out.println("Evolution statistics:");
         System.out.println("Generation\tFittest");
         for (int i = 0; i < Generation; i++)
         {
            System.out.println(i + "\t\t" + Fittest[i]);
         }
         System.out.println("Generation\tAverage");
         for (int i = 0; i < Generation; i++)
         {
            System.out.println(i + "\t\t" + Average[i]);
         }
         */
      }
   };

   // Population member.
   public class Member
   {
      int   ID;
      int   generation;
      float fitness;

      // Genome.
      CausationGenome genome;

      // Constructors.
      Member(int ID, int generation, Random randomizer)
      {
         this.ID         = ID;
         this.generation = generation;
         fitness         = 0.0f;

         // Create genome.
         if (CausationTrainingInstances.size() > 0)
         {
            int n = randomizer.nextInt(CausationTrainingInstances.size());
            genome = new CausationGenome(CausationTrainingInstances.get(n), randomizer);
         }
         else
         {
            genome = new CausationGenome(null, randomizer);
         }
      }


      // Construct mutation of given member.
      Member(int ID, int generation, Random randomizer, Member member)
      {
         this.ID         = ID;
         this.generation = generation;
         fitness         = 0.0f;

         // Create and mutate genome.
         genome.copyValues(member.genome);
         genome.mutate();
      }


      // Print properties.
      void printProperties()
      {
         System.out.println(getInfo());
         System.out.println("genome:");
         genome.print();
      }


      // Get information.
      String getInfo()
      {
         return("ID=" + ID + ", fitness=" + fitness + ", generation=" + generation);
      }
   }

   // Causation genome.
   public static class CausationGenome extends Genome
   {
      // Constructor.
      CausationGenome(CausationInstance causationInstance, Random randomizer)
      {
         super(MUTATION_RATE, randomizer.nextInt());

         // MAX_VALID_INTERVENING_EVENTS.
         genes.add(new Gene("MAX_VALID_INTERVENING_EVENTS",
                            randomizer.nextInt(Causation.MAX_INTERVENING_EVENTS + 1),
                            0, Causation.MAX_INTERVENING_EVENTS, 1, MUTATION_RATE, randomizer.nextInt()));

         // Causation events.
         for (int i = 0; i < Causation.NUM_EVENT_TYPES; i++)
         {
            genes.add(new Gene("CAUSATION_EVENT_" + i, 0, 0,
                               Causation.MAX_CAUSE_EVENTS, 1, MUTATION_RATE, randomizer.nextInt()));
         }
         if (causationInstance != null)
         {
            int n = causationInstance.effectEventIndex;
            if (n > 0)
            {
               int[] events = new int[n];
               for (int i = 0; i < n; i++)
               {
                  events[i] = causationInstance.events[i];
               }
               int j = randomizer.nextInt(Causation.MAX_CAUSE_EVENTS + 1);
               for (int i = 0; i < j && i < n; i++)
               {
                  int k = randomizer.nextInt(n);
                  for (int q = 0; q < n; q++, k = (k + 1) % n)
                  {
                     if (events[k] != -1)
                     {
                        genes.get(events[k] + 1).ivalue++;
                        events[k] = -1;
                     }
                  }
               }
            }
         }
      }
   }

   // Logging.
   void log(String message)
   {
      if (LOG)
      {
         System.out.println(message);
      }
   }


   // Print parameters.
   public static void printParameters()
   {
      System.out.println("GENERATIONS = " + GENERATIONS);
      System.out.println("POPULATION_SIZE = " + POPULATION_SIZE);
      System.out.println("FIT_POPULATION_SIZE = " + FIT_POPULATION_SIZE);
      System.out.println("MUTATION_RATE = " + MUTATION_RATE);
   }
}
