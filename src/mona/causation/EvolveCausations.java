// For conditions of distribution and use, see copyright notice in CausationLearning.java

/*
 * Evolve causations.
 */

package mona.causation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvolveCausations
{
   // Parameters.
   public static int   GENERATIONS         = 10;
   public static int   POPULATION_SIZE     = 20;
   public static int   FIT_POPULATION_SIZE = 10;
   public static float MUTATION_RATE       = 0.25f;

   // Causations.
   ArrayList<Causation> Causations;

   // Causation instances.
   ArrayList<CausationInstance> CausationInstances;

   // Causation populations.
   Population[] Populations;

   // Generation.
   int Generation;

   // Log.
   public static boolean LOG = true;

   // Constructor.
   public EvolveCausations(ArrayList<Causation> causations,
                           ArrayList<CausationInstance> causationInstances, Random randomizer)
   {
      Causations         = causations;
      CausationInstances = causationInstances;
      Populations        = new Population[Causations.size()];
      for (int i = 0; i < Populations.length; i++)
      {
         Populations[i] = new Population(i, randomizer);
      }
      Generation = 0;
   }


   // Train.
   public void train()
   {
      log("Parameters:");
      log("  GENERATIONS=" + GENERATIONS);
      log("  POPULATION_SIZE=" + POPULATION_SIZE);
      log("  FIT_POPULATION_SIZE=" + FIT_POPULATION_SIZE);
      log("  MUTATION_RATE=" + MUTATION_RATE);

      // Evolution loop.
      log("Begin evolve:");
      for (Generation = 0; Generation < GENERATIONS; Generation++)
      {
         log("Generation=" + Generation);
         for (int i = 0; i < Populations.length; i++)
         {
            log("Population=" + i);
            Populations[i].evolve(Generation);
         }
      }
      log("End evolve");
   }


   // Test.
   public ArrayList<Boolean> test(ArrayList<CausationInstance> causationInstances)
   {
      CausationInstances = causationInstances;
      ArrayList<Boolean> results = new ArrayList<Boolean>();
      log("Begin testing:");
      for (CausationInstance instance : causationInstances)
      {
         log("Test instance, causation ID=" + instance.causation.ID + ", valid=" + instance.valid);
         boolean result = true;
         for (int i = 0; i < Populations.length; i++)
         {
            String message = "  Test population, causation ID=" + Populations[i].causationID;
            if (!Populations[i].test(instance))
            {
               log(message + ", result=false");
               result = false;
               break;
            }
            log(message + ", result=true");
         }
         log("result=" + result);
         results.add(result);
      }
      log("End testing");
      return(results);
   }


   // Population.
   public class Population
   {
      int               causationID;
      ArrayList<Member> members;
      int               IDdispenser;
      Random            randomizer;

      public Population(int causationID, Random randomizer)
      {
         this.causationID = causationID;
         this.randomizer  = randomizer;
         members          = new ArrayList<Member>();
         IDdispenser      = 0;
         ArrayList<CausationInstance> instances = new ArrayList<CausationInstance>();
         for (int i = 0, j = CausationInstances.size(); i < j; i++)
         {
            CausationInstance instance = CausationInstances.get(i);
            if (instance.causation.ID == causationID)
            {
               instances.add(instance);
            }
         }
         for (int i = 0, j = instances.size(), k = randomizer.nextInt(j); i < POPULATION_SIZE; i++, k = (k + 1) % j)
         {
            members.add(new Member(IDdispenser++, instances.get(k), 0, randomizer));
         }
      }


      // Evolve.
      void evolve(int generation)
      {
         // Evaluate member fitness.
         evaluate();

         // Prune unfit members.
         prune();

         // Create new members by mutation.
         mutate();
      }


      // Evaluate member fitness.
      void evaluate()
      {
         log("Evaluate:");
         for (int i = 0; i < POPULATION_SIZE; i++)
         {
            Member member = members.get(i);
            member.evaluate();
            log("    member=" + i + ", " + member.getInfo());
         }
      }


      // Prune unfit members.
      void prune()
      {
         log("Prune:");
         Member[] fitPopulation = new Member[FIT_POPULATION_SIZE];
         for (int i = 0; i < FIT_POPULATION_SIZE; i++)
         {
            float max = 0.0f;
            int   m   = -1;
            for (int j = 0; j < POPULATION_SIZE; j++)
            {
               Member member = members.get(j);
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
            Member member = members.get(m);
            members.set(m, null);
            fitPopulation[i] = member;
            log("    member=" + member.ID + ", " + member.getInfo());
         }
         members.clear();
         for (int i = 0; i < FIT_POPULATION_SIZE; i++)
         {
            members.add(fitPopulation[i]);
         }
      }


      // Mutate members.
      void mutate()
      {
         log("Mutate:");
         if (FIT_POPULATION_SIZE > 0)
         {
            for (int i = 0, j = randomizer.nextInt(FIT_POPULATION_SIZE), k = FIT_POPULATION_SIZE;
                 i < FIT_POPULATION_SIZE && k < POPULATION_SIZE; i++, j = (j + 1) % FIT_POPULATION_SIZE, k++)
            {
               Member member = members.get(j);
               Member child  = new Member(IDdispenser++, Generation + 1, member);
               members.add(child);
               log("    member=" + (members.size() - 1) + ", " + child.getInfo());
            }
         }
      }


      // Test population fitness.
      boolean test(CausationInstance instance)
      {
         return(members.get(0).test(instance));
      }


      // Print.
      void print()
      {
         log("Population:");
         for (int i = 0; i < POPULATION_SIZE; i++)
         {
            members.get(i).print();
         }
      }
   };

   // Population member.
   public class Member
   {
      int    ID;
      int    causationID;
      int    generation;
      Random randomizer;
      float  fitness;

      // Genome.
      CausationGenome genome;

      // Constructors.
      Member(int ID, CausationInstance causationInstance, int generation, Random randomizer)
      {
         this.ID         = ID;
         causationID     = causationInstance.causation.ID;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create genome.
         genome = new CausationGenome(causationInstance, randomizer);
      }


      // Construct mutation of given member.
      Member(int ID, int generation, Member member)
      {
         this.ID         = ID;
         causationID     = member.causationID;
         this.generation = generation;
         this.randomizer = member.randomizer;
         fitness         = 0.0f;

         // Copy and mutate genome.
         genome = new CausationGenome(randomizer);
         genome.copyGenome(member.genome);
         mutate();

         // Evaluate mutant.
         evaluate();
      }


      // Evaluate.
      void evaluate()
      {
         ArrayList<Integer> genomeCauseEvents = new ArrayList<Integer>();
         for (int i = 1, j = genome.genes.size(); i < j; i++)
         {
            Gene gene = genome.genes.get(i);
            for (int k = 0; k < gene.ivalue; k++)
            {
               genomeCauseEvents.add(i - 1);
            }
         }
         List < List < Integer >> causationPermutations =
            CausationLearning.permuteList(genomeCauseEvents);
         fitness = 0.0f;
         for (CausationInstance instance : CausationInstances)
         {
            boolean match = false;
            for (List<Integer> permutation : causationPermutations)
            {
               int[] causeEvents = new int[permutation.size()];
               for (int i = 0; i < causeEvents.length; i++)
               {
                  causeEvents[i] = permutation.get(i);
               }
               if ((match = matchEventStream(instance.events, 0, causeEvents)))
               {
                  break;
               }
            }
            if (match)
            {
               if (instance.valid && (instance.causation.ID == causationID))
               {
                  fitness += 1.0f;
               }
               else
               {
                  fitness -= 1.0f;
               }
            }
            else
            {
               if (instance.valid && (instance.causation.ID == causationID))
               {
                  fitness -= 1.0f;
               }
               else
               {
                  fitness += 1.0f;
               }
            }
         }
      }


      // Test.
      boolean test(CausationInstance instance)
      {
         ArrayList<Integer> genomeCauseEvents = new ArrayList<Integer>();
         for (int i = 1, j = genome.genes.size(); i < j; i++)
         {
            Gene gene = genome.genes.get(i);
            for (int k = 0; k < gene.ivalue; k++)
            {
               genomeCauseEvents.add(i - 1);
            }
         }
         List < List < Integer >> causationPermutations =
            CausationLearning.permuteList(genomeCauseEvents);
         boolean match = false;
         for (List<Integer> permutation : causationPermutations)
         {
            int[] causeEvents = new int[permutation.size()];
            for (int i = 0; i < causeEvents.length; i++)
            {
               causeEvents[i] = permutation.get(i);
            }
            if ((match = matchEventStream(instance.events, 0, causeEvents)))
            {
               break;
            }
         }
         if (match)
         {
            if (instance.valid && (instance.causation.ID == causationID))
            {
               return(true);
            }
            else
            {
               return(false);
            }
         }
         else
         {
            if (instance.valid && (instance.causation.ID == causationID))
            {
               return(false);
            }
            else
            {
               return(true);
            }
         }
      }


      // Match cause event order in causation instance.
      boolean matchEventStream(int[] instanceEvents, int startIndex, int[] causeEvents)
      {
         if (instanceEvents[startIndex] == Causation.EFFECT_EVENT_TYPE)
         {
            return(false);
         }
         int maxValidInterveningEvents = genome.genes.get(0).ivalue;
         for (int i = 0, j = startIndex; i < causeEvents.length; i++)
         {
            int k = j;
            for ( ; instanceEvents[k] != Causation.EFFECT_EVENT_TYPE; k++)
            {
               if (causeEvents[i] == instanceEvents[k])
               {
                  if ((i == 0) || ((k - j) <= maxValidInterveningEvents))
                  {
                     if (i == causeEvents.length - 1)
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
         return(matchEventStream(instanceEvents, startIndex + 1, causeEvents));
      }


      // Mutate.
      void mutate()
      {
         // Mutate MAX_VALID_INTERVENING_EVENTS.
         Gene interveningEvents = genome.genes.get(0);

         if (randomizer.nextFloat() < MUTATION_RATE)
         {
            boolean increase = randomizer.nextBoolean();
            if (interveningEvents.ivalue == 0)
            {
               increase = true;
            }
            else if (interveningEvents.ivalue == Causation.MAX_INTERVENING_EVENTS)
            {
               increase = false;
            }
            if (increase)
            {
               interveningEvents.ivalue++;
            }
            else
            {
               interveningEvents.ivalue--;
            }
         }

         // Mutate causation events.
         if (randomizer.nextFloat() < MUTATION_RATE)
         {
            if (genome.genes.size() > 1)
            {
               int n = 0;
               for (int i = 1, j = genome.genes.size(); i < j; i++)
               {
                  n += genome.genes.get(i).ivalue;
               }
               for (int i = 0, j = genome.genes.size() - 1, k = randomizer.nextInt(j); i < j; i++, k = (k + 1) % j)
               {
                  Gene gene = genome.genes.get(k + 1);
                  if (n == 0)
                  {
                     gene.ivalue++;
                     break;
                  }
                  else if (n == Causation.MAX_CAUSE_EVENTS)
                  {
                     if (gene.ivalue > 0)
                     {
                        gene.ivalue--;
                        break;
                     }
                  }
                  else
                  {
                     if (gene.ivalue == 0)
                     {
                        gene.ivalue++;
                        break;
                     }
                     else
                     {
                        if (randomizer.nextBoolean())
                        {
                           gene.ivalue++;
                        }
                        else
                        {
                           gene.ivalue--;
                        }
                        break;
                     }
                  }
               }
            }
         }
      }


      // Print.
      void print()
      {
         log(getInfo());
         String info = "genome: ";
         info += genome.genes.get(0).name + "=" + genome.genes.get(0).ivalue;
         info += ", CAUSATION_EVENTS: {";
         for (int i = 1, j = genome.genes.size(); i < j; i++)
         {
            info += genome.genes.get(i).ivalue;
            if (i < j - 1)
            {
               info += ", ";
            }
         }
         info += "}";
         log(info);
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
      // Constructors.
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


      CausationGenome(Random randomizer)
      {
         super(MUTATION_RATE, randomizer.nextInt());
      }


      // Copy genome.
      void copyGenome(Genome genome)
      {
         genes.clear();
         for (Gene gene : genome.genes)
         {
            genes.add(gene.copy());
         }
      }
   }

   // Logging.
   static void log(String message)
   {
      if (LOG)
      {
         System.out.println(message);
      }
   }


   // Print parameters.
   public static void printParameters()
   {
      log("GENERATIONS = " + GENERATIONS);
      log("POPULATION_SIZE = " + POPULATION_SIZE);
      log("FIT_POPULATION_SIZE = " + FIT_POPULATION_SIZE);
      log("MUTATION_RATE = " + MUTATION_RATE);
   }
}
