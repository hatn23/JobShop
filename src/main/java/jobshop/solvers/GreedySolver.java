package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class GreedySolver implements Solver {
    //Priority Rule
    public enum Priority{
        SPT, //Shortest Processing Time
        LPT, //Longest Processing Time
        SRPT, //Shortest Remaining Processing Time
        LRPT, //Longest Remaining Processing Time
        EST_SPT, //Earliest Start Time & Shortest Processing Time
        EST_LPT, //Earliest Start Time & Longest Processing Time
        EST_SRPT, //Earliest Start Time & Shortest Remaining Processing Time
        EST_LRPT //Earliest Start Time & Longest Remaining Processing Time
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

    private ArrayList<Task> listESTTask (Instance instance, ArrayList<Task> listTasks, int[][] end, int[] releaseTime){
        Task task = listTasks.get(0);
        ArrayList<Task> newList = new ArrayList<>();
        newList.add(task);
        //similar to ResourceOrder
        int estTask = task.task == 0 ? 0 : end[task.job][task.task - 1];
        int bestTask = Math.max(releaseTime[instance.machine(task)], estTask);
        for (int i = 1; i < listTasks.size(); i++) {
            task = listTasks.get(i);
            estTask = task.task == 0 ? 0 : end[task.job][task.task - 1];
            int start = Math.max(releaseTime[instance.machine(task)], estTask);
            //if current task begin earlier than the best task, empty the list and add the current task to the List
            //else if they begin at the same time, add the current Task to the List
            if (start < bestTask) {
                newList.clear();
                newList.add(task);
                bestTask = start;
            } else if (start == bestTask) {
                newList.add(task);
            }
        }
        return newList;
    }

    private void updateEST(Instance instance, Task task, int[][] end, int[] releaseTime) {
        int machine = instance.machine(task);
        int duration = +instance.duration(task);
        int estTask = task.task == 0 ? 0 : end[task.job][task.task - 1];
        end[task.job][task.task] = Math.max(releaseTime[machine], estTask) + duration;
        releaseTime[machine] = end[task.job][task.task];
    }

    // Same method of SPT
    private Task EST_SPT(Instance instance, ArrayList<Task> listTasks, int[][] end, int[] releaseTime) {
        ArrayList<Task> reduce = listESTTask(instance, listTasks, end, releaseTime);
        Task task = SPT(instance, reduce);
        updateEST(instance, task, end, releaseTime);
        listTasks.remove(task);
        return task;
    }

    // Same method of LPT
    private Task EST_LPT(Instance instance, ArrayList<Task> listTasks, int[][] end, int[] releaseTime) {
        ArrayList<Task> reduce = listESTTask(instance, listTasks, end, releaseTime);
        Task task = LPT(instance, reduce);
        updateEST(instance, task, end, releaseTime);
        listTasks.remove(task);
        return task;
    }
    // Same method of SRPT
    private Task EST_SRPT(Instance instance, ArrayList<Task> listTasks, int[][] end, int[] releaseTime, int[] remainingTime) {
        int[] reduce = EST_RPT(instance, listTasks, remainingTime, end, releaseTime);
        Task task = SRPT(instance, listTasks, reduce);
        updateEST(instance, task, end, releaseTime);
        remainingTime[task.job] = reduce[task.job];
        return task;
    }

    // Same method of LRPT
    private Task EST_LRPT(Instance instance, ArrayList<Task> listTasks, int[][] end, int[] releaseTime, int[] remainingTime) {
        int[] reduce = EST_RPT(instance, listTasks, remainingTime, end, releaseTime);
        Task task = LRPT(instance, listTasks, reduce);
        updateEST(instance, task, end, releaseTime);
        remainingTime[task.job] = reduce[task.job];
        return task;
    }
    // Same method of listESTTask
    private int[] EST_RPT(Instance instance, ArrayList<Task> listTasks, int[] remainingTime, int[][] endAt, int[] releaseTime) {
        int[] reducer = new int[remainingTime.length];
        Task currentTask = listTasks.get(0);
        int estTask = currentTask.task == 0 ? 0 : endAt[currentTask.job][currentTask.task - 1];
        int bestTask = Math.max(releaseTime[instance.machine(currentTask)], estTask);
        reducer[currentTask.job] = remainingTime[currentTask.job];
        for (int i = 1; i < listTasks.size(); i++) {
            currentTask = listTasks.get(i);
            estTask = currentTask.task == 0 ? 0 : endAt[currentTask.job][currentTask.task - 1];
            int start = Math.max(releaseTime[instance.machine(currentTask)], estTask);
            if (start < bestTask) {
                reducer = new int[remainingTime.length];
                reducer[listTasks.get(i).job] = remainingTime[listTasks.get(i).job];
                bestTask = start;
            } else if (start == bestTask)
                reducer[listTasks.get(i).job] = remainingTime[listTasks.get(i).job];
        }
        return reducer;
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        //Representation by ResourceOrder of solution
        ResourceOrder solution = new ResourceOrder(instance);
        //List of task to scheduled
        ArrayList<Task> listToSchedule = new ArrayList<>();
        //time remaining of each job, only used for SRPT, LRPT and EST_LRPT
        int[] remainingTime = new int[instance.numJobs];
        //Initialize List of feasible tasks by the first task of each job
        for (int i = 0; i < instance.numJobs; i++){
            listToSchedule.add(new Task(i,0));
        }
        //Initialize remainingTime[] only for SRPT,LRPT and EST_LRPT
        if (priorityRule.name().endsWith("RPT")){
            for (int i = 0; i < instance.numJobs; i++){
                for (int j = 0; j < instance.numTasks; j ++){
                    remainingTime[i] += instance.duration(i,j);
                }
            }
        }
        //Array of end time of each task, if task haven't been processed yet, this value = 0 by default
        int[][] end = new int[instance.numJobs][instance.numTasks];
        //Array of time when each machine is released
        int[] releaseTime = new int[instance.numMachines];
        //While there are still tasks to be executed
        while (listToSchedule.size() > 0){
            Task task;
            //Task given by the corresponding method of glouton
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
                case EST_SPT:
                    task = EST_SPT(instance,listToSchedule,end,releaseTime);
                    break;
                case EST_LPT:
                    task = EST_LPT(instance,listToSchedule,end,releaseTime);
                    break;
                case EST_SRPT:
                    task = EST_SRPT(instance,listToSchedule,end,releaseTime,remainingTime);
                    break;
                case EST_LRPT:
                    task = EST_LRPT(instance,listToSchedule,end,releaseTime,remainingTime);
                    break;
                default:
                    task = EST_SPT(instance,listToSchedule,end,releaseTime);
                    break;
            }
            //Machine that execute Task
            int machine = instance.machine(task);
            //Schedule task
            solution.tasksByMachine[machine][solution.nextFreeSlot[machine]++] = task;
            //if Task is not the last one, add the next task to the list of task to scheduled
            if (task.task < instance.numTasks - 1){
                listToSchedule.add(new Task(task.job, task.task + 1));
            }
        }
        return new Result(instance, solution.toSchedule(), Result.ExitCause.Blocked);
    }
}
