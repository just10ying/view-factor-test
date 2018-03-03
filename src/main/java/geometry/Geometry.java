package geometry;

import org.j3d.loaders.stl.STLFileReader;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Represents data common to all triangles that must be sent to the GPU.
 * Transforms {@link STLFileReader}s into a data format that Aparapi can copy to the GPU. Represents a list of
 * triangles, defining for each the:
 * - normal
 * - two edges
 * - vertex where those two edges intersect
 * - center
 * - area
 *
 * Each of these values has xyz coordinates, given by the publicly accessible arrays below. These are primitive values
 * due to limitations on what types Aparapi can handle.
 */
abstract class Geometry {
    // Conventions for unwrapping vertex data from {@link STLFileReader}.
    protected static final int A = 0;
    protected static final int B = 1;
    protected static final int C = 2;

    // Conventions for unwrapping dimension data from {@link STLFileReader}.
    protected static final int X = 0;
    protected static final int Y = 1;
    protected static final int Z = 2;

    // Number of triangles stored.
    protected int size;

    // Triangle normal vectors, given by their coordinates.
    protected double[] normalX;
    protected double[] normalY;
    protected double[] normalZ;

    protected Geometry(STLFileReader stlReader) {
        initFromStlFileReader(stlReader);
    }

    public double[] getNormalX() {
        return normalX;
    }

    public double[] getNormalY() {
        return normalY;
    }

    public double[] getNormalZ() {
        return normalZ;
    }

    protected abstract void onArrayAllocation(int size);

    protected abstract void onVertexRead(double[][] vertices, int index);

    private void initFromStlFileReader(STLFileReader reader) {
        try {
            initWithSize(IntStream.of(reader.getNumOfFacets()).sum());
            double[] normal = new double[3];
            double[][] vertices = new double[3][3];
            for (int index = 0; index < size; index++) {
                reader.getNextFacet(normal, vertices);

                normalX[index] = normal[X];
                normalY[index] = normal[Y];
                normalZ[index] = normal[Z];

                onVertexRead(vertices, index);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initWithSize(int size) {
        this.size = size;

        normalX = new double[size];
        normalY = new double[size];
        normalZ = new double[size];

        onArrayAllocation(size);
    }
}
