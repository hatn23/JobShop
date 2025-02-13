package jobshop;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import jobshop.solvers.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class Main {

    /** All solvers available in this program */
    private static HashMap<String, Solver> solvers;
    static {
        solvers = new HashMap<>();
        solvers.put("basic", new BasicSolver());
        solvers.put("random", new RandomSolver());
        // add new solvers here
        // ******************** Greedy Solver ******************** //
        solvers.put("greedySPT", new GreedySolver(GreedySolver.Priority.SPT));
        solvers.put("greedyLPT", new GreedySolver(GreedySolver.Priority.LPT));
        solvers.put("greedySRPT", new GreedySolver(GreedySolver.Priority.SRPT));
        solvers.put("greedyLRPT", new GreedySolver(GreedySolver.Priority.LRPT));
        solvers.put("greedyEST_SPT", new GreedySolver(GreedySolver.Priority.EST_SPT));
        solvers.put("greedyEST_LPT", new GreedySolver(GreedySolver.Priority.EST_LPT));
        solvers.put("greedyEST_SRPT", new GreedySolver(GreedySolver.Priority.EST_SRPT));
        solvers.put("greedyEST_LRPT", new GreedySolver(GreedySolver.Priority.EST_LRPT));
        // ******************* Descent Solver ******************** //
        solvers.put("descentSPT",new DescentSolver(GreedySolver.Priority.SPT));
        solvers.put("descentLPT",new DescentSolver(GreedySolver.Priority.LPT));
        solvers.put("descentSRPT",new DescentSolver(GreedySolver.Priority.SRPT));
        solvers.put("descentLRPT",new DescentSolver(GreedySolver.Priority.LRPT));
        solvers.put("descentEST_SPT",new DescentSolver(GreedySolver.Priority.EST_SPT));
        solvers.put("descentEST_LPT",new DescentSolver(GreedySolver.Priority.EST_LPT));
        solvers.put("descentEST_SRPT",new DescentSolver(GreedySolver.Priority.EST_SRPT));
        solvers.put("descentEST_LRPT",new DescentSolver(GreedySolver.Priority.EST_LRPT));
        // ******************* Taboo Solver ******************** //
        solvers.put("taboo(1,1)", new TabooSolver(1, 1));
        solvers.put("taboo(10,1)", new TabooSolver(10, 1));
        solvers.put("taboo(10,2)", new TabooSolver(10, 2));
        solvers.put("taboo(10,3)", new TabooSolver(10, 3));
        solvers.put("taboo(10,4)", new TabooSolver(10, 4));
        solvers.put("taboo(10,5)", new TabooSolver(10, 5));
        solvers.put("taboo(10,6)", new TabooSolver(10, 6));
        solvers.put("taboo(10,7)", new TabooSolver(10, 7));
        solvers.put("taboo(10,8)", new TabooSolver(10, 8));
        solvers.put("taboo(10,9)", new TabooSolver(10, 9));
        solvers.put("taboo(10,10)", new TabooSolver(10, 10));
        solvers.put("taboo(10,20)", new TabooSolver(10, 20));
        solvers.put("taboo(10,50)", new TabooSolver(10, 50));
        solvers.put("taboo(10,100)", new TabooSolver(10, 100));

        solvers.put("taboo(100,1)", new TabooSolver(100, 1));
        solvers.put("taboo(100,3)", new TabooSolver(100, 3));
        solvers.put("taboo(100,5)", new TabooSolver(100, 5));
        solvers.put("taboo(100,10)", new TabooSolver(100, 10));
        solvers.put("taboo(100,12)", new TabooSolver(100, 12));
        solvers.put("taboo(100,15)", new TabooSolver(100, 15));
        solvers.put("taboo(100,20)", new TabooSolver(100, 20));
        solvers.put("taboo(100,30)", new TabooSolver(100, 30));
        solvers.put("taboo(100,40)", new TabooSolver(100, 40));
        solvers.put("taboo(100,50)", new TabooSolver(100, 50));
        solvers.put("taboo(100,60)", new TabooSolver(100, 60));
        solvers.put("taboo(100,70)", new TabooSolver(100, 70));
        solvers.put("taboo(100,80)", new TabooSolver(100, 80));
        solvers.put("taboo(100,90)", new TabooSolver(100, 90));
        solvers.put("taboo(100,100)", new TabooSolver(100, 100));
        solvers.put("taboo(100,120)", new TabooSolver(100, 120));
        solvers.put("taboo(100,140)", new TabooSolver(100, 140));
        solvers.put("taboo(100,160)", new TabooSolver(100, 160));
        solvers.put("taboo(100,180)", new TabooSolver(100, 180));
        solvers.put("taboo(100,200)", new TabooSolver(100, 200));

        solvers.put("taboo(500,1)", new TabooSolver(500, 1));
        solvers.put("taboo(500,3)", new TabooSolver(500, 3));
        solvers.put("taboo(500,5)", new TabooSolver(500, 5));
        solvers.put("taboo(500,10)", new TabooSolver(500, 10));
        solvers.put("taboo(500,12)", new TabooSolver(500, 12));
        solvers.put("taboo(500,15)", new TabooSolver(500, 15));
        solvers.put("taboo(500,20)", new TabooSolver(500, 20));
        solvers.put("taboo(500,30)", new TabooSolver(500, 30));
        solvers.put("taboo(500,40)", new TabooSolver(500, 40));
        solvers.put("taboo(500,50)", new TabooSolver(500, 50));
        solvers.put("taboo(500,60)", new TabooSolver(500, 60));
        solvers.put("taboo(500,70)", new TabooSolver(500, 70));
        solvers.put("taboo(500,80)", new TabooSolver(500, 80));
        solvers.put("taboo(500,90)", new TabooSolver(500, 90));
        solvers.put("taboo(500,100)", new TabooSolver(500, 100));

        solvers.put("taboo(1000,1)", new TabooSolver(1000, 1));
        solvers.put("taboo(1000,3)", new TabooSolver(1000, 3));
        solvers.put("taboo(1000,5)", new TabooSolver(1000, 5));
        solvers.put("taboo(1000,10)", new TabooSolver(1000, 10));
        solvers.put("taboo(1000,12)", new TabooSolver(1000, 12));
        solvers.put("taboo(1000,15)", new TabooSolver(1000, 15));
        solvers.put("taboo(1000,20)", new TabooSolver(1000, 20));
        solvers.put("taboo(1000,30)", new TabooSolver(1000, 30));
        solvers.put("taboo(1000,40)", new TabooSolver(1000, 40));
        solvers.put("taboo(1000,50)", new TabooSolver(1000, 50));
        solvers.put("taboo(1000,60)", new TabooSolver(1000, 60));
        solvers.put("taboo(1000,70)", new TabooSolver(1000, 70));
        solvers.put("taboo(1000,80)", new TabooSolver(1000, 80));
        solvers.put("taboo(1000,90)", new TabooSolver(1000, 90));
        solvers.put("taboo(1000,100)", new TabooSolver(1000, 100));


    }


    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("jsp-solver").build()
                .defaultHelp(true)
                .description("Solves jobshop problems.");

        parser.addArgument("-t", "--timeout")
                .setDefault(1L)
                .type(Long.class)
                .help("Solver timeout in seconds for each instance");
        parser.addArgument("--solver")
                .nargs("+")
                .required(true)
                .help("Solver(s) to use (space separated if more than one)");

        parser.addArgument("--instance")
                .nargs("+")
                .required(true)
                .help("Instance(s) to solve (space separated if more than one)");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        PrintStream output = System.out;

        long solveTimeMs = ns.getLong("timeout") * 1000;

        List<String> solversToTest = ns.getList("solver");
        for(String solverName : solversToTest) {
            if(!solvers.containsKey(solverName)) {
                System.err.println("ERROR: Solver \"" + solverName + "\" is not avalaible.");
                System.err.println("       Available solvers: " + solvers.keySet().toString());
                System.err.println("       You can provide your own solvers by adding them to the `Main.solvers` HashMap.");
                System.exit(1);
            }
        }
        List<String> instancePrefixes = ns.getList("instance");
        List<String> instances = new ArrayList<>();
        for(String instancePrefix : instancePrefixes) {
            List<String> matches = BestKnownResult.instancesMatching(instancePrefix);
            if(matches.isEmpty()) {
                System.err.println("ERROR: instance prefix \"" + instancePrefix + "\" does not match any instance.");
                System.err.println("       available instances: " + Arrays.toString(BestKnownResult.instances));
                System.exit(1);
            }
            instances.addAll(matches);
        }

        float[] runtimes = new float[solversToTest.size()];
        float[] distances = new float[solversToTest.size()];

        try {
            output.print(  "                         ");
            for(String s : solversToTest)
                output.printf("%-30s", s);
            output.println();
            output.print("instance size  best      ");
            for(String s : solversToTest) {
                output.print("runtime makespan ecart        ");
            }
            output.println();


            for(String instanceName : instances) {
                int bestKnown = BestKnownResult.of(instanceName);


                Path path = Paths.get("instances/", instanceName);
                Instance instance = Instance.fromFile(path);

                output.printf("%-8s %-5s %4d      ",instanceName, instance.numJobs +"x"+instance.numTasks, bestKnown);

                for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                    String solverName = solversToTest.get(solverId);
                    Solver solver = solvers.get(solverName);
                    long start = System.currentTimeMillis();
                    long deadline = System.currentTimeMillis() + solveTimeMs;
                    Result result = solver.solve(instance, deadline);
                    long runtime = System.currentTimeMillis() - start;

                    if(!result.schedule.isValid()) {
                        System.err.println("ERROR: solver returned an invalid schedule");
                        System.exit(1);
                    }

                    assert result.schedule.isValid();
                    int makespan = result.schedule.makespan();
                    float dist = 100f * (makespan - bestKnown) / (float) bestKnown;
                    runtimes[solverId] += (float) runtime / (float) instances.size();
                    distances[solverId] += dist / (float) instances.size();

                    output.printf("%7d %8s %5.1f        ", runtime, makespan, dist);
                    output.flush();
                }
                output.println();

            }


            output.printf("%-8s %-5s %4s      ", "AVG", "-", "-");
            for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                output.printf("%7.1f %8s %5.1f        ", runtimes[solverId], "-", distances[solverId]);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
