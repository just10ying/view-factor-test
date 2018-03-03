import com.aparapi.Kernel;
import com.aparapi.Range;
import com.google.common.annotations.VisibleForTesting;
import org.j3d.loaders.stl.STLFileReader;

import java.util.function.Consumer;

public class IntersectionKernel extends Kernel {
    private static final class MathOnlyKernelException extends RuntimeException {
        private MathOnlyKernelException() {
            super("Error: this kernel is missing geometry data and can only be used for its math methods.");
        }
    }

    public interface KernelComplete {
        double onComplete();
    }

    @Constant private static final double PI = 3.141592653589793238462643383279502884197169399375105820974944592307816406286d;

    @Constant private final double[] emitterNormalX;
    @Constant private final double[] emitterNormalY;
    @Constant private final double[] emitterNormalZ;
    @Constant private final double[] emitterCenterX;
    @Constant private final double[] emitterCenterY;
    @Constant private final double[] emitterCenterZ;
    @Constant private final double[] emitterAreas;

    @Constant private final int interconnectSize;
    @Constant private final double[] interconnectNormalX;
    @Constant private final double[] interconnectNormalY;
    @Constant private final double[] interconnectNormalZ;
    @Constant private final double[] interconnectVertexAX;
    @Constant private final double[] interconnectVertexAY;
    @Constant private final double[] interconnectVertexAZ;
    @Constant private final double[] interconnectEdgeBAX;
    @Constant private final double[] interconnectEdgeBAY;
    @Constant private final double[] interconnectEdgeBAZ;
    @Constant private final double[] interconnectEdgeCAX;
    @Constant private final double[] interconnectEdgeCAY;
    @Constant private final double[] interconnectEdgeCAZ;

    @Constant private final double[] receiverNormalX;
    @Constant private final double[] receiverNormalY;
    @Constant private final double[] receiverNormalZ;
    @Constant private final double[] receiverCenterX;
    @Constant private final double[] receiverCenterY;
    @Constant private final double[] receiverCenterZ;
    @Constant private final double[] receiverAreas;

    private double[] result;
    private int emitterIndex; // TODO(justinying): does the GPU properly get this number?

    /**
     * Constructor, used only by the Builder class. The builder exists so the above fields can be final, allowing Aparapi
     * to put them in faster memory.
     */
    private IntersectionKernel(
            double[] emitterNormalX,
            double[] emitterNormalY,
            double[] emitterNormalZ,
            double[] emitterVertexAX,
            double[] emitterVertexAY,
            double[] emitterVertexAZ,
            double[] emitterCenterX,
            double[] emitterCenterY,
            double[] emitterCenterZ,
            double[] emitterAreas,
            int interconnectSize,
            double[] interconnectNormalX,
            double[] interconnectNormalY,
            double[] interconnectNormalZ,
            double[] interconnectVertexAX,
            double[] interconnectVertexAY,
            double[] interconnectVertexAZ,
            double[] interconnectEdgeBAX,
            double[] interconnectEdgeBAY,
            double[] interconnectEdgeBAZ,
            double[] interconnectEdgeCAX,
            double[] interconnectEdgeCAY,
            double[] interconnectEdgeCAZ,
            double[] receiverNormalX,
            double[] receiverNormalY,
            double[] receiverNormalZ,
            double[] receiverVertexAX,
            double[] receiverVertexAY,
            double[] receiverVertexAZ,
            double[] receiverCenterX,
            double[] receiverCenterY,
            double[] receiverCenterZ,
            double[] receiverAreas) {
        this.emitterNormalX = emitterNormalX;
        this.emitterNormalY = emitterNormalY;
        this.emitterNormalZ = emitterNormalZ;
        this.emitterCenterX = emitterCenterX;
        this.emitterCenterY = emitterCenterY;
        this.emitterCenterZ = emitterCenterZ;
        this.emitterAreas = emitterAreas;

        this.interconnectSize = interconnectSize;
        this.interconnectNormalX = interconnectNormalX;
        this.interconnectNormalY = interconnectNormalY;
        this.interconnectNormalZ = interconnectNormalZ;
        this.interconnectVertexAX = interconnectVertexAX;
        this.interconnectVertexAY = interconnectVertexAY;
        this.interconnectVertexAZ = interconnectVertexAZ;
        this.interconnectEdgeBAX = interconnectEdgeBAX;
        this.interconnectEdgeBAY = interconnectEdgeBAY;
        this.interconnectEdgeBAZ = interconnectEdgeBAZ;
        this.interconnectEdgeCAX = interconnectEdgeCAX;
        this.interconnectEdgeCAY = interconnectEdgeCAY;
        this.interconnectEdgeCAZ = interconnectEdgeCAZ;

        this.receiverNormalX = receiverNormalX;
        this.receiverNormalY = receiverNormalY;
        this.receiverNormalZ = receiverNormalZ;
        this.receiverCenterX = receiverCenterX;
        this.receiverCenterY = receiverCenterY;
        this.receiverCenterZ = receiverCenterZ;
        this.receiverAreas = receiverAreas;
    }

