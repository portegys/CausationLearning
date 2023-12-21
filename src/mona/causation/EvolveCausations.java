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
   public static int GENERATIONS = 10;
   public static int POPULATION_SIZE = 20;
   public static int FIT_POPULATION_SIZE = 10;   
   public static float MUTATION_RATE        = 0.25f;
   
   // Generations.
   int Generation;
   int Generations;

   // Causation populations.
   ArrayList<Population> Populations;
   
   // Log output.
   boolean Log = false;

   // Print population properties.
   boolean PrintProperties = false;

   // Print evolution statistics.
   boolean PrintStatistics = false;

   // Constructor.
   public EvolveCausations(ArrayList<CausationInstance> causationTrainingInstances,
		   ArrayList<CausationInstance> causationTestingInstances)
   {
	   Populations = new ArrayList<Population>();
	      Generation     = 0;
	      Generations    = -1;
   }

   // Run.
   public void run()
   {
      // Log run.
      log("Initializing evolve:");
      log("  Options:");
      log("    generations=" + Generations);
      log("    MutationRate=" + MUTATION_RATE);
      log("    RandomSeed=" + RANDOM_SEED);
      log("  Parameters:");
      log("    FORAGER_FIT_POPULATION_SIZE=" + EvolveCommon.FORAGER_FIT_POPULATION_SIZE);
      log("    FORAGER_NUM_MUTANTS=" + EvolveCommon.FORAGER_NUM_MUTANTS);
      log("    FORAGER_NUM_OFFSPRING=" + EvolveCommon.FORAGER_NUM_OFFSPRING);

      // Print population properties?
      if (PrintProperties)
      {
         printProperties();
         return;
      }

      // Print evolution statistics?
      if (PrintStatistics)
      {
         printStatistics();
         return;
      }

      // Evolution loop.
      log("Begin evolve:");
      for (Generations += Generation; Generation < Generations; Generation++)
      {
         log("Generation=" + Generation);
         evolve(Generation);
      }

      log("End evolve");
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
      int    i, j, step;
      String logEntry;

      log("Evaluate:");
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         logEntry = "    member=" + i + ", " + Populations.get(i).getInfo();
         log(logEntry);
      }
   }

   // Prune unfit members.
   void prune()
   {
      double min, max, d;
      int    i, j, m;

      Member member;

      log("Select:");
      Member[] fitPopulation = new Member[FIT_POPULATION_SIZE];
      max = 0.0;
      for (i = 0; i < FIT_POPULATION_SIZE; i++)
      {
         m = -1;
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
   }


   // Mutate members.
   void mutate()
   {
      int i, j;

      Member member, mutant;

      log("Mutate:");
      for (i = 0; i < EvolveCommon.FORAGER_NUM_MUTANTS; i++)
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
   }

   // Print population properties.
   void printProperties()
   {
      int i;

      System.out.println("Population properties:");;
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         Populations.get(i).printProperties();
      }
   }


   // Print evolution statistics.
   void printStatistics()
   {
      int i;

      System.out.println("Evolution statistics:");
      System.out.println("Generation\tFittest");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + Fittest[i]);
      }
      System.out.println("Generation\tAverage");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + Average[i]);
      }
   }


   // Logging.
   void log(String message)
   {
	   if (Log)
	   {
		   System.out.println(message);
	   }
   }

   // Population.
   public class Population
   {
	   ArrayList<Member> members;
	   
	   public Population(Random randomizer)
	   {
		   members = new ArrayList<Member>();
		   for (int i = 0; i < POPULATION_SIZE; i++)
		   {
			   members.add(new Member(idDispenser++, 0, randomizer));
		   }
	   }
   };
   
   // Population member.
   public class Member
   {
	   int id;
      int    generation;
      double fitness;

      // Genome.
      CausationGenome genome;

      // Constructors.
      Member(int id, int generation, Random randomizer)
      {
    	  this.id = id;
         this.generation = generation;
         fitness         = 0.0;

         // Create genome.
         genome = new CausationGenome(randomizer);
      }


      // Construct mutation of given member.
      Member(int id, int generation, Random randomizer, Member member)
      {
    	  this.id = id;
         this.generation = generation;
         fitness         = 0.0;

         // Create and mutate genome.
         genome.copyValues(member.genome);
         genome.mutate();
      }

      // Print properties.
      void printProperties()
      {
         System.out.println(getInfo());
         System.out.println("parameters:");
         genome.print();
      }

      // Get information.
      String getInfo()
      {
         return("id=" + id +
                ", fitness=" + fitness +
                ", generation=" + generation);
      }
   }
   
   // Causation genome.
   public static class CausationGenome extends Genome
   {
      // Constructor.
      CausationGenome(Random randomizer)
      {
         super(MUTATION_RATE, randomizer.nextInt());

         // MAX_VALID_INTERVENING_EVENTS.
         genes.add(new Gene("MAX_VALID_INTERVENING_EVENTS", 
        		 randomizer.nextInt(Causation.MAX_INTERVENING_EVENTS + 1), 
            		0, Causation.MAX_INTERVENING_EVENTS, 1, MUTATION_RATE, randomizer.nextInt()));
         
         // Causation events.
         for (int i = 0; i < Causation.NUM_EVENT_TYPES; i++)
         {
             genes.add(new Gene("CAUSATION_EVENT_" + i, randomizer.nextInt(Causation.MAX_CAUSE_EVENTS + 1),
                    		 0, Causation.MAX_CAUSE_EVENTS, 1, MUTATION_RATE, randomizer.nextInt()));
         }
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
