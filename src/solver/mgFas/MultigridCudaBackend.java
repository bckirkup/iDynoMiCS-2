package solver.mgFas;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * Multigrid solver backend that runs the red-black relaxation on the GPU when
 * the native CUDA library is available. Falls back to the standard Java
 * implementation otherwise.
 *
 * <p>When GPU is used, reaction rates are updated once at the start of each
 * relax batch (per level) instead of every relaxation iteration, which
 * reduces CPU–GPU sync at the cost of a slight numerical difference.
 */
public class MultigridCudaBackend extends Multigrid implements MultigridSolverBackend {

    @Override
    public void relax(int nIter) {
        if (!GpuRelax.isAvailable()) {
            super.relax(nIter);
            return;
        }

        updateReacRateAndDiffRate(order);
        boolean allOk = true;
        for (int iSolute : _soluteIndex) {
            if (!GpuRelax.relaxBatch(_solute[iSolute], order, nIter)) {
                allOk = false;
                break;
            }
        }
        if (!allOk) {
            Log.out(Tier.NORMAL, "GPU relax failed for one or more solutes; falling back to CPU");
            super.relax(nIter);
        }
    }
}
