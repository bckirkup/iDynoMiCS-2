# GPU acceleration (reaction–diffusion solver)

This document describes the optional GPU-accelerated backend for the multigrid PDE solver. For a general documentation index, see [Docs/README.md](README.md). For the main readme and fork summary, see [README.MD](../README.MD) and [FORK_NOTES.md](../FORK_NOTES.md).

## Overview

The main computational cost in many iDynoMiCS 2 runs is the **reaction–diffusion (PDE) multigrid solver**. That work is a good fit for GPU acceleration: regular 3D stencil operations and grid transfers.

The solver uses a **pluggable backend**. The default is `MultigridCudaBackend`, which runs the red–black relaxation on the GPU when the native CUDA library is available, and falls back to the CPU implementation otherwise.

## Pluggable backend

- **Interface:** `solver.mgFas.MultigridSolverBackend`
  - `init(Domain, EnvironmentContainer, AgentContainer, ProcessDiffusion, vCycles, preSteps, coarseSteps, postSteps, autoVcycleAdjust)`
  - `initAndSolve()`
- **CPU implementation:** `solver.mgFas.Multigrid`.
- **GPU-capable implementation:** `solver.mgFas.MultigridCudaBackend` (extends `Multigrid`, uses `GpuRelax` and the native library when present).
- **Selection point:** `processManager.library.PDEWrapper.createMultigridBackend()` returns the backend (currently `MultigridCudaBackend`). If the native library is not loaded or CUDA is unavailable, the same backend uses the CPU path automatically.

## Implemented GPU backend

The **native** library and Java integration are in place:

1. **Native library** (`native/`): CUDA kernel for the red–black relaxation stencil and a JNI bridge. Build with CMake (see `native/README.md`). Produces `idynomics_mg_cuda.dll` (Windows), `libidynomics_mg_cuda.so` (Linux), or `idynomics_mg_cuda.dylib` (macOS).
2. **Java:** `solver.mgFas.GpuRelax` loads the library and exposes `relaxBatch(MultigridSolute, order, nIter)`. `MultigridCudaBackend` overrides `relax(int nIter)` to use it when available.
3. **Reaction rates** are updated once per relax batch (per level) when using the GPU, instead of every iteration, to reduce CPU–GPU sync. Restriction, interpolation, and boundary handling remain on the CPU.
4. **Loading:** Put the library on `java.library.path` or set `-Didynomics.gpu.native.path=/full/path/to/library`.

## Where GPU helps most

- **Large 3D or fine 2D grids, multiple solutes:** The PDE solve dominates; moving relaxation and grid transfers to the GPU can give large speedups (often on the order of 5–20× for the solver, depending on problem size and hardware).
- **Small grids or chemostat-only:** PDE cost is small; GPU brings little benefit.
- **Agent-heavy, mechanics-heavy runs:** The main gain is still from accelerating the PDE; agent loops and collision detection are harder to move to the GPU without larger redesigns.

## Extending the GPU implementation

The current native code only accelerates the **relax** kernel. To move more work to the GPU:

- Implement **restriction** and **interpolation** in CUDA and call them from the same JNI library so that entire V-cycles run on the device with fewer CPU–GPU syncs.
- Keep reaction-rate updates on the CPU (they depend on agent state) or implement a simplified reaction update on the GPU if your use case allows it.

The Java `Multigrid` and `MultigridSolute` code (especially `relax()`, `computeLop()`, `computeDiffLop()`, and the restrict/prolong steps) are the reference for any further GPU kernels.
