### CMAKE
project( mpw C )
cmake_minimum_required( VERSION 3.0.2 )


### CONFIGURATION
# Features.
option( USE_SODIUM              "Implement crypto functions with sodium (depends on libsodium)." ON )
option( USE_JSON                "Support JSON-based user configuration format (depends on libjson-c)." ON )
option( USE_COLOR               "Colorized identicon (depends on libncurses)." ON )
option( USE_XML                 "XML parsing (depends on libxml2)." ON )

option( BUILD_MPW               "C CLI version of Master Password (needs: mpw_sodium, optional: mpw_color, mpw_json)." ON )
option( BUILD_MPW_BENCH         "C CLI Master Password benchmark utility (needs: mpw_sodium)." OFF )
option( BUILD_MPW_TESTS         "C Master Password algorithm test suite (needs: mpw_sodium, mpw_xml)." OFF )

# Default build flags.
set( CMAKE_BUILD_TYPE           Release )
set( CMAKE_C_FLAGS              "-O3" )

# Version.
file( READ                      "VERSION" mpw_version )
add_definitions(                -DMP_VERSION=${VERSION} )


### DEPENDENCIES
function( use_mpw_sodium t r )
    if( USE_SODIUM )
        target_link_libraries( ${t} sodium )
        target_compile_definitions( ${t} PUBLIC -DMPW_SODIUM=1 )
        message(STATUS      "${t}: USE_SODIUM is enabled.")

    elseif( r STREQUAL "required" )
        message(FATAL_ERROR "${t}: USE_SODIUM was required but is not enabled.  Please enable the option or remove this target.")

    else()
        message(STATUS      "${t}: USE_SODIUM is supported but not enabled.")

    endif()
endfunction()

function( use_mpw_color t )
    if( USE_COLOR )
        target_link_libraries( ${t} curses)
        target_compile_definitions( ${t} PUBLIC -DMPW_COLOR=1 )
        message(STATUS      "${t}: USE_COLOR is enabled.")

    elseif( r STREQUAL "required" )
        message(FATAL_ERROR "${t}: USE_COLOR was required but is not enabled.  Please enable the option or remove this target.")

    else()
        message(STATUS      "${t}: USE_COLOR is supported but not enabled.")

    endif()
endfunction()

function( use_mpw_json t )
    if( USE_JSON )
        target_link_libraries( ${t} json-c)
        target_compile_definitions( ${t} PUBLIC -DMPW_JSON=1 )
        message(STATUS      "${t}: USE_JSON is enabled.")

    elseif( r STREQUAL "required" )
        message(FATAL_ERROR "${t}: USE_JSON was required but is not enabled.  Please enable the option or remove this target.")

    else()
        message(STATUS      "${t}: USE_JSON is supported but not enabled.")

    endif()
endfunction()

function( use_mpw_xml t r )
    find_package( LIBXML2 )
    if( USE_XML )
        if ( LIBXML2_FOUND )
            target_include_directories( ${t} PUBLIC ${LIBXML2_INCLUDE_DIR} )
            target_link_libraries( ${t} ${LIBXML2_LIBRARIES} )
            target_compile_definitions( ${t} PUBLIC -DMPW_XML=1 ${LIBXML2_DEFINITIONS} )
            message(STATUS      "${t}: USE_XML is enabled.")

        elseif( r STREQUAL "required" )
            message(FATAL_ERROR "${t}: USE_XML was enabled but is missing libxml2.  Please install this library before continuing.")

        else()
            message(WARNING     "${t}: USE_XML was enabled but is missing libxml2.  Will continue with USE_XML disabled!")

        endif()

    elseif( r STREQUAL "required" )
        message(FATAL_ERROR "${t}: USE_XML was required but is not enabled.  Please enable the option or remove this target.")

    else()
        message(STATUS      "${t}: USE_XML is supported but not enabled.")

    endif()
endfunction()


### TARGET: MPW
if( BUILD_MPW )
    # target
    add_executable( mpw "core/base64.c" "core/mpw-algorithm.c" "core/mpw-types.c" "core/mpw-util.c" "core/mpw-marshal-util.c" "core/mpw-marshal.c"
                        "cli/mpw-cli-util.c" "cli/mpw-cli.c" )
    target_include_directories( mpw PUBLIC core cli )

    # dependencies
    use_mpw_sodium( mpw required )
    use_mpw_color( mpw optional )
    use_mpw_json( mpw optional )
endif()


### TARGET: MPW-BENCH
if( BUILD_MPW_BENCH )
    # target
    add_executable( mpw_bench "core/base64.c" "core/mpw-algorithm.c" "core/mpw-types.c" "core/mpw-util.c"
                        "cli/mpw-bench.c" )
    target_include_directories( mpw_bench PUBLIC core cli )

    # dependencies
    use_mpw_sodium( mpw_bench required )
endif()


### TARGET: MPW-TESTS
if( BUILD_MPW_TESTS )
    # target
    add_executable( mpw_tests "core/base64.c" "core/mpw-algorithm.c" "core/mpw-types.c" "core/mpw-util.c"
                              "cli/mpw-tests-util.c" "cli/mpw-tests.c" )
    target_include_directories( mpw_tests PUBLIC core cli )

    # dependencies
    use_mpw_sodium( mpw_tests required )
    use_mpw_xml( mpw_tests required )
endif()
