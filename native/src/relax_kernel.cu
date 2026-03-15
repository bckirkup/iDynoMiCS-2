/**
 * CUDA kernel: one red-black Gauss-Seidel relax step for the multigrid
 * reaction-diffusion solver. Matches the stencil in MultigridSolute (Java).
 *
 * Grid layout: padded (nI+2)*(nJ+2)*(nK+2), interior [1..nI] x [1..nJ] x [1..nK].
 * Flat index: idx = i + (nI+2)*j + (nI+2)*(nJ+2)*k (row-major, i varies fastest).
 */

#include <cuda_runtime.h>

#define BLTHRESH 0.1

__device__ static int idx(int i, int j, int k, int nI, int nJ) {
    return i + (nI + 2) * (j + (nJ + 2) * k);
}

/* One thread per interior point. Only threads with (i+j+k)%2 == color do work. */
__global__ void relax_pass_kernel(
    const int nI, const int nJ, const int nK,
    const double h2i, const double diffusivity, const double blThresh,
    const int color,
    const double* __restrict__ bl,
    const double* __restrict__ rd,
    const double* __restrict__ rhs,
    const double* __restrict__ reac,
    const double* __restrict__ diffReac,
    double* __restrict__ u)
{
    const int nInterior = nI * nJ * nK;
    int linear = blockIdx.x * blockDim.x + threadIdx.x;
    if (linear >= nInterior) return;

    int k = linear / (nI * nJ);
    int r = linear % (nI * nJ);
    int j = r / nI;
    int i = r % nI;
    i += 1; j += 1; k += 1;  /* 1-based interior */

    if ((i + j + k) % 2 != color) return;
    if (bl[idx(i, j, k, nI, nJ)] < blThresh) return;

    double rd_c  = rd[idx(i, j, k, nI, nJ)];
    double rd_mi = rd[idx(i - 1, j, k, nI, nJ)];
    double rd_pi = rd[idx(i + 1, j, k, nI, nJ)];
    double rd_mj = rd[idx(i, j - 1, k, nI, nJ)];
    double rd_pj = rd[idx(i, j + 1, k, nI, nJ)];
    double rd_mk = rd[idx(i, j, k - 1, nI, nJ)];
    double rd_pk = rd[idx(i, j, k + 1, nI, nJ)];

    double d_c  = diffusivity * rd_c;
    double d_mi = diffusivity * rd_mi;
    double d_pi = diffusivity * rd_pi;
    double d_mj = diffusivity * rd_mj;
    double d_pj = diffusivity * rd_pj;
    double d_mk = diffusivity * rd_mk;
    double d_pk = diffusivity * rd_pk;

    double u_c  = u[idx(i, j, k, nI, nJ)];
    double u_mi = u[idx(i - 1, j, k, nI, nJ)];
    double u_pi = u[idx(i + 1, j, k, nI, nJ)];
    double u_mj = u[idx(i, j - 1, k, nI, nJ)];
    double u_pj = u[idx(i, j + 1, k, nI, nJ)];
    double u_mk = u[idx(i, j, k - 1, nI, nJ)];
    double u_pk = u[idx(i, j, k + 1, nI, nJ)];

    double lop = ((d_pi + d_c) * (u_pi - u_c) + (d_mi + d_c) * (u_mi - u_c) +
                  (d_pj + d_c) * (u_pj - u_c) + (d_mj + d_c) * (u_mj - u_c) +
                  (d_pk + d_c) * (u_pk - u_c) + (d_mk + d_c) * (u_mk - u_c)) * h2i
                 + reac[idx(i, j, k, nI, nJ)];

    double dlop = -h2i * (6.0 * d_c + d_pi + d_mi + d_pj + d_mj + d_pk + d_mk)
                  + diffReac[idx(i, j, k, nI, nJ)];

    double res = (lop - rhs[idx(i, j, k, nI, nJ)]) / dlop;
    double unew = u_c - res;
    if (unew < 0.0) unew = 0.0;
    u[idx(i, j, k, nI, nJ)] = unew;
}

extern "C" {

void run_relax_redblack(
    int nI, int nJ, int nK,
    double h2i, double diffusivity, double blThresh,
    const double* bl, const double* rd, const double* rhs,
    const double* reac, const double* diffReac,
    double* u,
    int nIter)
{
    const int nInterior = nI * nJ * nK;
    if (nInterior <= 0) return;

    int threads = 256;
    int blocks = (nInterior + threads - 1) / threads;

    for (int iter = 0; iter < nIter; iter++) {
        relax_pass_kernel<<<blocks, threads>>>(
            nI, nJ, nK, h2i, diffusivity, blThresh, 0,
            bl, rd, rhs, reac, diffReac, u);
        cudaDeviceSynchronize();
        relax_pass_kernel<<<blocks, threads>>>(
            nI, nJ, nK, h2i, diffusivity, blThresh, 1,
            bl, rd, rhs, reac, diffReac, u);
        cudaDeviceSynchronize();
    }
}

} /* extern "C" */
