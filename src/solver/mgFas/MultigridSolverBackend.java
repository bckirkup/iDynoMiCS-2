package solver.mgFas;

import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessDiffusion;

/**
 * Pluggable backend for the reaction-diffusion multigrid solver.
 * The default implementation is {@link Multigrid} (CPU). A GPU-accelerated
 * implementation can be supplied (e.g. via JNI to a native library) and
 * selected at runtime to improve performance for large 3D or fine 2D grids.
 *
 * @see Multigrid
 * @see Docs/GPU_ACCELERATION.md
 */
public interface MultigridSolverBackend {

	/**
	 * Initialise the solver with domain, environment, agents and parameters.
	 *
	 * @param domain       computational domain
	 * @param environment  environment container (solutes, etc.)
	 * @param agents       agent container
	 * @param manager      process manager (e.g. PDEWrapper) for reactions and settings
	 * @param vCycles      number of V-cycles per solve
	 * @param preSteps     pre-smoothing steps
	 * @param coarseSteps  coarsest-level steps
	 * @param postSteps    post-smoothing steps
	 * @param autoVcycleAdjust whether to auto-adjust V-cycle count
	 */
	void init(Domain domain, EnvironmentContainer environment,
			  AgentContainer agents, ProcessDiffusion manager,
			  int vCycles, int preSteps, int coarseSteps, int postSteps,
			  boolean autoVcycleAdjust);

	/**
	 * Initialise concentration fields and solve the diffusion-reaction system
	 * for the current time step.
	 */
	void initAndSolve();
}
