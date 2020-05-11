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

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
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

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
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
            Task task1 = order.tasksByMachine[machine][t1];
            Task task2 = order.tasksByMachine[machine][t1];
            order.tasksByMachine[machine][t1] = task2;
            order.tasksByMachine[machine][t2] = task1;
        }
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
        deadline = deadline + System.currentTimeMillis();

        Result result = new GreedySolver(GreedySolver.Priority.SPT).solve(instance,deadline);
        ResourceOrder order = new ResourceOrder(result.schedule);
        int best = result.schedule.makespan();
        boolean foundBetter = true;
        ResourceOrder optimalOrder = order.copy();
        while(((deadline - System.currentTimeMillis()) > 1) && foundBetter) {
            foundBetter = false;
            List<Block> listBlock = blocksOfCriticalPath(order);
            List<Swap> listSwap = new ArrayList<>();
            for (int i = 0; i < listBlock.size(); i++) {
                listSwap.addAll(neighbors(listBlock.get(i)));
            }
            for (int i = 0; i < listSwap.size(); i++) {
                ResourceOrder currentOrder = optimalOrder.copy();
                Schedule schedule = currentOrder.toSchedule();
                listSwap.get(i).applyOn(currentOrder);
                Schedule newSchedule = currentOrder.toSchedule();
                if (newSchedule != null) {
                    int makespan = newSchedule.makespan();
                    if (best > makespan) {
                        optimalOrder = currentOrder;
                        best = makespan;
                        foundBetter = true;
                    }
                }
            }
        }
        return new Result(optimalOrder.instance, optimalOrder.toSchedule(),Result.ExitCause.Timeout);
    }

    /** Returns a list of all blocks of the critical path. */
    static List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Task> criticalPath = order.toSchedule().criticalPath();
        List<Block> listBlocks = new ArrayList<>();
        int currentMachine = order.instance.machine(criticalPath.get(0));
        //convert to list
        List<Task> list = Arrays.asList(order.tasksByMachine[currentMachine]);
        int firstTask = list.indexOf(criticalPath.get(0));
        int lastTask = firstTask;
        for (Task currentTask : criticalPath ){
            if (currentMachine == order.instance.machine(currentTask)){
                lastTask ++;
            }
            else {
                if (lastTask != firstTask){
                    listBlocks.add(new Block(currentMachine,firstTask,lastTask));
                }
                // Update current machine
                currentMachine = order.instance.machine(currentTask);
                list = Arrays.asList(order.tasksByMachine[currentMachine]);
                firstTask = list.indexOf(currentTask);
                lastTask = firstTask;
            }
        }
        return listBlocks;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    static List<Swap> neighbors(Block block) {
        List<Swap> swapList = new ArrayList<>();
        
        if (block.lastTask - block.firstTask != 1 ){
            swapList.add(new Swap(block.machine, block.firstTask, block.lastTask));
        }
        else {
            swapList.add(new Swap(block.machine, block.firstTask, block.firstTask + 1));
            swapList.add(new Swap(block.machine, block.lastTask -1 , block.lastTask));
        }
        //System.out.print("firstTask :" + block.firstTask);
        //System.out.print("lastTask :" + block.lastTask);
        return swapList;
    }

}
