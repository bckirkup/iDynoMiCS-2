# Fork notes: cleanup and GPU-oriented structure

This fork applies minor cleanups identified in a code review and structures the reaction–diffusion solver for optional GPU acceleration. Behaviour and results match the upstream CPU solver when the GPU library is not used.

**See also:** [README.MD](README.MD) (build, test, documentation index), [Docs/GPU_ACCELERATION.md](Docs/GPU_ACCELERATION.md), [Docs/TEST_ASSESSMENT.md](Docs/TEST_ASSESSMENT.md).

## Cleanups

- **Typos:** `concatinate` → `concatenate`, `suplied` → `supplied`, `precission` → `precision`, `storrage` → `storage`, `un-expected` → `unexpected`, `ia` → `is` (LICENSE.MD), `summorized` → `summarized` (README).
- **PDEWrapper concurrency:** Shared thread pool for concurrent pH calculations (no new pool per call); exceptions logged via `Log.out(Tier.CRITICAL, …)`; `InterruptedException` restores interrupt status.
- **Build:** Removed inclusion of Java source files in Maven `<resources>` (redundant with compiled classes).
- **Naming:** Renamed folder `protocol/Depreciated test protocols (not maintained)` to `protocol/Deprecated test protocols (not maintained)` (correct spelling: “deprecated” = no longer recommended; “depreciated” = decrease in value).

## GPU-oriented changes

- **Pluggable solver backend:** New interface `solver.mgFas.MultigridSolverBackend`; `Multigrid` implements it. `PDEWrapper.createMultigridBackend()` returns `MultigridCudaBackend`, which uses the GPU when the native library is available.
- **GPU backend implementation:**
  - **Native library** in `native/`: CUDA kernel for the red–black relaxation stencil and JNI bridge. Build with CMake; see `native/README.md`. Output: `idynomics_mg_cuda.dll` / `libidynomics_mg_cuda.so` / `idynomics_mg_cuda.dylib`.
  - **Java:** `solver.mgFas.GpuRelax` loads the library and runs `relaxBatch()` for each solute; `MultigridCudaBackend` extends `Multigrid` and overrides `relax(int nIter)` to use the GPU when available. If the library is absent or CUDA is unavailable, the same backend falls back to the CPU path.
  - **Loading:** Use `java.library.path` or `-Didynomics.gpu.native.path=/path/to/library`.
- **Documentation:** `Docs/GPU_ACCELERATION.md` updated with the implemented backend and build/load instructions.

When the library is present and CUDA is available, only the relaxation kernel runs on the GPU; reaction rates are updated once per relax batch per level (instead of every iteration) to reduce sync.

## Testing

Unit and integration tests were added or expanded: **HelperTest** (utility methods), **GpuRelaxTest** (GPU path without native lib), **SolverBackendTest** (backend type), **ReactionDiffusionOneStepTest** (one-step integration). The build job in CI runs `mvn test` so all tests run from the repo. See [Docs/TEST_ASSESSMENT.md](Docs/TEST_ASSESSMENT.md).

## Documentation

- **[Docs/README.md](Docs/README.md)** — Index of documentation in `Docs/`.
- **[Docs/GPU_ACCELERATION.md](Docs/GPU_ACCELERATION.md)** — GPU backend, build, and usage.
- **[Docs/PDEsolverNotes.md](Docs/PDEsolverNotes.md)** — PDE solvers (transient vs steady-state).
- **[native/README.md](native/README.md)** — Building the optional CUDA native library.
