// For conditions of distribution and use, see copyright notice in CausationLearning.java

/*
 * Gene.
 */

package mona.causation;

import java.util.Random;

public class Gene
{
   // Mutation rate.
   float mutationRate;

   // Random numbers.
   int    randomSeed;
   Random randomizer;

   // Value types.
   enum VALUE_TYPE
   {
      INTEGER_VALUE, FLOAT_VALUE
   };

   // Mutable value.
   VALUE_TYPE type;
   String     name;
   int        ivalue, imin, imax, idelta;
   float      fvalue, fmin, fmax, fdelta;

   // Constructors.
   Gene(float mutationRate, int randomSeed)
   {
      type              = VALUE_TYPE.FLOAT_VALUE;
      name              = null;
      ivalue            = imin = imax = idelta = 0;
      fvalue            = fmin = fmax = fdelta = 0.0f;
      this.mutationRate = mutationRate;
      this.randomSeed   = randomSeed;
      randomizer        = new Random(randomSeed);
   }


   Gene(String name, int value, int min, int max, int delta,
        float mutationRate, int randomSeed)
   {
      type              = VALUE_TYPE.INTEGER_VALUE;
      this.name         = new String(name);
      ivalue            = imin = imax = idelta = 0;
      fvalue            = fmin = fmax = fdelta = 0.0f;
      ivalue            = value;
      imin              = min;
      imax              = max;
      idelta            = delta;
      this.mutationRate = mutationRate;
      this.randomSeed   = randomSeed;
      randomizer        = new Random(randomSeed);
   }


   Gene(String name, float value, float min, float max, float delta,
        float mutationRate, int randomSeed)
   {
      type              = VALUE_TYPE.FLOAT_VALUE;
      this.name         = new String(name);
      ivalue            = imin = imax = idelta = 0;
      fvalue            = fmin = fmax = fdelta = 0.0f;
      fvalue            = value;
      fmin              = min;
      fmax              = max;
      fdelta            = delta;
      this.mutationRate = mutationRate;
      this.randomSeed   = randomSeed;
      randomizer        = new Random(randomSeed);
   }


   // Copy.
   Gene copy()
   {
      Gene gene = new Gene(mutationRate, randomSeed);

      gene.type = type;
      if (name != null)
      {
         gene.name = new String(name);
      }
      gene.ivalue = ivalue;
      gene.imin   = imin;
      gene.imax   = imax;
      gene.idelta = idelta;
      gene.fvalue = fvalue;
      gene.fmin   = fmin;
      gene.fmax   = fmax;
      gene.fdelta = fdelta;
      return(gene);
   }


   // Mutate gene.
   void mutate()
   {
      int   i;
      float f;

      if (randomizer.nextDouble() > mutationRate)
      {
         return;
      }

      switch (type)
      {
      case INTEGER_VALUE:
         if (randomizer.nextDouble() <= mutationRate)
         {
            i = imax - imin;
            if (i > 0)
            {
               ivalue = randomizer.nextInt(imax - imin) + imin;
            }
            else
            {
               ivalue = imin;
            }
         }
         else
         {
            i = ivalue;
            if (randomizer.nextBoolean())
            {
               i += idelta;
               if (i > imax) { i = imax; }
            }
            else
            {
               i -= idelta;
               if (i < imin) { i = imin; }
            }
            ivalue = i;
         }
         break;

      case FLOAT_VALUE:
         if (randomizer.nextDouble() <= mutationRate)
         {
            fvalue = (randomizer.nextFloat() * (fmax - fmin)) + fmin;
         }
         else
         {
            f = fvalue;
            if (randomizer.nextBoolean())
            {
               f += fdelta;
               if (f > fmax) { f = fmax; }
            }
            else
            {
               f -= fdelta;
               if (f < fmin) { f = fmin; }
            }
            fvalue = f;
         }
         break;
      }
   }


   // Copy gene value.
   void copyValue(Gene from)
   {
      switch (type)
      {
      case INTEGER_VALUE:
         ivalue = from.ivalue;
         break;

      case FLOAT_VALUE:
         fvalue = from.fvalue;
         break;
      }
   }


   // Print gene.
   void print()
   {
      switch (type)
      {
      case INTEGER_VALUE:
         System.out.println(name + "=" + ivalue);
         break;

      case FLOAT_VALUE:
         System.out.println(name + "=" + fvalue);
         break;
      }
   }
}
