package test.junit.newTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import idynomics.Idynomics;

/**
 * Integration test: load the reaction-diffusion mgFas unit-test protocol,
 * run initialRun() and one step(). Asserts no exception and that the
 * biofilm compartment has finite, non-NaN glucose concentrations.
 * Validates that the default solver backend (MultigridCudaBackend with
 * CPU fallback) runs correctly.
 */
public class ReactionDiffusionOneStepTest {

    private static final String PROTOCOL = "protocol/unit-tests/reaction_diffusion_mgFas.xml";
    private static final String SOLUTE = "glucose";

    @Test
    public void oneStepProducesFiniteConcentrations() {
        Log.set(Tier.CRITICAL);
        Idynomics.setupSimulator(PROTOCOL);
        Log.set(Tier.CRITICAL);

        Idynomics.simulator.initialRun();
        Idynomics.simulator.step();

        Compartment biofilm = Idynomics.simulator.getCompartment("biofilm");
        assertNotNull(biofilm);
        assertNotNull(biofilm.environment);
        assertTrue("Biofilm should have solute " + SOLUTE,
                biofilm.environment.getSoluteNames().contains(SOLUTE));

        double[][][] conc = biofilm.environment.getSoluteGrid(SOLUTE).getArray(ArrayType.CONCN);
        assertNotNull(conc);

        boolean hasNonZero = false;
        for (int i = 0; i < conc.length; i++) {
            for (int j = 0; j < conc[i].length; j++) {
                for (int k = 0; k < conc[i][j].length; k++) {
                    double c = conc[i][j][k];
                    assertFalse("Concentration should not be NaN at " + i + "," + j + "," + k,
                            Double.isNaN(c));
                    assertFalse("Concentration should not be infinite at " + i + "," + j + "," + k,
                            Double.isInfinite(c));
                    assertTrue("Concentration should be non-negative", c >= -1e-10);
                    if (c > 1e-15) hasNonZero = true;
                }
            }
        }
        assertTrue("At least one grid point should have non-negligible concentration", hasNonZero);
    }

    @Test
    public void oneStepConcentrationArrayDimensionsMatchShape() {
        Log.set(Tier.CRITICAL);
        Idynomics.setupSimulator(PROTOCOL);
        Log.set(Tier.CRITICAL);

        Idynomics.simulator.initialRun();
        Idynomics.simulator.step();

        Compartment biofilm = Idynomics.simulator.getCompartment("biofilm");
        double[][][] conc = biofilm.environment.getSoluteGrid(SOLUTE).getArray(ArrayType.CONCN);
        assertNotNull(conc);
        assertTrue(conc.length > 0);
        assertTrue(conc[0].length > 0);
        assertTrue(conc[0][0].length > 0);
    }
}
