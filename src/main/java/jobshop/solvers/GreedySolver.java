package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
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
        ResourceOrder solution = new ResourceOrder(instance);
        ArrayList<Task> listToSchedule = new ArrayList<>();
        //time remaining of each job
        int[] remainingTime = new int[instance.numJobs];
        int[][] end = new int[instance.numJobs][instance.numTasks];
        int[] releaseTime = new int[instance.numMachines];
        for (int i = 0; i < instance.numJobs; i++){
            listToSchedule.add(new Task(i,0));
        }
        //Initialize remainingTime[] only for SRPT,LRPT
        if (priorityRule.name() == "SRPT " || priorityRule.name() == "LRPT"){
            for (int i = 0; i < instance.numJobs; i++){
                for (int j = 0; j < instance.numTasks; j ++){
                    remainingTime[i] += instance.duration(i,j);
                }
            }
        }

        while (listToSchedule.size() > 0){
            Task task;
            switch (priorityRule){
                case LPT:
                    task = LPT(instance,listToSchedule);
                    break;
                case SPT:
                    task = SPT(instance,listToSchedule);
                    break;
                case LRPT:
                    task = LRPT(instance,listToSchedule,remainingTime);
                    break;
                case SRPT:
                    task = SRPT(instance,listToSchedule,remainingTime);
                    break;
                default:
                    task = SPT(instance,listToSchedule);
                    break;
            }
            int machine = instance.machine(task);
            //Schedule task
            solution.tasksByMachine[machine][solution.nextFreeSlot[machine]++] = task;
            if (task.task < instance.numTasks - 1){
                listToSchedule.add(new Task(task.job, task.task + 1));
            }
        }
        return new Result(instance, solution.toSchedule(), Result.ExitCause.Blocked);
    }
}
