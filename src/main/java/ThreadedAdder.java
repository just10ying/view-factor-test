import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.DoubleStream;

/**
 * Using threads, quickly adds multiple arrays of doubles into a single value. Each thread handles summing a single
 * double array in serial. Performance gains occur when add is requested on double arrays more quickly than a single
 * thread can sum an array.
 */
public final class ThreadedAdder {
    private static final int NUM_THREADS = 4;

    private final ExecutorService threadPool;
    private final DoubleAdder sum;

    public ThreadedAdder() {
        threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        sum = new DoubleAdder();
    }

    /** Enqueues the given array's addition job into the thread pool. */
    public void add(double[] toAdd) {
        if (toAdd == null) throw new NullPointerException();
        threadPool.submit(() -> sum.add(DoubleStream.of(toAdd).sum()));
    }

    /** Shuts down the threadpool and returns the sum of all previous calls to {@code add()}.*/
    public double finishAndGet() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return sum.doubleValue();
    }
}
