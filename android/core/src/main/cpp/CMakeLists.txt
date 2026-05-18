cmake_minimum_required(VERSION 3.22.1)

project("core")

message("CMAKE_SOURCE_DIR ${CMAKE_SOURCE_DIR}")

message("CMAKE_BUILD_TYPE ${CMAKE_BUILD_TYPE}")

if (NOT "${CMAKE_BUILD_TYPE}" STREQUAL "Debug")
    set(CMAKE_BUILD_TYPE Release CACHE STRING "Build type" FORCE)
    add_compile_options(-O3 -flto -g0 -fno-exceptions -fno-rtti)
    add_link_options(-flto -Wl,--gc-sections,--strip-all)
endif ()

set(LIB_CLASH_JNI_PATH "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libclash.so")
set(LIB_CLASH_ROOT_PATH "${CMAKE_SOURCE_DIR}/../../../../../libclash/android/${ANDROID_ABI}/libclash.so")

if (EXISTS "${LIB_CLASH_JNI_PATH}")
    set(LIB_CLASH_PATH "${LIB_CLASH_JNI_PATH}")
    set(LIB_CLASH_INCLUDE_PATH "${CMAKE_SOURCE_DIR}/../cpp/includes/${ANDROID_ABI}")
elseif (EXISTS "${LIB_CLASH_ROOT_PATH}")
    set(LIB_CLASH_PATH "${LIB_CLASH_ROOT_PATH}")
    set(LIB_CLASH_INCLUDE_PATH "${CMAKE_SOURCE_DIR}/../../../../../libclash/android/includes/${ANDROID_ABI}")
else ()
    message(FATAL_ERROR "Required libclash.so not found for ABI ${ANDROID_ABI}. Run setup.dart android --out core first.")
endif ()

message("LIB_CLASH_PATH ${LIB_CLASH_PATH}")

get_filename_component(LIB_CLASH_DIR "${LIB_CLASH_PATH}" DIRECTORY)

add_compile_definitions(LIBCLASH)
include_directories(${LIB_CLASH_INCLUDE_PATH})
link_directories(${LIB_CLASH_DIR})
add_library(${CMAKE_PROJECT_NAME} SHARED
        jni_helper.cpp
        core.cpp)
target_link_libraries(${CMAKE_PROJECT_NAME}
        clash)
