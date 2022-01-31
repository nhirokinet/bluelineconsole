#!/bin/bash -eux

SRC_TOP="$(dirname "$0")/.."

gradle_compile_version=$(grep 'compileSdkVersion [1-9]' "$SRC_TOP/app/build.gradle" | sed -e 's/^.*compileSdkVersion //')
gradle_tool_version=$(grep 'buildToolsVersion '\''[1-9]' "$SRC_TOP/app/build.gradle" | sed -e 's/^.*buildToolsVersion '\''//' -e 's/'\''$//')

gitlab_compile_version=$(grep ANDROID_COMPILE_SDK: "$SRC_TOP/.gitlab-ci.yml" | sed -e 's/^.*ANDROID_COMPILE_SDK: "//' -e 's/"$//')
gitlab_tool_version=$(grep ANDROID_BUILD_TOOLS: "$SRC_TOP/.gitlab-ci.yml" | sed -e 's/^.*ANDROID_BUILD_TOOLS: "//' -e 's/"$//')

test "$gradle_compile_version" = "$gitlab_compile_version"
test "$gradle_tool_version" = "$gitlab_tool_version"
