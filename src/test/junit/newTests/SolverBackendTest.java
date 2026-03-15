package test.junit.newTests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import processManager.library.PDEWrapper;
import solver.mgFas.MultigridCudaBackend;
import solver.mgFas.MultigridSolverBackend;

/**
 * Tests that the default PDE solver backend is GPU-capable and implements
 * the pluggable interface. Does not require CUDA or native library.
 */
public class SolverBackendTest {

    @Test
    public void createMultigridBackendReturnsCudaBackend() throws Exception {
        PDEWrapper wrapper = new PDEWrapper();
        Method m = PDEWrapper.class.getDeclaredMethod("createMultigridBackend");
        m.setAccessible(true);
        MultigridSolverBackend backend = (MultigridSolverBackend) m.invoke(wrapper);
        assertNotNull(backend);
        assertTrue("Default backend should be MultigridCudaBackend for GPU fallback",
                backend instanceof MultigridCudaBackend);
    }

    @Test
    public void cudaBackendIsMultigridSolverBackend() {
        MultigridSolverBackend backend = new MultigridCudaBackend();
        assertNotNull(backend);
        assertTrue(backend instanceof MultigridCudaBackend);
    }
}
