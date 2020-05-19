package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;

import java.util.ArrayList;
import java.util.List;

public class TabooSolver implements Solver {
    //Number max of iteration
    private int maxIter ;
    //times allow to stock the taboo solution
    private int durationTaboo;

    public TabooSolver(int maxIter, int durationTaboo){
        this.maxIter = maxIter;
        this.durationTaboo = durationTaboo;
    }

    //Verify if a solution is already visited
    static class sTaboo {
        //Matrix nbJob * nbMachines
        final int[][] tab;
        final int durationTaboo;
        final int nbJob;

        sTaboo(int nbMachine, int nbJob, int durationTaboo) {
            this.tab = new int[nbJob*nbMachine][nbJob*nbMachine];
            this.durationTaboo = durationTaboo;
            this.nbJob = nbJob;
        }
        //update the iteration from which can do Swap
        public void add(DescentSolver.Swap swap, int k) {
            tab[swap.t1][swap.t2] = k + durationTaboo;
        }

        //Check if it is possible to do Swap
        public boolean check(DescentSolver.Swap swap, int k) {
            return k > tab[swap.t1][swap.t2];
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        // Initialize Result with a solution given by GreedySolver
        // Here I use EST_LRPT because it give the best results among all heuristic glouton
        Result result = new GreedySolver(GreedySolver.Priority.EST_LRPT).solve(instance,deadline);
        // Best result of the current iteration
        Result current_result = result;
        int best = result.schedule.makespan();
        //Taboo solution to check if the solution if already visited
        sTaboo sTaboo = new sTaboo(instance.numMachines, instance.numJobs, durationTaboo);
        //Count iteration
        int cpt = 0;
        //While not reach the maxIter and the deadline
        while (cpt < maxIter && deadline - System.currentTimeMillis() > 1){
            cpt++;
            //Order of best schedule
            ResourceOrder order = new ResourceOrder(result.schedule);
            //Order of the best schedule of the current iteration
            ResourceOrder current_order = new ResourceOrder(current_result.schedule);
            //List of Blocks of critical path
            List<DescentSolver.Block> blocksList = DescentSolver.blocksOfCriticalPath(current_order);
            //bestSwap is used to store the local best result
            DescentSolver.Swap bestSwap = null;
            int best_local = -1;
            for (DescentSolver.Block block : blocksList) {
                // List of Swap of the current Block
                List<DescentSolver.Swap> swapList = DescentSolver.neighbors(block);
                swapList = DescentSolver.neighbors(block);
                for (DescentSolver.Swap swap : swapList) {
                    //Check if it is possible to do Swap
                    if (sTaboo.check(swap, cpt)) {
                        //Copy the order of the best schedule of the current iteration
                        ResourceOrder copy = current_order.copy();
                        //Apply the Swap
                        swap.applyOn(copy);
                        int makespan ;
                        try {
                            makespan = copy.toSchedule().makespan();
                        } catch (Exception e){
                            //if the Swap is not allowed
                            return result;
                        }
                        // if the result given by Swap is better than the local best result
                        if (best_local == -1 || makespan < best_local) {
                            bestSwap = swap;
                            best_local = makespan;
                            current_order = copy;
                            //if the result given by Swap is also better then the best result
                            if (makespan < best) {
                                //Update the best result
                                best = makespan;
                                order = copy;
                            }
                        }
                    }
                }
            }
            // If a result given by Swap is better than the local best result
            if (bestSwap != null) {
                // store it in the Taboo Solution
                sTaboo.add(bestSwap, cpt);
            }
            //Update the best result and the local best result
            current_result = new Result(current_order.instance, current_order.toSchedule(), Result.ExitCause.Blocked);
            result = new Result(order.instance, order.toSchedule(), Result.ExitCause.Blocked);
        }
        //If the no of maxIter or the deadline is reached
        //exit (TimeOut)
        if (cpt == maxIter) return result;
        return new Result(result.instance, result.schedule, Result.ExitCause.Timeout);
    }
}
