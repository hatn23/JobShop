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

    private GreedySolver.Priority priorityRule;

    public DescentSolver(GreedySolver.Priority priorityRule){
        this.priorityRule = priorityRule;
    }


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

    /** Returns a list of all blocks of the critical path. */
    static List<Block> blocksOfCriticalPath(ResourceOrder order) {
        //critical path of order
        List<Task> criticalPath = order.toSchedule().criticalPath();
        //list of blocks of critical path
        List<Block> blocksList = new ArrayList<>();
        Task t = criticalPath.get(0);
        int machine = order.instance.machine(t);
        //the position in the order of execution of the machine
        int firstTask = Arrays.asList(order.tasksByMachine[machine]).indexOf(t);
        int lastTask = firstTask;
        for (int i = 1; i < criticalPath.size(); i++) {
            t = criticalPath.get(i);
            //verify if both tasks are executed by the same machine
            if (machine == order.instance.machine(t)) {
                lastTask++;
            } else {
                //verify if a block exist
                if (firstTask != lastTask) {
                    blocksList.add(new Block(machine, firstTask, lastTask));
                }
                //reset variables
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

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    static List<Swap> neighbors(Block block) {
        List<Swap> swapList = new ArrayList<>();
        swapList.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
        // if size of Block > 2
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
        // Initialize Result with a solution given by GreedySolver
        Result result = new GreedySolver(this.priorityRule).solve(instance,deadline);
        //order which corresponds to the best schedule
        ResourceOrder order = new ResourceOrder(result.schedule);
        int best = result.schedule.makespan();
        //until the deadline is not reach
        while((deadline - System.currentTimeMillis()) > 1) {
            //exit by default (in case that no better order is found)
            boolean foundBetter = true;
            //List of Block of Critical Path
            List<Block> listBlock = blocksOfCriticalPath(order);
            for (Block block : listBlock){
                //list of Swap of the current Block
                List<Swap> swapList = neighbors(block);
                for (Swap swap: swapList){
                    //Copy order of result and apply Swap
                    ResourceOrder optimalOrder = order.copy();
                    swap.applyOn(optimalOrder);
                    int makespan = optimalOrder.toSchedule().makespan();
                    //If Swap return a better result then we actualize Result
                    if (makespan < best){
                       if (foundBetter){
                           foundBetter = false;
                       }
                       order = optimalOrder;
                       best = makespan;
                    }
                }
            }
            //If no better order is found, exit
            if (!foundBetter) result = new Result(order.instance, order.toSchedule(), Result.ExitCause.Blocked);
            else return result;
        }
        return new Result(result.instance, result.schedule,Result.ExitCause.Timeout);
    }
}
