/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.info6205.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.Arrays;
import java.util.Collections;
import static edu.neu.coe.info6205.util.Utilities.formatWhole;
import edu.neu.coe.info6205.sort.elementary.InsertionSort;
import edu.neu.coe.info6205.graphs.BFS_and_prims.StdRandom;

/**
 * This class implements a simple Benchmark utility for measuring the running time of algorithms.
 * It is part of the repository for the INFO6205 class, taught by Prof. Robin Hillyard
 * <p>
 * It requires Java 8 as it uses function types, in particular, UnaryOperator&lt;T&gt; (a function of T => T),
 * Consumer&lt;T&gt; (essentially a function of T => Void) and Supplier&lt;T&gt; (essentially a function of Void => T).
 * <p>
 * In general, the benchmark class handles three phases of a "run:"
 * <ol>
 *     <li>The pre-function which prepares the input to the study function (field fPre) (may be null);</li>
 *     <li>The study function itself (field fRun) -- assumed to be a mutating function since it does not return a result;</li>
 *     <li>The post-function which cleans up and/or checks the results of the study function (field fPost) (may be null).</li>
 * </ol>
 * <p>
 * Note that the clock does not run during invocations of the pre-function and the post-function (if any).
 *
 * @param <T> The generic type T is that of the input to the function f which you will pass in to the constructor.
 */
public class Benchmark_Timer<T> implements Benchmark<T> {


    public static void main(String[] arg) {

        InsertionSort<Integer> insert = new InsertionSort<Integer>();
        int maxLength=10000;
        Consumer<Integer[]> consumer=array->insert.sort(array, 0, array.length);
        int initial=500;
        Benchmark_Timer<Integer[]> benchmarkTimer = new Benchmark_Timer<Integer[]>("Insertion Sort", consumer);

        int m = 50;
        System.out.println("-----Ordered-----");

        for (int i = initial; i < 20000; i += i) {
            int incrementor=i;
            //generating ordered array
            Supplier<Integer[]> supplier = () -> {
                Integer[] intArr = new Integer[incrementor];
                for (int j = 0; j < incrementor; j++)
                    intArr[j] = StdRandom.uniform(-maxLength, maxLength);
                Arrays.sort(intArr);//Sorting
                return intArr;
            };

            System.out.println(formatWhole(m) + " runs with " + incrementor + " in " + benchmarkTimer.runFromSupplier(supplier, m));
        }

        System.out.print("\n");
        System.out.println("-----Partially Ordered-----");

        for (int i = initial; i < 20000; i += i) {
            int incrementor=i;
            Supplier<Integer[]> supplier = () -> {
                Integer[] a = new Integer[incrementor];
                for (int j = 0; j < incrementor; j++)
                    a[j] = StdRandom.uniform(-maxLength, maxLength);

                Arrays.sort(a); //Sorting partially

                for (int j = 0; j < incrementor/2; j++)
                    a[j] = StdRandom.uniform(-maxLength, maxLength);

                return a;
            };
            System.out.println(formatWhole(m) + " runs with " + incrementor + " in " + benchmarkTimer.runFromSupplier(supplier, m));
        }

        System.out.print("\n");
        System.out.println("-----Randomly Ordered-----");
        for (int i = initial; i < 20000; i += i) {
            int incrementor=i;
            Supplier<Integer[]> supplier = () -> {
                Integer[] a = new Integer[incrementor];
                for (int j = 0; j < incrementor; j++)
                    a[j] = StdRandom.uniform(-maxLength, maxLength);
                return a;
            };
            System.out.println(formatWhole(m) + " runs with " + incrementor + " in " + benchmarkTimer.runFromSupplier(supplier, m));
        }


        System.out.println("-----Reverse Ordered-----");

        for (int i = initial; i < 20000; i += i) {
            int incrementor=i;
            Supplier<Integer[]> supplier = () -> {
                Integer[] a = new Integer[incrementor];
                for (int j = 0; j < incrementor; j++)
                    a[j] = StdRandom.uniform(-maxLength, maxLength);
                Arrays.sort(a, Collections.reverseOrder());//reverse sort
                return a;
            };
            System.out.println(formatWhole(m) + " runs with " + incrementor + " in " + benchmarkTimer.runFromSupplier(supplier, m));
        }
    }

    /**
     * Calculate the appropriate number of warmup runs.
     *
     * @param m the number of runs.
     * @return at least 2 and at most m/10.
     */
    static int getWarmupRuns(int m) {
        return Integer.max(2, Integer.min(10, m / 10));
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param supplier a Supplier of a T
     * @param m        the number of times the function f will be called.
     * @return the average number of milliseconds taken for each run of function f.
     */
    @Override
    public double runFromSupplier(Supplier<T> supplier, int m) {
        logger.info("Begin run: " + description + " -> ");
        // Warmup phase
        final Function<T, T> function = t -> {
            fRun.accept(t);
            return t;
        };
        new Timer().repeat(getWarmupRuns(m), supplier, function, fPre, null);

        // Timed phase
        return new Timer().repeat(m, supplier, function, fPre, fPost);
    }

    /**
     * Constructor for a Benchmark_Timer with option of specifying all three functions.
     *
     * @param description the description of the benchmark.
     * @param fPre        a function of T => T.
     *                    Function fPre is run before each invocation of fRun (but with the clock stopped).
     *                    The result of fPre (if any) is passed to fRun.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *                    When you create a lambda defining fRun, you must return "null."
     * @param fPost       a Consumer function (i.e. a function of T => Void).
     */
    public Benchmark_Timer(String description, UnaryOperator<T> fPre, Consumer<T> fRun, Consumer<T> fPost) {
        this.description = description;
        this.fPre = fPre;
        this.fRun = fRun;
        this.fPost = fPost;
    }

    /**
     * Constructor for a Benchmark_Timer with option of specifying all three functions.
     *
     * @param description the description of the benchmark.
     * @param fPre        a function of T => T.
     *                    Function fPre is run before each invocation of fRun (but with the clock stopped).
     *                    The result of fPre (if any) is passed to fRun.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     */
    public Benchmark_Timer(String description, UnaryOperator<T> fPre, Consumer<T> fRun) {
        this(description, fPre, fRun, null);
    }

    /**
     * Constructor for a Benchmark_Timer with only fRun and fPost Consumer parameters.
     *
     * @param description the description of the benchmark.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *                    When you create a lambda defining fRun, you must return "null."
     * @param fPost       a Consumer function (i.e. a function of T => Void).
     */
    public Benchmark_Timer(String description, Consumer<T> fRun, Consumer<T> fPost) {
        this(description, null, fRun, fPost);
    }

    /**
     * Constructor for a Benchmark_Timer where only the (timed) run function is specified.
     *
     * @param description the description of the benchmark.
     * @param f           a Consumer function (i.e. a function of T => Void).
     *                    Function f is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     */
    public Benchmark_Timer(String description, Consumer<T> f) {
        this(description, null, f, null);
    }

    private final String description;
    private final UnaryOperator<T> fPre;
    private final Consumer<T> fRun;
    private final Consumer<T> fPost;

    final static LazyLogger logger = new LazyLogger(Benchmark_Timer.class);
}
