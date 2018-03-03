package geometry;

import com.google.common.annotations.VisibleForTesting;
import org.j3d.loaders.stl.STLFileReader;

public final class EmitterReceiverGeometry extends Geometry {
    // The centerpoints from which rays should be emitted or received.
    private double[] centerX;
    private double[] centerY;
    private double[] centerZ;

    // Emitter areas.
    private double[] area;

    public EmitterReceiverGeometry(STLFileReader stlReader) {
        super(stlReader);
    }

    public double[] getCenterX() {
        return centerX;
    }

    public double[] getCenterY() {
        return centerY;
    }

    public double[] getCenterZ() {
        return centerZ;
    }

    public double[] getArea() {
        return area;
    }

    @Override
    protected void onArrayAllocation(int size) {
        centerX = new double[size];
        centerY = new double[size];
        centerZ = new double[size];

        area = new double[size];
    }

    @Override
    protected void onVertexRead(double[][] vertices, int index) {
        centerX[index] = (vertices[A][X] + vertices[B][X] + vertices[C][X]) / 3;
        centerY[index] = (vertices[A][Y] + vertices[B][Y] + vertices[C][Y]) / 3;
        centerZ[index] = (vertices[A][Z] + vertices[B][Z] + vertices[C][Z]) / 3;

        area[index] = areaOf(vertices);
    }

    /** Calculates the area of the triangle given by edges AB and AC. */
    @VisibleForTesting
    static double areaOf(double[][] triangle) {
        double abX = triangle[B][X] - triangle[A][X];
        double abY = triangle[B][Y] - triangle[A][Y];
        double abZ = triangle[B][Z] - triangle[A][Z];

        double acX = triangle[C][X] - triangle[A][X];
        double acY = triangle[C][Y] - triangle[A][Y];
        double acZ = triangle[C][Z] - triangle[A][Z];

        return .5 * Math.sqrt(
                (abY * acZ - abZ * acY) * (abY * acZ - abZ * acY)
                        + (abZ * acX - abX * acZ) * (abZ * acX - abX * acZ)
                        + (abX * acY - abY * acX) * (abX * acY - abY * acX));
    }
}
