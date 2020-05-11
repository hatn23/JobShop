package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;

import java.util.ArrayList;
import java.util.List;

public class TabooSolver implements Solver {

    private int maxIter = 500;
    private int durationTaboo = 10;

    public TabooSolver(int maxIter, int duration){
        this.maxIter = maxIter;
        this.durationTaboo = duration;
    }

    static class sTaboo {
        final int[][] tab;
        final int durationTaboo;

        sTaboo(int nbMachine, int nbJob, int durationTaboo) {
            this.tab = new int[nbJob*nbMachine][nbJob*nbMachine];
            this.durationTaboo = durationTaboo;
        }

        public void add(DescentSolver.Swap swap, int k) {
            tab[swap.t1][swap.t2] = k + durationTaboo;
        }

        public boolean check(DescentSolver.Swap swap, int k) {
            return k > tab[swap.t1][swap.t2];
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        Result result = new GreedySolver(GreedySolver.Priority.EST_LRPT).solve(instance,deadline);
        Result current_result = result;
        int best = result.schedule.makespan();
        ResourceOrder order = new ResourceOrder(result.schedule);
        ResourceOrder current_order = order.copy();
        int ind = instance.numJobs * instance.numMachines;
        int cpt = 0;
        while (cpt < maxIter && deadline - System.currentTimeMillis() > 1){
            cpt++;
            List<DescentSolver.Block> blocksList = DescentSolver.blocksOfCriticalPath(current_order);
            List<DescentSolver.Swap> swapList = new ArrayList<>();
            DescentSolver.Swap bestSwap = null;
            int best_local = -1;

            for (DescentSolver.Block block : blocksList) {
                swapList = DescentSolver.neighbors(block);
                for (DescentSolver.Swap swap : swapList) {
                    if (sTaboo.check(swap, cpt)) {
                        ResourceOrder copy = current_order.copy();
                        swap.applyOn(copy);
                        int makespan = copy.toSchedule().makespan();
                        if (best_local == -1 || makespan < best_local) {
                            bestSwap = swap;
                            best_local = makespan;
                            current_order = copy;
                            if (makespan < best) {
                                best = makespan;
                                order = copy;
                            }
                        }
                    }
                }
            }
            if (bestSwap != null) {
                sTaboo.add(bestSwap, cpt);
            }
            //on actualise s et s_local
            current_result = new Result(current_order.instance, current_order.toSchedule(), Result.ExitCause.Blocked);
            result = new Result(order.instance, order.toSchedule(), Result.ExitCause.Blocked);
        }
        if (cpt == maxIter) return result;
        return new Result(result.instance, result.schedule, Result.ExitCause.Timeout);
    }
}
