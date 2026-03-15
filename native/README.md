# Native GPU library for iDynoMiCS 2 multigrid solver

This directory builds the **idynomics_mg_cuda** shared library used to run the red–black relaxation kernel on a CUDA-capable GPU. The solver falls back to the CPU implementation automatically if the library is not found or CUDA is unavailable.

**See also:** [Docs/GPU_ACCELERATION.md](../Docs/GPU_ACCELERATION.md) (overview and when GPU helps), [README.MD](../README.MD) (main project readme).

## Requirements

- **CUDA Toolkit** (e.g. 11.x or 12.x) with `nvcc` and `cudart` on the path
- **CMake** 3.18+
- **Java JDK** (for JNI headers; same major version as the JRE used to run iDynoMiCS)
- C++ compiler supported by CUDA (e.g. MSVC on Windows, gcc on Linux)

## Build

From this directory:

```bash
mkdir build
cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
cmake --build .
```

Output:

- **Linux:** `libidynomics_mg_cuda.so`
- **Windows:** `idynomics_mg_cuda.dll`
- **macOS:** `idynomics_mg_cuda.dylib`

## Use with iDynoMiCS 2

1. Put the built library in a directory that is on `java.library.path` when you start the JVM, **or**
2. Set a system property when launching the JVM:
   - `-Djava.library.path=/path/to/dir/containing/library`, or
   - `-Didynomics.gpu.native.path=/full/path/to/idynomics_mg_cuda.dll` (use `.so` or `.dylib` on Linux/macOS).

If the library is not found or CUDA is unavailable, the solver falls back to the CPU implementation automatically.

## What runs on the GPU

Only the **red–black Gauss–Seidel relaxation** for the reaction–diffusion multigrid is executed on the GPU. Restriction, interpolation, reaction-rate updates, and boundary handling remain on the CPU. For large 3D or fine 2D grids this still gives a substantial speedup.
