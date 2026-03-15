/**
 * JNI bridge for the CUDA relax kernel. Load from Java via System.loadLibrary("idynomics_mg_cuda").
 */

#include <jni.h>
#include <cuda_runtime.h>
#include <cstring>

extern void run_relax_redblack(
    int nI, int nJ, int nK,
    double h2i, double diffusivity, double blThresh,
    const double* bl, const double* rd, const double* rhs,
    const double* reac, const double* diffReac,
    double* u,
    int nIter);

static const double BLTHRESH = 0.1;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_solver_mgFas_GpuRelax_nativeRelax(
    JNIEnv* env, jclass clazz,
    jint nI, jint nJ, jint nK,
    jdouble h2i, jdouble diffusivity,
    jdoubleArray jU, jdoubleArray jBl, jdoubleArray jRd,
    jdoubleArray jRhs, jdoubleArray jReac, jdoubleArray jDiffReac,
    jint nIter)
{
    const int nP = (nI + 2) * (nJ + 2) * (nK + 2);

    jdouble* u     = env->GetDoubleArrayElements(jU, NULL);
    jdouble* bl    = env->GetDoubleArrayElements(jBl, NULL);
    jdouble* rd    = env->GetDoubleArrayElements(jRd, NULL);
    jdouble* rhs   = env->GetDoubleArrayElements(jRhs, NULL);
    jdouble* reac  = env->GetDoubleArrayElements(jReac, NULL);
    jdouble* diffReac = env->GetDoubleArrayElements(jDiffReac, NULL);

    if (!u || !bl || !rd || !rhs || !reac || !diffReac) {
        if (u) env->ReleaseDoubleArrayElements(jU, u, JNI_ABORT);
        if (bl) env->ReleaseDoubleArrayElements(jBl, bl, JNI_ABORT);
        if (rd) env->ReleaseDoubleArrayElements(jRd, rd, JNI_ABORT);
        if (rhs) env->ReleaseDoubleArrayElements(jRhs, rhs, JNI_ABORT);
        if (reac) env->ReleaseDoubleArrayElements(jReac, reac, JNI_ABORT);
        if (diffReac) env->ReleaseDoubleArrayElements(jDiffReac, diffReac, JNI_ABORT);
        return JNI_FALSE;
    }

    double *d_u = NULL, *d_bl = NULL, *d_rd = NULL, *d_rhs = NULL;
    double *d_reac = NULL, *d_diffReac = NULL;
    cudaError_t err;

    err = cudaMalloc((void**)&d_u, (size_t)nP * sizeof(double));
    if (err != cudaSuccess) goto cleanup_host;
    err = cudaMalloc((void**)&d_bl, (size_t)nP * sizeof(double));
    if (err != cudaSuccess) goto cleanup_dev;
    err = cudaMalloc((void**)&d_rd, (size_t)nP * sizeof(double));
    if (err != cudaSuccess) goto cleanup_dev;
    err = cudaMalloc((void**)&d_rhs, (size_t)nP * sizeof(double));
    if (err != cudaSuccess) goto cleanup_dev;
    err = cudaMalloc((void**)&d_reac, (size_t)nP * sizeof(double));
    if (err != cudaSuccess) goto cleanup_dev;
    err = cudaMalloc((void**)&d_diffReac, (size_t)nP * sizeof(double));
    if (err != cudaSuccess) goto cleanup_dev;

    cudaMemcpy(d_u, u, (size_t)nP * sizeof(double), cudaMemcpyHostToDevice);
    cudaMemcpy(d_bl, bl, (size_t)nP * sizeof(double), cudaMemcpyHostToDevice);
    cudaMemcpy(d_rd, rd, (size_t)nP * sizeof(double), cudaMemcpyHostToDevice);
    cudaMemcpy(d_rhs, rhs, (size_t)nP * sizeof(double), cudaMemcpyHostToDevice);
    cudaMemcpy(d_reac, reac, (size_t)nP * sizeof(double), cudaMemcpyHostToDevice);
    cudaMemcpy(d_diffReac, diffReac, (size_t)nP * sizeof(double), cudaMemcpyHostToDevice);

    run_relax_redblack(nI, nJ, nK, h2i, diffusivity, BLTHRESH,
                      d_bl, d_rd, d_rhs, d_reac, d_diffReac, d_u, nIter);

    cudaMemcpy(u, d_u, (size_t)nP * sizeof(double), cudaMemcpyDeviceToHost);

cleanup_dev:
    cudaFree(d_u);
    cudaFree(d_bl);
    cudaFree(d_rd);
    cudaFree(d_rhs);
    cudaFree(d_reac);
    cudaFree(d_diffReac);
cleanup_host:
    env->ReleaseDoubleArrayElements(jU, u, 0);
    env->ReleaseDoubleArrayElements(jBl, bl, JNI_ABORT);
    env->ReleaseDoubleArrayElements(jRd, rd, JNI_ABORT);
    env->ReleaseDoubleArrayElements(jRhs, rhs, JNI_ABORT);
    env->ReleaseDoubleArrayElements(jReac, reac, JNI_ABORT);
    env->ReleaseDoubleArrayElements(jDiffReac, diffReac, JNI_ABORT);

    return (err == cudaSuccess) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_solver_mgFas_GpuRelax_nativeAvailable(JNIEnv* env, jclass clazz) {
    int deviceCount = 0;
    cudaError_t err = cudaGetDeviceCount(&deviceCount);
    return (err == cudaSuccess && deviceCount > 0) ? JNI_TRUE : JNI_FALSE;
}

} /* extern "C" */