    private boolean isMathOnly() {
        return emitterNormalX == null
                || emitterNormalY == null
                || emitterNormalZ == null
                || emitterCenterX == null
                || emitterCenterY == null
                || emitterCenterZ == null
                || emitterAreas == null
                || interconnectNormalX == null
                || interconnectNormalY == null
                || interconnectNormalZ == null
                || interconnectVertexAX == null
                || interconnectVertexAY == null
                || interconnectVertexAZ == null
                || interconnectEdgeBAX == null
                || interconnectEdgeBAY == null
                || interconnectEdgeBAZ == null
                || interconnectEdgeCAX == null
                || interconnectEdgeCAY == null
                || interconnectEdgeCAZ == null
                || receiverNormalX == null
                || receiverNormalY == null
                || receiverNormalZ == null
                || receiverCenterX == null
                || receiverCenterY == null
                || receiverCenterZ == null
                || receiverAreas == null;
    }

    /**
     * Runs the kernel for each emitter triangle, passing the view factor result of that triangle's dA incrementally back
     * to resultConsumer. Calls completionHandler onComplete when the task is finished.
     */
    void calculate(Consumer<double[]> resultConsumer, KernelComplete completionHandler) {
        if (isMathOnly()) throw new MathOnlyKernelException();

        setExplicit(true);
        put(emitterNormalX).put(emitterNormalY).put(emitterNormalZ);
        put(emitterCenterX).put(emitterCenterY).put(emitterCenterZ);
        put(emitterAreas);

        put(interconnectNormalX).put(interconnectNormalY).put(interconnectNormalZ);
        put(interconnectVertexAX).put(interconnectVertexAY).put(interconnectVertexAZ);
        put(interconnectEdgeBAX).put(interconnectEdgeBAY).put(interconnectEdgeBAZ);
        put(interconnectEdgeCAX).put(interconnectEdgeCAY).put(interconnectEdgeCAZ);

        put(receiverNormalX).put(receiverNormalY).put(receiverNormalZ);
        put(receiverCenterX).put(receiverCenterY).put(receiverCenterZ);
        put(receiverAreas);

        result = new double[receiverAreas.length];

        for (emitterIndex = 0; emitterIndex < receiverAreas.length; emitterIndex++) {
            super.execute(Range.create(receiverAreas.length));
            get(result);
            resultConsumer.accept(result);
        }
    }

    private static double sum(double...values) {
        double result = 0;
        for (double value:values)
            result += value;
        return result;
    }

