import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;

public class ThreadedAdderTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private ThreadedAdder underTest;

    @Before
    public void setup() {
        underTest = new ThreadedAdder();
    }

    @Test
    public void addNull_shouldThrowNullPointerException() {
        exception.expect(NullPointerException.class);
        underTest.add(null);
    }

    @Test
    public void add128Array_shouldReturnSum() {
        double[] smallTest = generateRandomDoubleArray(128);
        underTest.add(smallTest);
        assertThat(underTest.finishAndGet()).isEqualTo(sum(smallTest));
    }

    @Test
    public void add256Array_shouldReturnSum() {
        double[] smallTest = generateRandomDoubleArray(256);
        underTest.add(smallTest);
        assertThat(underTest.finishAndGet()).isEqualTo(sum(smallTest));
    }

    @Test
    public void add2048Array_shouldReturnSum() {
        double[] smallTest = generateRandomDoubleArray(2048);
        underTest.add(smallTest);
        assertThat(underTest.finishAndGet()).isEqualTo(sum(smallTest));
    }

    @Test
    public void add1048576Array_shouldReturnSum() {
        double[] smallTest = generateRandomDoubleArray(1048576);
        underTest.add(smallTest);
        assertThat(underTest.finishAndGet()).isEqualTo(sum(smallTest));
    }

    /** Generates an array of doubles of the specified size. */
    private double[] generateRandomDoubleArray(int size) {
        double[] result = new double[size];
        for (int index = 0; index < size; index++) {
            result[index] = ThreadLocalRandom.current().nextDouble(Double.MAX_VALUE);
        }
        return result;
    }

    /** Known working method for a double array. */
    private double sum(double[] input) {
        return DoubleStream.of(input).sum();
    }
}