package geometry;

import org.j3d.loaders.stl.STLFileReader;

public final class InterconnectGeometry extends Geometry {
    // Triangle vertices A where the two edges intersect, given by their xyz coordinates.
    private double[] vertexAX;
    private double[] vertexAY;
    private double[] vertexAZ;

    // Triangle edges from B to A, given by their xyz coordinates.
    private double[] edgeABX;
    private double[] edgeABY;
    private double[] edgeABZ;

    // Triangle edges from C to A., given by their xyz coordinates.
    private double[] edgeACX;
    private double[] edgeACY;
    private double[] edgeACZ;

    /** Instantiates*/
    public InterconnectGeometry(STLFileReader stlReader) {
        super(stlReader);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public double[] getVertexAX() {
        return vertexAX;
    }

    public double[] getVertexAY() {
        return vertexAY;
    }

    public double[] getVertexAZ() {
        return vertexAZ;
    }

    public double[] getEdgeABX() {
        return edgeABX;
    }

    public double[] getEdgeABY() {
        return edgeABY;
    }

    public double[] getEdgeABZ() {
        return edgeABZ;
    }

    public double[] getEdgeACX() {
        return edgeACX;
    }

    public double[] getEdgeACY() {
        return edgeACY;
    }

    public double[] getEdgeACZ() {
        return edgeACZ;
    }

    @Override
    protected void onArrayAllocation(int size) {
        vertexAX = new double[size];
        vertexAY = new double[size];
        vertexAZ = new double[size];

        edgeABX = new double[size];
        edgeABY = new double[size];
        edgeABZ = new double[size];

        edgeACX = new double[size];
        edgeACY = new double[size];
        edgeACZ = new double[size];
    }

    @Override
    protected void onVertexRead(double[][] vertices, int index) {
        vertexAX[index] = vertices[A][X];
        vertexAY[index] = vertices[A][Y];
        vertexAZ[index] = vertices[A][Z];

        edgeABX[index] = vertices[B][X] - vertices[A][X];
        edgeABY[index] = vertices[B][Y] - vertices[A][Y];
        edgeABZ[index] = vertices[B][Z] - vertices[A][Z];

        edgeACX[index] = vertices[C][X] - vertices[A][X];
        edgeACY[index] = vertices[C][Y] - vertices[A][Y];
        edgeACZ[index] = vertices[C][Z] - vertices[A][Z];
    }
}