    /**
     * Kernel code goes here. This is public, but don't call this!
     */
    @Override
    public void run() {
        int receiverIndex = getGlobalId();

        // Calculate the ray from the emitter to the destination triangle.
        double rayX = receiverCenterX[receiverIndex] - emitterCenterX[emitterIndex];
        double rayY = receiverCenterY[receiverIndex] - emitterCenterY[emitterIndex];
        double rayZ = receiverCenterZ[receiverIndex] - emitterCenterZ[emitterIndex];
        double rayMagnitude = magnitude(rayX, rayY, rayZ);

        // Check if any intersecting geometry exists.
        for (int interconnectIndex = 0; interconnectIndex < interconnectSize; interconnectIndex++) {
            double intersectionDistance = intersectionDistance(interconnectIndex, rayX, rayY, rayZ);
            // If intersecting geometry exists, the contributed view factor is zero.
            if (intersectionDistance != 0 && intersectionDistance <= rayMagnitude) {
                result[receiverIndex] = 0;
                return;
            }
        }

        double emitterDenominator =
                magnitude(
                        emitterNormalX[emitterIndex],
                        emitterNormalY[emitterIndex],
                        emitterNormalZ[emitterIndex]) * rayMagnitude;
        double receiverDenominator =
                magnitude(
                        receiverNormalX[receiverIndex],
                        receiverNormalY[receiverIndex],
                        receiverNormalZ[receiverIndex]) * rayMagnitude;

        double emitterNormalDotRay =
                emitterNormalX[emitterIndex] * rayX
                        + emitterNormalY[emitterIndex] * rayY
                        + emitterNormalZ[emitterIndex] * rayZ;
        double receiverNormalDotRay =
                receiverNormalX[receiverIndex] * rayX
                        + receiverNormalY[receiverIndex] * rayY
                        + receiverNormalZ[receiverIndex] * rayZ;

        double cosThetaOne = emitterNormalDotRay / emitterDenominator;
        double cosThetaTwo = receiverNormalDotRay / receiverDenominator;

        /*
         * TODO(Matthew Barry): please verify that cosThetaOne and cosThetaTwo are correct. Dot product with opposite ray
         * should just cause the angle to be negative.
         */
        if (cosThetaOne < 0) cosThetaOne = -cosThetaOne;
        if (cosThetaTwo < 0) cosThetaTwo = -cosThetaTwo;

        result[receiverIndex] = cosThetaOne * cosThetaTwo * emitterAreas[emitterIndex] * receiverAreas[receiverIndex]
                / (PI * rayMagnitude * rayMagnitude);
    }

    // TODO: tests.
    // From https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
    private double intersectionDistance(int interconnectIndex, double rayX, double rayY, double rayZ) {
        // pvec = cross product of ray and edge2.
        double pvecX = rayY * interconnectEdgeCAZ[interconnectIndex] - rayZ * interconnectEdgeCAY[interconnectIndex];
        double pvecY = rayZ * interconnectEdgeCAX[interconnectIndex] - rayX * interconnectEdgeCAZ[interconnectIndex];
        double pvecZ = rayX * interconnectEdgeCAY[interconnectIndex] - rayY * interconnectEdgeCAX[interconnectIndex];

        double intersectionDistance = -1;

        // Dot product of edge1 and pvec.
        double det = interconnectEdgeBAX[interconnectIndex] * pvecX
                + interconnectEdgeBAY[interconnectIndex] * pvecY
                + interconnectEdgeBAZ[interconnectIndex] * pvecZ;

        // Ray is parallel to plane.
        if (det < 1e-8 && det > -1e-8) return 0;

        double invDet = 1 / det;

        // tvec = ray from center vertex to emitterCenter.
        double tvecX = emitterCenterX[emitterIndex] - interconnectVertexAX[interconnectIndex];
        double tvecY = emitterCenterY[emitterIndex] - interconnectVertexAY[interconnectIndex];
        double tvecZ = emitterCenterZ[emitterIndex] - interconnectVertexAZ[interconnectIndex];

        // u = dot product of tvec and pvec.
        double u = tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ;
        if (u < 0 || u > 1) return 0;

        // qvec = cross product of tvec and edge1.
        double qvecX = tvecY * interconnectEdgeBAZ[interconnectIndex] - tvecZ * interconnectEdgeBAY[interconnectIndex];
        double qvecY = tvecZ * interconnectEdgeBAX[interconnectIndex] - tvecX * interconnectEdgeBAZ[interconnectIndex];
        double qvecZ = tvecX * interconnectEdgeBAY[interconnectIndex] - tvecY * interconnectEdgeBAX[interconnectIndex];

        // v = dot product of dir(ray) and qvec * invDet.
        double v = (rayX * qvecX + rayY * qvecY + rayZ * qvecZ) * invDet;

        if (v < 0 || u + v > 1) {
            return 0;
        } else {
            return (interconnectEdgeCAX[interconnectIndex] * qvecX
                    + interconnectEdgeCAY[interconnectIndex] * qvecY
                    + interconnectEdgeCAZ[interconnectIndex] * qvecZ) * invDet;
        }
    }

    @VisibleForTesting
    double magnitude(double a, double b, double c) {
        return Math.sqrt(a * a + b * b + c * c);
    }
}
