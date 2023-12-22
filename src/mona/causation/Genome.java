// For conditions of distribution and use, see copyright notice in CausationLearning.java

/*
 * Genome.
 */

package mona.causation;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Genome
{
   // Genes.
   ArrayList<Gene> genes;

   // Mutation rate.
   float mutationRate;

   // Random numbers.
   int    randomSeed;
   Random randomizer;

   // Constructor.
   Genome(float mutationRate, int randomSeed)
   {
      this.mutationRate = mutationRate;
      this.randomSeed   = randomSeed;
      randomizer        = new Random(randomSeed);
      genes             = new ArrayList<Gene>();
   }


   // Mutate.
   void mutate()
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).mutate();
      }
   }


   // Randomly merge genome values from given genome.
   void meldValues(Genome from1, Genome from2)
   {
      Gene gene;

      for (int i = 0; i < genes.size(); i++)
      {
         gene = genes.get(i);
         if (randomizer.nextBoolean())
         {
            gene.copyValue(from1.genes.get(i));
         }
         else
         {
            gene.copyValue(from2.genes.get(i));
         }
      }
   }


   // Copy genome values from given genome.
   void copyValues(Genome from)
   {
      Gene gene;

      for (int i = 0; i < genes.size(); i++)
      {
         gene = genes.get(i);
         gene.copyValue(from.genes.get(i));
      }
   }


   // Get genome as key-value pairs.
   void getKeyValues(ArrayList<String> keys, ArrayList<Object> values)
   {
      Gene gene;

      keys.clear();
      values.clear();
      for (int i = 0; i < genes.size(); i++)
      {
         gene = genes.get(i);
         keys.add(new String(gene.name));
         switch (gene.type)
         {
         case INTEGER_VALUE:
            values.add(gene.ivalue + "");
            break;

         case FLOAT_VALUE:
            values.add(gene.fvalue + "");
            break;
         }
      }
   }


   // Load values.
   void loadValues(DataInputStream reader) throws IOException
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).loadValue(reader);
      }
   }


   // Save values.
   void saveValues(PrintWriter writer) throws IOException
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).saveValue(writer);
      }
      writer.flush();
   }


   // Print genome.
   void print()
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).print();
      }
   }
}
