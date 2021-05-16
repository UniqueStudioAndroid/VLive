cmake_minimum_required(VERSION 3.4.1)


## Find the Oboe package
# Build our own native library
add_library (sles-lib SHARED sles-lib.cpp )


# Specify the libraries which our native library is dependent on, including Oboe
target_link_libraries(
        sles-lib
        log
        OpenSLES
)