# Multigrid FAS (Full Approximation Storage)

The reaction–diffusion solver in this package uses a **multigrid** method with **Full Approximation Storage (FAS)**, following Brandt’s algorithm (see e.g. *Numerical Recipes in C*, pp. 882).

- **FMG-FAS:** Full Multigrid (FMG) with Full Approximation Scheme (FAS).
- **V-cycles:** Coarsening and refinement with pre- and post-smoothing (red–black relaxation) at each level.
- **GPU:** The relaxation kernel can be run on a CUDA GPU when the native library is built and loaded; see [Docs/GPU_ACCELERATION.md](../../../Docs/GPU_ACCELERATION.md) and [native/README.md](../../../native/README.md).

For the role of PDE solvers in iDynoMiCS 2 (transient vs steady-state), see [Docs/PDEsolverNotes.md](../../../Docs/PDEsolverNotes.md).
