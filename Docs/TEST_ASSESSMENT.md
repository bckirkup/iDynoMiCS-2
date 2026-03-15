# Unit test assessment

This document summarizes the unit and integration test suite and the tests added in this fork. For the documentation index, see [Docs/README.md](README.md).

## Current state

- **Layout:** Tests live under `src/test`: `junit/oldTests/` (many small unit tests), `junit/newTests/` (integration-style tests that load protocols and run the simulator), and `other/` (runners, benchmarks, not JUnit).
- **Count:** Roughly 90+ `@Test` methods across ~45 test classes, plus the new tests below. OldTests suite runs a subset (including HelperTest); CI runs selected newTests against the built JAR and runs **mvn test** in the build job so all unit and integration tests run from the repo.
- **Coverage:**
  - **Strong:** Linear algebra, ExtraMath, shapes, iterators, multigrid layers (solver.multigrid), boundaries, some PDE/solver paths, XMLable, compartments.
  - **Integration:** Reaction–diffusion (mgFas), mass balance, collision, acid–base; these load protocol XML and run the simulator.
  - **Fork additions:** Helper (concatenate, setIfNone, isNullOrEmpty, restrict, etc.), solver backend (PDEWrapper.createMultigridBackend, MultigridCudaBackend), GpuRelax (availability, flatten/unflatten), and one-step reaction–diffusion integration.
- **Expectations:** As a research codebase, coverage is uneven but reasonable; the fork adds tests to make the codebase more robust and to protect the new backend and utilities.

## What was added (fork)

### Unit tests

1. **HelperTest** (`test.junit.oldTests.HelperTest`) – In the OldTests suite. Covers: concatenate (empty+extra, with extra, order, no extra), firstToUpper, setIfNone, isNullOrEmpty, listIsNullOrEmpty, restrict, copyStringA, enumToString, removeWhitespace, stringAToString (comma and custom delimiter, null).
2. **GpuRelaxTest** (`test.junit.newTests.GpuRelaxTest`) – When native library is not loaded, isAvailable() is false; flatten/unflatten round-trip for 2D and 3D grids; class loadable (no GPU required).
3. **SolverBackendTest** (`test.junit.newTests.SolverBackendTest`) – PDEWrapper.createMultigridBackend() returns MultigridCudaBackend; MultigridCudaBackend implements MultigridSolverBackend.

### Integration tests

4. **ReactionDiffusionOneStepTest** (`test.junit.newTests.ReactionDiffusionOneStepTest`) – Loads protocol/unit-tests/reaction_diffusion_mgFas.xml, runs initialRun() and one step(); asserts biofilm glucose concentrations are finite, non-NaN, non-negative and dimensions positive. Validates the default solver backend on the full stack.

### CI

- **Build job:** Step “Run unit tests (Maven)” runs **mvn test** after the build so all tests (including the new ones) run from the repo. The existing matrix job that runs selected test classes from the artifact is unchanged.

## What could be added later (optional)

- **PDEWrapper with minimal protocol:** A test that instantiates PDEWrapper, calls init from a minimal XML fragment, and runs one internal step to assert non-NaN concentrations.
- **MultigridSolute.getH2i:** Unit test would require constructing a minimal MultigridSolute (heavy setup); currently covered indirectly by the one-step integration test.

No new tests were added for the “Deprecated” folder rename, config typos, or POM resource cleanup; those are one-off changes with no ongoing behaviour to guard.
