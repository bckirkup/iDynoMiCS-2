package solver.mgFas;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * GPU-accelerated red-black relaxation for the multigrid solver.
 * Loads the native library {@code idynomics_mg_cuda} (e.g. libidynomics_mg_cuda.so
 * or idynomics_mg_cuda.dll) and delegates the relax stencil to CUDA when available.
 */
public final class GpuRelax {

    private static final String LIB_NAME = "idynomics_mg_cuda";
    private static volatile boolean loaded;
    private static volatile boolean available;

    static {
        try {
            String path = System.getProperty("idynomics.gpu.native.path");
            if (path != null && !path.isEmpty()) {
                System.load(path);
            } else {
                System.loadLibrary(LIB_NAME);
            }
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            loaded = false;
            Log.out(Tier.NORMAL, "GPU relax library not loaded: " + e.getMessage());
        }
    }

    /** Returns true if the native library was loaded. */
    public static boolean isLoaded() {
        return loaded;
    }

    /** Returns true if CUDA is available and the library is loaded. */
    public static boolean isAvailable() {
        if (!loaded) return false;
        if (!available) {
            available = nativeAvailable();
        }
        return available;
    }

    /**
     * Run nIter red-black relax steps for one solute grid on the GPU.
     * Flattens grid data, calls native kernel, copies result back and refreshes boundary.
     *
     * @param solute the multigrid solute (concentration, bLayer, rd, rhs, reac, diffReac)
     * @param order  multigrid level
     * @param nIter  number of relax iterations (each is red + black pass)
     * @return true if GPU was used, false if fell back to Java or failed
     */
    public static boolean relaxBatch(MultigridSolute solute, int order, int nIter) {
        if (!isAvailable() || nIter <= 0) return false;

        int nI = solute._conc[order].getGridSizeI();
        int nJ = solute._conc[order].getGridSizeJ();
        int nK = solute._conc[order].getGridSizeK();
        int nP = (nI + 2) * (nJ + 2) * (nK + 2);

        double[] uFlat = flatten(solute._conc[order].grid, nI, nJ, nK);
        double[] blFlat = flatten(solute._bLayer[order].grid, nI, nJ, nK);
        double[] rdFlat = flatten(solute._relDiff[order].grid, nI, nJ, nK);
        double[] rhsFlat = flatten(solute._rhs[order].grid, nI, nJ, nK);
        double[] reacFlat = flatten(solute._reac[order].grid, nI, nJ, nK);
        double[] diffReacFlat = flatten(solute._diffReac[order].grid, nI, nJ, nK);

        double h2i = solute.getH2i(order);
        double diffusivity = solute.realGrid.diffusivity;

        boolean ok = nativeRelax(nI, nJ, nK, h2i, diffusivity,
                uFlat, blFlat, rdFlat, rhsFlat, reacFlat, diffReacFlat, nIter);

        if (ok) {
            unflatten(uFlat, solute._conc[order].grid, nI, nJ, nK);
            solute._conc[order].refreshBoundary();
        }
        return ok;
    }

    private static double[] flatten(double[][][] grid, int nI, int nJ, int nK) {
        int si = nI + 2, sj = nJ + 2, sk = nK + 2;
        double[] out = new double[si * sj * sk];
        for (int k = 0; k < sk; k++)
            for (int j = 0; j < sj; j++)
                for (int i = 0; i < si; i++)
                    out[i + si * (j + sj * k)] = grid[i][j][k];
        return out;
    }

    private static void unflatten(double[] flat, double[][][] grid, int nI, int nJ, int nK) {
        int si = nI + 2, sj = nJ + 2, sk = nK + 2;
        for (int k = 0; k < sk; k++)
            for (int j = 0; j < sj; j++)
                for (int i = 0; i < si; i++)
                    grid[i][j][k] = flat[i + si * (j + sj * k)];
    }

    private static native boolean nativeAvailable();
    private static native boolean nativeRelax(int nI, int nJ, int nK,
            double h2i, double diffusivity,
            double[] u, double[] bl, double[] rd, double[] rhs, double[] reac, double[] diffReac,
            int nIter);
}
