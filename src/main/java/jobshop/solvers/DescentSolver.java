package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DescentSolver implements Solver {

    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task t = order.tasksByMachine[machine][t1];
            order.tasksByMachine[machine][t1] = order.tasksByMachine[machine][t2];
            order.tasksByMachine[machine][t2] = t;
        }
    }

    static List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Task> criticalPath = order.toSchedule().criticalPath();
        List<Block> blocksList = new ArrayList<>();
        Task t = criticalPath.get(0);
        int machine = order.instance.machine(t);
        int firstTask = Arrays.asList(order.tasksByMachine[machine]).indexOf(t);
        int lastTask = firstTask;
        for (int i = 1; i < criticalPath.size(); i++) {
            t = criticalPath.get(i);
            if (machine == order.instance.machine(t)) {
                lastTask++;
            } else {
                if (firstTask != lastTask) {
                    blocksList.add(new Block(machine, firstTask, lastTask));
                }
                machine = order.instance.machine(t);
                firstTask = Arrays.asList(order.tasksByMachine[machine]).indexOf(t);
                lastTask = firstTask;
            }
        }
        if (firstTask != lastTask) {
            blocksList.add(new Block(machine, firstTask, lastTask));
        }
        return blocksList;
    }

    static List<Swap> neighbors(Block block) {
        List<Swap> swapList = new ArrayList<>();
        swapList.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
        if (block.firstTask != block.lastTask+1) {
            swapList.add(new Swap(block.machine, block.lastTask-1, block.lastTask));
        }
        return swapList;
    }

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

            List<Block> listBlock = blocksOfCriticalPath(order);

            for (Block block : listBlock){
                List<Swap> swapList = neighbors(block);
                for (Swap swap: swapList){
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
