cmake_minimum_required(VERSION 3.18)
project(idynomics_mg_cuda LANGUAGES CXX CUDA)

set(CMAKE_CUDA_STANDARD 14)

# JNI: find Java to get include path
find_package(JNI REQUIRED)

add_library(idynomics_mg_cuda SHARED
  src/relax_kernel.cu
  src/relax_jni.cpp
)

target_include_directories(idynomics_mg_cuda PRIVATE
  ${JNI_INCLUDE_DIRS}
)

target_link_libraries(idynomics_mg_cuda PRIVATE
  CUDA::cudart
)

# Output name matches System.loadLibrary("idynomics_mg_cuda")
set_target_properties(idynomics_mg_cuda PROPERTIES
  PREFIX ""
  OUTPUT_NAME "idynomics_mg_cuda"
  CUDA_SEPARABLE_COMPILATION ON
)

if(WIN32)
  set_target_properties(idynomics_mg_cuda PROPERTIES SUFFIX ".dll")
elseif(APPLE)
  set_target_properties(idynomics_mg_cuda PROPERTIES SUFFIX ".dylib")
else()
  set_target_properties(idynomics_mg_cuda PROPERTIES PREFIX "lib" SUFFIX ".so")
endif()
