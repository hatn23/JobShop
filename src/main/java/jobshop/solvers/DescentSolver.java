package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.Utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DescentSolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {
        /*Initialisation : sinit   Glouton(Pb) : // générer une solution réalisable avec la méthode
        de votre choix;
        • Mémoriser la meilleure solution : s   sinit
        • Répéter // Exploration des voisinages successifs
            1. Choisir le meilleur voisin s0 :
                – s0 2 Neighboor(s) tq 8s00 2 Neighboor(s),Obj(s0) < Obj(s00)
            2. si s0 meilleur que s alors s   s0
        • Arrêt : pas d’amélioration de la solution ou time out*/
        Result result = new GreedySolver(GreedySolver.Priority.SPT).solve(instance,deadline);
        ResourceOrder order = new ResourceOrder(result.schedule);
        int best = result.schedule.makespan();
        while((deadline - System.currentTimeMillis()) > 1) {
            boolean foundBetter = true;

            List<Utils.Block> listBlock = Utils.blocksOfCriticalPath(order);

            for (Utils.Block block : listBlock){
                List<Utils.Swap> swapList = Utils.neighbors(block);
                for (Utils.Swap swap: swapList){
                    ResourceOrder optimalOrder = order.copy();
                   swap.applyOn(optimalOrder);
                   int makespan = optimalOrder.toSchedule().makespan();
                   if (makespan < best){
                       if (foundBetter){
                           foundBetter = false;
                       }
                       order = optimalOrder;
                       best = makespan;
                    }
                }
            }
            if (!foundBetter) result = new Result(order.instance, order.toSchedule(), Result.ExitCause.Blocked);
            else return result;
        }
        return new Result(result.instance, result.schedule,Result.ExitCause.Timeout);
    }

}
