package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class GreedySolver implements Solver {

    public enum Priority{
        SPT,
        LPT,
        SRPT,
        LRPT
    }

    private Priority priorityRule;

    public GreedySolver(Priority priorityRule){
        this.priorityRule = priorityRule;
    }

    /*
    *   SPT(Shortest Processing Time)
    *   Task with smallest processing time
    */

    private Task SPT(Instance instance, ArrayList<Task> listTasks){
        int shortestTaskInd = 0;
        int shortestDuration = instance.duration(listTasks.get(shortestTaskInd));
        Task current;
        for (int i = 1; i < listTasks.size(); i++){
            int currentTaskDuration = instance.duration(listTasks.get(i));
            if (currentTaskDuration > shortestDuration){
                shortestDuration = currentTaskDuration;
                shortestTaskInd = i;
            }
        }
        return listTasks.remove(shortestTaskInd);
    }

    /*
     *   LPT(Shortest Processing Time)
     *   Task with longest processing time
     */

    private Task LPT(Instance instance, ArrayList<Task> listTasks){
        int longestTaskInd = 0;
        int longestDuration = instance.duration(listTasks.get(longestTaskInd));
        Task current;
        for (int i = 1; i < listTasks.size(); i++){
            int currentTaskDuration = instance.duration(listTasks.get(i));
            if (currentTaskDuration < longestDuration){
                longestDuration = currentTaskDuration;
                longestTaskInd = i;
            }
        }
        return listTasks.remove(longestTaskInd);
    }

    /*
     *   SRPT(Shortest Remaining Processing Time)
     *   Task with shortest remaining job processing time
     */

    private Task SRPT(Instance instance, ArrayList<Task> listTasks, int[] remainingTime){
        int shortestJob = 0;
        int bestTask = 0;
        for (int i = 1; i < remainingTime.length; i++){
            if ((remainingTime[i] < remainingTime[shortestJob] || remainingTime[shortestJob] == 0) && remainingTime[i] != 0) {
                shortestJob = i;
            }
        }
        for (int i = 1; i < listTasks.size(); i++){
            if (listTasks.get(i).job == shortestJob){
                remainingTime[shortestJob] -= instance.duration(shortestJob,listTasks.get(i).task);
                bestTask = i;
                break;
            }
        }
        return listTasks.remove(bestTask);
    }


    /*
     *   LRPT(Longest Remaining Processing Time)
     *   Task with longest remaining job processing time
     */

    private Task LRPT(Instance instance, ArrayList<Task> listTasks, int[] remainingTime){
        int longestJob = 0;
        int bestTask = 0;
        for (int i = 1; i < remainingTime.length; i++){
            if (remainingTime[i] > remainingTime[longestJob]) {
                longestJob = i;
            }
        }
        for (int i = 1; i < listTasks.size(); i++){
            if (listTasks.get(i).job == longestJob){
                remainingTime[longestJob] -= instance.duration(longestJob,listTasks.get(i).task);
                bestTask = i;
                break;
            }
        }
        return listTasks.remove(bestTask);
    }

    @Override
    public Result solve(Instance instance, long deadline) {
        return null;
    }


}
