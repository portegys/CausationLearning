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
   public static float MUTATION_RATE        = 0.25f;
	
   // Generations.
   int Generation;
   int Generations;

   // Causation populations.
   ArrayList<Population> Populations;

   // Print population properties.
   boolean PrintProperties;

   // Print evolution statistics.
   boolean PrintStatistics;

   // Constructor.
   public EvolveCausations()
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
      log("    steps=" + Steps);
      if (StepGameOfLife)
      {
         log("    stepGameOfLife=true");
      }
      else
      {
         log("    stepGameOfLife=false");
      }
      if (InputFileName == null)
      {
         if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
         {
            log("    MoxPopulations=" + EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY.getName());
         }
         else
         {
            log("    MoxPopulations=" + EvolveCommon.MOX_POPULATIONS.FORAGERS_AND_PREDATORS.getName());
         }
         for (int i = 0; i < CellsFileNames.size(); i++)
         {
            log("    loadCells=" + CellsFileNames.get(i));
         }
      }
      else
      {
         log("    input=" + InputFileName);
      }
      log("    output=" + OutputFileName);
      log("    MutationRate=" + EvolveCommon.MutationRate);
      log("    RandomMutationRate=" + EvolveCommon.RandomMutationRate);
      log("    MaxSensorRange=" + EvolveCommon.MaxSensorRange);
      log("    RandomSeed=" + EvolveCommon.RandomSeed);
      log("  Parameters:");
      log("    FORAGER_FIT_POPULATION_SIZE=" + EvolveCommon.FORAGER_FIT_POPULATION_SIZE);
      log("    FORAGER_NUM_MUTANTS=" + EvolveCommon.FORAGER_NUM_MUTANTS);
      log("    FORAGER_NUM_OFFSPRING=" + EvolveCommon.FORAGER_NUM_OFFSPRING);
      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_AND_PREDATORS)
      {
         log("    PREDATOR_FIT_POPULATION_SIZE=" + EvolveCommon.PREDATOR_FIT_POPULATION_SIZE);
         log("    PREDATOR_NUM_MUTANTS=" + EvolveCommon.PREDATOR_NUM_MUTANTS);
         log("    PREDATOR_NUM_OFFSPRING=" + EvolveCommon.PREDATOR_NUM_OFFSPRING);
      }

      // Extract moxen files?
      if (Extract)
      {
         extract();
         return;
      }

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

      // Set maximum mox cycle time according to current running conditions.
      MaxMoxCycleTime = Mox.getMaxCycleTime();

      // Evolution loop.
      log("Begin evolve:");
      for (Generations += Generation; Generation < Generations; Generation++)
      {
         log("Generation=" + Generation);

         evolve(Generation);

         // Save populations?
         if ((Generation % EvolveCommon.SAVE_FREQUENCY) == 0)
         {
            save(Generation);
         }
      }

      log("End evolve");
   }

   // Evolve.
   void evolve(int generation)
   {
      // Train foragers?
      if (TrainForagers)
      {
         trainForagers();
      }

      // Evaluate member fitness.
      evaluate(generation);

      // Prune unfit members.
      prune();

      // Create new members by mutation.
      mutate();

      // Create new members by mating.
      mate();
   }


   // Train foragers.
   void trainForagers()
   {
      int i, j, step;
      int blueFoodNeedIdx;
      Mox mox;

      ArrayList<Mox> moxen;
      MoxWorld       moxWorld;

      blueFoodNeedIdx = ForagerMox.NEED_TYPE.BLUE_FOOD.getValue();
      for (i = 0; i < EvolveCommon.FORAGER_POPULATION_SIZE; i++)
      {
         mox = ForagerPopulation[i].mox;
         mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
         moxen = new ArrayList<Mox>(1);
         moxen.add(0, mox);

         // Train mox in all mox worlds.
         ForagerPopulation[i].fitness = 0.0;
         for (j = 0; j < moxWorlds.size(); j++)
         {
            moxWorld = moxWorlds.get(j);
            moxWorld.setMoxen(moxen);
            moxWorld.reset();

            // Step world.
            if (Dashboard)
            {
               moxWorld.createDashboard();
               Dashboard = moxWorld.updateDashboard(0, Steps,
                                                    "forager training" +
                                                    ", member=" + i + ", mox=" + mox.id +
                                                    ", world=" + j +
                                                    ", blue food need=" +
                                                    mox.getNeed(blueFoodNeedIdx));
            }
            for (step = 0; step < Steps; step++)
            {
               if (mox.getNeed(blueFoodNeedIdx) == 0.0)
               {
                  break;
               }
               if (!mox.isAlive)
               {
                  break;
               }
               setTrainingResponse(mox, moxWorld);
               moxWorld.stepMoxen();
               if (StepGameOfLife)
               {
                  moxWorld.stepGameOfLife();
               }
               if (Dashboard)
               {
                  Dashboard = moxWorld.updateDashboard(step + 1, Steps,
                                                       "forager training" +
                                                       ", member=" + i + ", mox=" + mox.id +
                                                       ", world=" + j +
                                                       ", blue food need=" +
                                                       mox.getNeed(blueFoodNeedIdx));
               }
            }
            moxWorld.destroyDashboard();
            moxWorld.setMoxen(new ArrayList<Mox>());
            moxWorld.reset();
         }
      }
   }


   // Set mox training response.
   void setTrainingResponse(Mox mox, MoxWorld moxWorld)
   {
      int     x, y, nx, ny, ex, ey, sx, sy, wx, wy, w, h;
      int     d, dn, de, ds, dw;
      int     blueFoodNeedIdx;
      boolean needBlueFood;

      // Locate adjacent cells.
      w  = moxWorld.getWidth();
      h  = moxWorld.getHeight();
      nx = mox.x;
      ny = ((mox.y + 1) % h);
      ex = (mox.x + 1) % w;
      ey = mox.y;
      sx = mox.x;
      sy = mox.y - 1;
      if (sy < 0) { sy += h; }
      wx = mox.x - 1;
      if (wx < 0) { wx += w; }
      wy = mox.y;

      // Get distance from goal to nearest adjacent cell.
      blueFoodNeedIdx = ForagerMox.NEED_TYPE.BLUE_FOOD.getValue();
      needBlueFood    = false;
      if (mox.getNeed(blueFoodNeedIdx) > 0.0)
      {
         needBlueFood = true;
      }
      dn = de = ds = dw = -1;
      for (x = 0; x < w; x++)
      {
         for (y = 0; y < h; y++)
         {
            if ((moxWorld.gameOfLife.cells[x][y] == GameOfLife.BLUE_CELL_COLOR_VALUE) &&
                needBlueFood)
            {
               d = dist(x, y, nx, ny, w, h);
               if ((dn == -1) || (d < dn))
               {
                  dn = d;
               }
               d = dist(x, y, ex, ey, w, h);
               if ((de == -1) || (d < de))
               {
                  de = d;
               }
               d = dist(x, y, sx, sy, w, h);
               if ((ds == -1) || (d < ds))
               {
                  ds = d;
               }
               d = dist(x, y, wx, wy, w, h);
               if ((dw == -1) || (d < dw))
               {
                  dw = d;
               }
            }
         }
      }
      if (dn == -1)
      {
         return;
      }

      if (mox.direction == Mox.DIRECTION.NORTH.getValue())
      {
         if ((dn <= de) && (dn <= dw) && (dn <= ds))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.FORWARD.getValue());
         }
         else if ((de <= dw) && (de <= ds))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.RIGHT.getValue());
         }
         else
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.LEFT.getValue());
         }
         return;
      }
      else if (mox.direction == Mox.DIRECTION.EAST.getValue())
      {
         if ((de <= dn) && (de <= ds) && (de <= dw))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.FORWARD.getValue());
         }
         else if ((ds <= dn) && (ds <= dw))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.RIGHT.getValue());
         }
         else
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.LEFT.getValue());
         }
         return;
      }
      else if (mox.direction == Mox.DIRECTION.SOUTH.getValue())
      {
         if ((ds <= de) && (ds <= dw) && (ds <= dn))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.FORWARD.getValue());
         }
         else if ((dw <= de) && (dw <= dn))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.RIGHT.getValue());
         }
         else
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.LEFT.getValue());
         }
         return;
      }
      else
      {
         if ((dw <= dn) && (dw <= ds) && (dw <= de))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.FORWARD.getValue());
         }
         else if ((dn <= ds) && (dn <= de))
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.RIGHT.getValue());
         }
         else
         {
            mox.overrideResponse(Mox.RESPONSE_TYPE.LEFT.getValue());
         }
         return;
      }
   }


   // City-block distance.
   int dist(int x1, int y1, int x2, int y2, int w, int h)
   {
      int d, dx, dy;

      dx = x2 - x1;
      if (dx < 0) { dx = -dx; }
      d = w - dx;
      if (d < dx) { dx = d; }
      dy = y2 - y1;
      if (dy < 0) { dy = -dy; }
      d = h - dy;
      if (d < dy) { dy = d; }
      d = dx + dy;
      return(d);
   }


   // Evaluate member fitnesses.
   void evaluate(int generation)
   {
      int    i, j, step;
      int    blueFoodNeedIdx, moxFoodNeedIdx;
      int    moxFoodStep;
      Mox    mox;
      long   excessCycleTime;
      String logEntry;

      ArrayList<Mox> moxen;
      MoxWorld       moxWorld;

      log("Evaluate:");

      // Prepare for evaluation.
      prepareEvaluation(generation);

      // Evaluate foragers.
      log("  Foragers:");
      blueFoodNeedIdx = ForagerMox.NEED_TYPE.BLUE_FOOD.getValue();
      moxFoodNeedIdx  = PredatorMox.NEED_TYPE.MOX_FOOD.getValue();
      for (i = 0; i < EvolveCommon.FORAGER_POPULATION_SIZE; i++)
      {
         // Set up mox world.
         if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
         {
            mox = ForagerPopulation[i].mox;
            mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
            moxen = new ArrayList<Mox>(1);
            moxen.add(0, mox);
         }
         else
         {
            // Set up mox world with random fit predator.
            mox = PredatorPopulation[Randomizer.nextInt(EvolveCommon.PREDATOR_FIT_POPULATION_SIZE)].mox;
            mox.setNeed(moxFoodNeedIdx, PredatorMox.MOX_FOOD_NEED_VALUE);
            moxen = new ArrayList<Mox>(2);
            moxen.add(0, mox);
            mox = ForagerPopulation[i].mox;
            mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
            moxen.add(1, mox);
         }

         // Evaluate mox in all mox worlds.
         excessCycleTime = 0;
         ForagerPopulation[i].fitness = 0.0;
         for (j = 0; j < moxWorlds.size(); j++)
         {
            moxWorld = moxWorlds.get(j);
            moxWorld.setMoxen(moxen);
            moxWorld.reset();

            // Step world.
            if (Dashboard)
            {
               moxWorld.createDashboard();
               Dashboard = moxWorld.updateDashboard(0, Steps,
                                                    "generation=" + generation +
                                                    ", member=" + i + ", mox=" + mox.id +
                                                    ", world=" + j +
                                                    ", blue food need=" +
                                                    mox.getNeed(blueFoodNeedIdx));
            }
            for (step = 0; step < Steps; step++)
            {
               mox.startCycleTimeAccumulation();
               moxWorld.stepMoxen();
               if (!mox.isAlive)
               {
                  break;
               }
               if (mox.getNeed(blueFoodNeedIdx) == 0.0)
               {
                  ForagerPopulation[i].fitness += 1.0 + (1.0 / (double)(step + 1));
                  mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
               }
               if (mox.getCycleTimeAccumulator() > MaxMoxCycleTime)
               {
                  excessCycleTime = mox.getCycleTimeAccumulator();
                  break;
               }
               if (StepGameOfLife)
               {
                  moxWorld.stepGameOfLife();
               }
               if (Dashboard)
               {
                  Dashboard = moxWorld.updateDashboard(step + 1, Steps,
                                                       "generation=" + generation +
                                                       ", member=" + i + ", mox=" + mox.id +
                                                       ", world=" + j +
                                                       ", blue food need=" +
                                                       mox.getNeed(blueFoodNeedIdx));
               }
            }
            moxWorld.destroyDashboard();
            moxWorld.setMoxen(new ArrayList<Mox>());
            moxWorld.reset();
         }
         logEntry = "    member=" + i + ", " + ForagerPopulation[i].getInfo();
         if (!mox.isAlive)
         {
            logEntry = logEntry + ", eaten";
         }
         if (excessCycleTime > 0)
         {
            logEntry = logEntry + ", excess cycle time=" + excessCycleTime;
         }
         log(logEntry);
      }

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      // Evaluate predators.
      log("  Predators:");
      for (i = 0; i < EvolveCommon.PREDATOR_POPULATION_SIZE; i++)
      {
         // Set up mox world with random fit prey.
         mox = ForagerPopulation[Randomizer.nextInt(EvolveCommon.FORAGER_FIT_POPULATION_SIZE)].mox;
         mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
         moxen = new ArrayList<Mox>(2);
         moxen.add(0, mox);
         mox = PredatorPopulation[i].mox;
         mox.setNeed(moxFoodNeedIdx, PredatorMox.MOX_FOOD_NEED_VALUE);
         moxen.add(1, mox);

         excessCycleTime = 0;
         PredatorPopulation[i].fitness = 0.0;
         for (j = 0; j < moxWorlds.size(); j++)
         {
            moxWorld = moxWorlds.get(j);
            moxWorld.setMoxen(moxen);
            moxWorld.reset();

            // Step world.
            if (Dashboard)
            {
               moxWorld.createDashboard();
               Dashboard = moxWorld.updateDashboard(0, Steps,
                                                    "generation=" + generation +
                                                    ", member=" + i + ", mox=" + mox.id +
                                                    ", world=" + j +
                                                    ", mox food need=" +
                                                    mox.getNeed(moxFoodNeedIdx));
            }
            moxFoodStep = Steps;
            for (step = 0; step < Steps; step++)
            {
               mox.startCycleTimeAccumulation();
               moxWorld.stepMoxen();
               if (mox.getNeed(moxFoodNeedIdx) == 0.0)
               {
                  moxFoodStep = step;
                  break;
               }
               if (mox.getCycleTimeAccumulator() > MaxMoxCycleTime)
               {
                  excessCycleTime = mox.getCycleTimeAccumulator();
                  break;
               }
               if (StepGameOfLife)
               {
                  moxWorld.stepGameOfLife();
               }
               if (Dashboard)
               {
                  Dashboard = moxWorld.updateDashboard(step + 1, Steps,
                                                       "generation=" + generation +
                                                       ", member=" + i + ", mox=" + mox.id +
                                                       ", world=" + j +
                                                       ", mox food need=" +
                                                       mox.getNeed(moxFoodNeedIdx));
               }
            }
            PredatorPopulation[i].fitness += (double)moxFoodStep;
            moxWorld.destroyDashboard();
            moxWorld.setMoxen(new ArrayList<Mox>());
            moxWorld.reset();
         }
         logEntry = "    member=" + i + ", " + PredatorPopulation[i].getInfo();
         if (excessCycleTime > 0)
         {
            logEntry = logEntry + ", excess cycle time=" + excessCycleTime;
         }
         log(logEntry);
      }
   }


   // Prepare new moxen for evaluation by giving them
   // experience equivalent to existing moxen.
   void prepareEvaluation(int generation)
   {
      int i, j, n, runs, step;
      int blueFoodNeedIdx, moxFoodNeedIdx;
      Mox mox;

      ArrayList<Mox> moxen;
      MoxWorld       moxWorld;
      blueFoodNeedIdx = ForagerMox.NEED_TYPE.BLUE_FOOD.getValue();
      moxFoodNeedIdx  = PredatorMox.NEED_TYPE.MOX_FOOD.getValue();

      log("  Preparing new moxen:");

      // Catch up to current generation.
      if (generation < EvolveCommon.MAX_PREPARATION_TRIALS)
      {
         runs = generation;
      }
      else
      {
         runs = EvolveCommon.MAX_PREPARATION_TRIALS;
      }
      for (n = 0; n < runs; n++)
      {
         log("    Run " + (n + 1) + " of " + runs);

         // Run new foragers.
         for (i = EvolveCommon.FORAGER_FIT_POPULATION_SIZE;
              i < EvolveCommon.FORAGER_POPULATION_SIZE; i++)
         {
            // Set up mox world.
            if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
            {
               mox = ForagerPopulation[i].mox;
               mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
               moxen = new ArrayList<Mox>(1);
               moxen.add(0, mox);
            }
            else
            {
               // Set up mox world with random fit predator.
               mox = PredatorPopulation[Randomizer.nextInt(EvolveCommon.PREDATOR_FIT_POPULATION_SIZE)].mox;
               mox.setNeed(moxFoodNeedIdx, PredatorMox.MOX_FOOD_NEED_VALUE);
               moxen = new ArrayList<Mox>(2);
               moxen.add(0, mox);
               mox = ForagerPopulation[i].mox;
               mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
               moxen.add(1, mox);
            }

            // Run mox in all mox worlds.
            ForagerPopulation[i].fitness = 0.0;
            for (j = 0; j < moxWorlds.size(); j++)
            {
               moxWorld = moxWorlds.get(j);
               moxWorld.setMoxen(moxen);
               moxWorld.reset();

               // Step world.
               for (step = 0; step < Steps; step++)
               {
                  mox.startCycleTimeAccumulation();
                  moxWorld.stepMoxen();
                  if (!mox.isAlive)
                  {
                     break;
                  }
                  if (mox.getNeed(blueFoodNeedIdx) == 0.0)
                  {
                     mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
                  }
                  if (mox.getCycleTimeAccumulator() > MaxMoxCycleTime)
                  {
                     break;
                  }
                  if (StepGameOfLife)
                  {
                     moxWorld.stepGameOfLife();
                  }
               }
               moxWorld.destroyDashboard();
               moxWorld.setMoxen(new ArrayList<Mox>());
               moxWorld.reset();
            }
         }

         if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
         {
            continue;
         }

         // Run new predators.
         for (i = EvolveCommon.PREDATOR_FIT_POPULATION_SIZE;
              i < EvolveCommon.PREDATOR_POPULATION_SIZE; i++)
         {
            // Set up mox world with random fit prey.
            mox = ForagerPopulation[Randomizer.nextInt(EvolveCommon.FORAGER_FIT_POPULATION_SIZE)].mox;
            mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
            moxen = new ArrayList<Mox>(2);
            moxen.add(0, mox);
            mox = PredatorPopulation[i].mox;
            mox.setNeed(moxFoodNeedIdx, PredatorMox.MOX_FOOD_NEED_VALUE);
            moxen.add(1, mox);

            PredatorPopulation[i].fitness = 0.0;
            for (j = 0; j < moxWorlds.size(); j++)
            {
               moxWorld = moxWorlds.get(j);
               moxWorld.setMoxen(moxen);
               moxWorld.reset();

               // Step world.
               for (step = 0; step < Steps; step++)
               {
                  mox.startCycleTimeAccumulation();
                  moxWorld.stepMoxen();
                  if (mox.getNeed(moxFoodNeedIdx) == 0.0)
                  {
                     break;
                  }
                  if (mox.getCycleTimeAccumulator() > MaxMoxCycleTime)
                  {
                     break;
                  }
                  if (StepGameOfLife)
                  {
                     moxWorld.stepGameOfLife();
                  }
               }
               moxWorld.destroyDashboard();
               moxWorld.setMoxen(new ArrayList<Mox>());
               moxWorld.reset();
            }
         }
      }
      log("  Preparation completed");
   }


   // Prune unfit members.
   void prune()
   {
      double min, max, d;
      int    i, j, m;

      EvolveCommon.Member member;

      log("Select:");
      log("  Foragers:");
      EvolveCommon.Member[] fitPopulation =
         new EvolveCommon.Member[EvolveCommon.FORAGER_FIT_POPULATION_SIZE];
      max = 0.0;
      for (i = 0; i < EvolveCommon.FORAGER_FIT_POPULATION_SIZE; i++)
      {
         m = -1;
         for (j = 0; j < EvolveCommon.FORAGER_POPULATION_SIZE; j++)
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

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      log("  Predators:");
      fitPopulation =
         new EvolveCommon.Member[EvolveCommon.PREDATOR_FIT_POPULATION_SIZE];
      min = 0.0;
      for (i = 0; i < EvolveCommon.PREDATOR_FIT_POPULATION_SIZE; i++)
      {
         m = -1;
         for (j = 0; j < EvolveCommon.PREDATOR_POPULATION_SIZE; j++)
         {
            member = PredatorPopulation[j];
            if (member == null)
            {
               continue;
            }
            if ((m == -1) || (member.fitness < min))
            {
               m   = j;
               min = member.fitness;
            }
         }
         member = PredatorPopulation[m];
         PredatorPopulation[m] = null;
         fitPopulation[i]      = member;
         log("    " + member.getInfo());
      }
      for (i = 0; i < EvolveCommon.PREDATOR_POPULATION_SIZE; i++)
      {
         if (PredatorPopulation[i] != null)
         {
            PredatorPopulation[i] = null;
         }
      }
      d = 0.0;
      for (i = 0; i < EvolveCommon.PREDATOR_FIT_POPULATION_SIZE; i++)
      {
         PredatorPopulation[i] = fitPopulation[i];
         d += PredatorPopulation[i].fitness;
      }
      PredatorFittest[Generation] = PredatorPopulation[0].fitness;
      PredatorAverage[Generation] = d / (double)EvolveCommon.PREDATOR_FIT_POPULATION_SIZE;
   }


   // Mutate members.
   void mutate()
   {
      int i, j;

      EvolveCommon.Member member, mutant;

      log("Mutate:");
      log("  Foragers:");
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

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      log("  Predators:");
      for (i = 0; i < EvolveCommon.PREDATOR_NUM_MUTANTS; i++)
      {
         // Select a fit member to mutate.
         j      = Randomizer.nextInt(EvolveCommon.PREDATOR_FIT_POPULATION_SIZE);
         member = PredatorPopulation[j];

         // Create mutant member.
         mutant = new EvolveCommon.Member(member, member.generation + 1, Randomizer);
         PredatorPopulation[EvolveCommon.PREDATOR_FIT_POPULATION_SIZE + i] = mutant;
         log("    member=" + j + ", " + member.getInfo() +
             " -> member=" + (EvolveCommon.PREDATOR_FIT_POPULATION_SIZE + i) +
             ", " + mutant.getInfo());
      }
   }


   // Produce offspring by melding parent parameters.
   void mate()
   {
      int i, j, k;

      EvolveCommon.Member member1, member2, offspring;

      log("Mate:");
      if (EvolveCommon.FORAGER_FIT_POPULATION_SIZE > 1)
      {
         log("  Foragers:");
         for (i = 0; i < EvolveCommon.FORAGER_NUM_OFFSPRING; i++)
         {
            // Select a pair of fit members to mate.
            j       = Randomizer.nextInt(EvolveCommon.FORAGER_FIT_POPULATION_SIZE);
            member1 = ForagerPopulation[j];
            while ((k = Randomizer.nextInt(EvolveCommon.FORAGER_FIT_POPULATION_SIZE)) == j) {}
            member2 = ForagerPopulation[k];

            // Create offspring.
            offspring = new EvolveCommon.Member(member1, member2,
                                                (member1.generation > member2.generation ?
                                                 member1.generation : member2.generation) + 1, Randomizer);
            ForagerPopulation[EvolveCommon.FORAGER_FIT_POPULATION_SIZE +
                              EvolveCommon.FORAGER_NUM_MUTANTS + i] = offspring;
            log("    member=" + j + ", " + member1.getInfo() + " + member=" +
                k + ", " + member2.getInfo() +
                " -> member=" + (EvolveCommon.FORAGER_FIT_POPULATION_SIZE +
                                 EvolveCommon.FORAGER_NUM_MUTANTS + i) +
                ", " + offspring.getInfo());
         }
      }

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      if (EvolveCommon.PREDATOR_FIT_POPULATION_SIZE > 1)
      {
         log("  Predators:");
         for (i = 0; i < EvolveCommon.PREDATOR_NUM_OFFSPRING; i++)
         {
            // Select a pair of fit members to mate.
            j       = Randomizer.nextInt(EvolveCommon.PREDATOR_FIT_POPULATION_SIZE);
            member1 = PredatorPopulation[j];
            while ((k = Randomizer.nextInt(EvolveCommon.PREDATOR_FIT_POPULATION_SIZE)) == j) {}
            member2 = PredatorPopulation[k];

            // Create offspring.
            offspring = new EvolveCommon.Member(member1, member2,
                                                (member1.generation > member2.generation ?
                                                 member1.generation : member2.generation) + 1, Randomizer);
            PredatorPopulation[EvolveCommon.PREDATOR_FIT_POPULATION_SIZE +
                               EvolveCommon.PREDATOR_NUM_MUTANTS + i] = offspring;
            log("    member=" + j + ", " + member1.getInfo() + " + member=" +
                k + ", " + member2.getInfo() +
                " -> member=" + (EvolveCommon.PREDATOR_FIT_POPULATION_SIZE +
                                 EvolveCommon.PREDATOR_NUM_MUTANTS + i) +
                ", " + offspring.getInfo());
         }
      }
   }


   // Extract moxen files.
   void extract()
   {
      int      i, j;
      int      blueFoodNeedIdx, moxFoodNeedIdx;
      Mox      mox;
      String   filename;
      MoxWorld moxWorld;

      // Extract foragers.
      blueFoodNeedIdx = ForagerMox.NEED_TYPE.BLUE_FOOD.getValue();
      for (i = 0; i < EvolveCommon.FORAGER_POPULATION_SIZE; i++)
      {
         // Set up mox world.
         mox = ForagerPopulation[i].mox;
         mox.setNeed(blueFoodNeedIdx, ForagerMox.BLUE_FOOD_NEED_VALUE);
         ArrayList<Mox> moxen = new ArrayList<Mox>(1);
         moxen.add(0, mox);

         // Save mox world.
         for (j = 0; j < moxWorlds.size(); j++)
         {
            filename = "mox_world_forager_" + mox.id + "_world_" + j + ".mw";
            moxWorld = moxWorlds.get(j);
            moxWorld.setMoxen(moxen);
            moxWorld.reset();
            try
            {
               moxWorld.save(filename);
            }
            catch (Exception e) {
               System.err.println("Cannot save mox world to file " + filename +
                                  ":" + e.getMessage());
               System.exit(1);
            }
         }
      }

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      // Extract predators.
      moxFoodNeedIdx = PredatorMox.NEED_TYPE.MOX_FOOD.getValue();
      for (i = 0; i < EvolveCommon.PREDATOR_POPULATION_SIZE; i++)
      {
         // Set up mox world.
         mox = PredatorPopulation[i].mox;
         mox.setNeed(moxFoodNeedIdx, PredatorMox.MOX_FOOD_NEED_VALUE);
         ArrayList<Mox> moxen = new ArrayList<Mox>(1);
         moxen.add(0, mox);

         // Save mox world.
         for (j = 0; j < moxWorlds.size(); j++)
         {
            filename = "mox_world_predator_" + mox.id + "_world_" + j + ".mw";
            moxWorld = moxWorlds.get(j);
            moxWorld.setMoxen(moxen);
            moxWorld.reset();
            try
            {
               moxWorld.save(filename);
            }
            catch (Exception e) {
               System.err.println("Cannot save mox world to file " + filename +
                                  ":" + e.getMessage());
               System.exit(1);
            }
         }
      }
   }


   // Print population properties.
   void printProperties()
   {
      int i;

      System.out.println("Population properties:");

      // Print foragers.
      System.out.println("=============================");
      System.out.println("Foragers:");
      for (i = 0; i < EvolveCommon.FORAGER_POPULATION_SIZE; i++)
      {
         System.out.println("-----------------------------");
         ForagerPopulation[i].printProperties();
      }

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      // Print predators.
      System.out.println("=============================");
      System.out.println("Predators:");
      for (i = 0; i < EvolveCommon.PREDATOR_POPULATION_SIZE; i++)
      {
         System.out.println("-----------------------------");
         PredatorPopulation[i].printProperties();
      }
   }


   // Print evolution statistics.
   void printStatistics()
   {
      int i;

      System.out.println("Evolution statistics:");

      // Print forager statistics.
      System.out.println("Foragers:");
      System.out.println("Generation\tFittest");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + ForagerFittest[i]);
      }
      System.out.println("Generation\tAverage");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + ForagerAverage[i]);
      }

      if (MoxPopulations == EvolveCommon.MOX_POPULATIONS.FORAGERS_ONLY)
      {
         return;
      }

      // Print predator statistics.
      System.out.println("Predators:");
      System.out.println("Generation\tFittest");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + PredatorFittest[i]);
      }
      System.out.println("Generation\tAverage");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + PredatorAverage[i]);
      }
   }


   // Logging.
   void log(String message)
   {
      if (LogWriter != null)
      {
         LogWriter.println(message);
         LogWriter.flush();
      }
   }

   // Causation genome.
   public static class CausationGenome extends Genome
   {
      Gene maxMediatorLevel;
      Gene maxResponseEquippedMediatorLevel;
      Gene minResponseUnequippedMediatorLevel;

      // Constructor.
      CausationGenome(Random randomizer)
      {
         super(MUTATION_RATE, randomizer.nextInt());

         // INITIAL_ENABLEMENT.
         genes.add(
            new Gene("INITIAL_ENABLEMENT", 0.1, 0.1, 1.0, 0.1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // DRIVE_ATTENUATION.
         genes.add(
            new Gene("DRIVE_ATTENUATION", 0.0, 0.0, 1.0, 0.1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // LEARNING_DECREASE_VELOCITY.
         genes.add(
            new Gene("LEARNING_DECREASE_VELOCITY", 0.1, 0.1, 0.9, 0.1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // LEARNING_INCREASE_VELOCITY.
         genes.add(
            new Gene("LEARNING_INCREASE_VELOCITY", 0.1, 0.1, 0.9, 0.1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // FIRING_STRENGTH_LEARNING_DAMPER.
         genes.add(
            new Gene("FIRING_STRENGTH_LEARNING_DAMPER", 0.1, 0.05, 0.9, 0.05,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // UTILITY_ASYMPTOTE.
         genes.add(
            new Gene("UTILITY_ASYMPTOTE", 10.0, 0.0, 100.0, 10.0,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // RESPONSE_RANDOMNESS.
         genes.add(
            new Gene("RESPONSE_RANDOMNESS", 0.01, 0.01, 0.2, 0.01,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // DEFAULT_MAX_LEARNING_EFFECT_EVENT_INTERVAL.
         genes.add(
            new Gene("DEFAULT_MAX_LEARNING_EFFECT_EVENT_INTERVAL", 1, 1, 3, 1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // DEFAULT_NUM_EFFECT_EVENT_INTERVALS.
         genes.add(
            new Gene("DEFAULT_NUM_EFFECT_EVENT_INTERVALS", 1, 1, 3, 1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // MAX_ASSOCIATOR_EVENTS.
         genes.add(
            new Gene("MAX_ASSOCIATOR_EVENTS", 1, 1, 5, 1,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // MAX_MEDIATORS.
         genes.add(
            new Gene("MAX_MEDIATORS", 100, 50, 500, 50,
                     MutationRate, RandomMutationRate, randomizer.nextInt()));

         // MAX_MEDIATOR_LEVEL.
         maxMediatorLevel =
            new Gene("MAX_MEDIATOR_LEVEL", 2, 1, 5, 1,
                     MutationRate, RandomMutationRate, randomizer.nextInt());
         genes.add(maxMediatorLevel);

         // MAX_RESPONSE_EQUIPPED_MEDIATOR_LEVEL.
         maxResponseEquippedMediatorLevel =
            new Gene("MAX_RESPONSE_EQUIPPED_MEDIATOR_LEVEL", 2, 1, 5, 1,
                     MutationRate, RandomMutationRate, randomizer.nextInt());
         genes.add(maxResponseEquippedMediatorLevel);

         // MIN_RESPONSE_UNEQUIPPED_MEDIATOR_LEVEL.
         minResponseUnequippedMediatorLevel =
            new Gene("MIN_RESPONSE_UNEQUIPPED_MEDIATOR_LEVEL", 1, 1, 5, 1,
                     MutationRate, RandomMutationRate, randomizer.nextInt());
         genes.add(minResponseUnequippedMediatorLevel);
      }


      // Mutate.
      void mutate()
      {
         // Mutate.
         super.mutate();

         // Sanity checks.
         if (maxResponseEquippedMediatorLevel.ivalue > maxMediatorLevel.ivalue)
         {
            maxResponseEquippedMediatorLevel.ivalue = maxMediatorLevel.ivalue;
         }
         if (minResponseUnequippedMediatorLevel.ivalue > maxMediatorLevel.ivalue)
         {
            minResponseUnequippedMediatorLevel.ivalue = maxMediatorLevel.ivalue;
         }
         if (minResponseUnequippedMediatorLevel.ivalue >
             (maxResponseEquippedMediatorLevel.ivalue + 1))
         {
            if (randomizer.nextBoolean())
            {
               minResponseUnequippedMediatorLevel.ivalue =
                  maxResponseEquippedMediatorLevel.ivalue;
            }
            else
            {
               maxResponseEquippedMediatorLevel.ivalue =
                  minResponseUnequippedMediatorLevel.ivalue;
            }
         }
      }
   }
   
   // Population.
   public class Population
   {
	   ArrayList<Member> members;
	   
	   public Population()
	   {
		   members = new ArrayList<Member>();
	   }
   };
   
   // Population member.
   public class Member
   {
      int    species;
      int    generation;
      double fitness;

      // Mox parameters.
      MoxParmGenome moxParmGenome;

      // Instincts.
      Gene numInstincts;
      Vector<MoxHomeostatGenome> instinctHomeostats;

      // Mox.
      Mox mox;

      // Constructors.
      Member(int species, int generation, Random randomizer)
      {
         this.species    = species;
         this.generation = generation;
         fitness         = 0.0;

         // Create parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);

         // Create instinct genomes.
         numInstincts = new Gene("NUM_INSTINCTS", 0, 0, 5, 1,
                                 MutationRate, RandomMutationRate, randomizer.nextInt());
         instinctHomeostats = new Vector<MoxHomeostatGenome>();
         for (int i = 0; i < numInstincts.ivalue; i++)
         {
            instinctHomeostats.add(new MoxHomeostatGenome(randomizer));
         }

         // Initialize mox.
         initMox(x, y, direction, randomizer);
      }


      // Construct mutation of given member.
      Member(Member member, int generation, Random randomizer)
      {
         species         = member.species;
         this.generation = generation;
         fitness         = 0.0;

         // Create and mutate parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
         moxParmGenome.copyValues(member.moxParmGenome);
         moxParmGenome.mutate();

         // Create and mutate instinct genomes.
         numInstincts = new Gene("NUM_INSTINCTS", 0, 0, 5, 1,
                                 MutationRate, RandomMutationRate, randomizer.nextInt());
         numInstincts.copyValue(member.numInstincts);
         numInstincts.mutate();
         instinctHomeostats = new Vector<MoxHomeostatGenome>();
         for (int i = 0; i < numInstincts.ivalue; i++)
         {
            instinctHomeostats.add(new MoxHomeostatGenome(randomizer));
            if (i < member.numInstincts.ivalue)
            {
               instinctHomeostats.get(i).copyValues(member.instinctHomeostats.get(i));
               instinctHomeostats.get(i).mutate();
            }
         }

         // Initialize mox.
         initMox(member.mox.x2, member.mox.y2, member.mox.direction2, randomizer);
      }

      // Print properties.
      void printProperties()
      {
         System.out.println(getInfo());
         System.out.println("parameters:");
         moxParmGenome.print();
         for (int i = 0; i < numInstincts.ivalue; i++)
         {
            System.out.println("instinct " + i + ":");
            instinctHomeostats.get(i).print();
         }
      }


      // Get information.
      String getInfo()
      {
         return("id=" + id +
                ", fitness=" + fitness +
                ", generation=" + generation);
      }
   }
   
   // Print parameters.
   public static void printParameters()
   {
      System.out.println("GENERATIONS = " + GENERATIONS);
      System.out.println("POPULATION_SIZE = " + POPULATION_SIZE);
      System.out.println("MUTATION_RATE = " + MUTATION_RATE);
   }   
}
