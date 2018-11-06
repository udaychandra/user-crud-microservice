#!/bin/bash

# Exits if the previous command failed.
function isOK() {
    if [ $? -ne 0 ]; then
        echo "$1"
        exit 1
    fi
}

if [[ -z "${JAVA_HOME}" ]]; then
    echo "JAVA_HOME is not defined. Set it to JDK 10 or above"
    exit 1
fi

JAVA_VERSION=$("${JAVA_HOME}/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}')

if [[ "${JAVA_VERSION}" < "10.0.0" ]]; then
    echo "Requires JDK 10 or above. Set JAVA_HOME to JDK 10 or above"
    exit 1
fi

JDEPS="${JAVA_HOME}/bin/jdeps"
JLINK="${JAVA_HOME}/bin/jlink"
DEPLOY_DIR=target

echo "Cleaning deploy directory"
rm -rf "${DEPLOY_DIR}"


echo "Building Helidon based user CRUD service..."
mvn clean package
isOK "FAILED: Gradle execution failed"

echo "Preparing custom JRE image..."
cd "$DEPLOY_DIR"

JAVA_BASE_MODS=$(find . -name "*.jar" -exec jdeps --module-path libs:*.jar -s {} \; | sed -En "s/.* -> (java.*)/ \1;/p" | sort | uniq | grep -v "java.annotation" | tr -s ';\n ' ',')
cd ..

# Add java.management and jdk.unsupported to allow netty to access required classes.
"${JLINK}" --module-path "${JAVA_HOME}/jmods:target/libs:target/*.jar" \
    --add-modules java.management,jdk.unsupported"${JAVA_BASE_MODS}" \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output ${DEPLOY_DIR}/image

isOK "Unable to build custom JRE image"

