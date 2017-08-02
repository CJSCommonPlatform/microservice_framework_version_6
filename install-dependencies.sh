#!/usr/bin/env bash

# Script to checkout and install locally all Microservice Framework dependencies

#Build versions
# maven-super-pom
maven_super_pom_versions=("release-1.0.0")

# maven-parent-pom
maven_parent_pom_versions=("release-1.1.0" "release-1.2.0" "release-1.4.0" "release-1.4.1" "release-1.5.0")

# maven-common-bom
maven_parent_bom_versions=("release-1.9.0" "release-1.13.0" "release-1.15.0" "release-1.16.0")

# maven-common
maven_common_versions=("release-1.6.4" "release-1.6.10" "release-1.6.11")

# raml-maven
raml_maven_versions=("release-1.5.0")

# embedded-artemis
embedded_artemis_versions=("release-1.0.0")

# maven-framework-parent-pom
maven_framework_parent_pom_versions=("release-1.2.0" "release-1.3.0" "release-1.5.0")

# test-utils
test_utils_versions=("release-1.2.0" "release-1.4.0")

# utilities
utilities_versions=("release-1.6.0")

# file-service
file_service_versions=("release-1.8.0")

# json-schema
json_schema_versions=("release-1.4.1.MoJ.Fork")

# Checkout and install given version
function installVersion {
    git checkout ${1}
    mvn clean install
}

# Checkout and install array of versions
function installVersions {
    git checkout master
    git pull
    versions=("$@")
    for version in ${versions[@]}; do
        installVersion ${version}
    done
}

cd ../maven-super-pom
installVersions ${maven_super_pom_versions[@]}

cd ../maven-parent-pom
installVersions ${maven_parent_pom_versions[@]}

cd ../maven-common-bom
installVersions ${maven_parent_bom_versions[@]}

cd ../maven-common
installVersions ${maven_common_versions[@]}

cd ../raml-maven
installVersions ${raml_maven_versions[@]}

cd ../embedded-artemis
installVersions ${embedded_artemis_versions[@]}

cd ../maven-framework-parent-pom
installVersions ${maven_framework_parent_pom_versions[@]}

cd ../test-utils
installVersions ${test_utils_versions[@]}

cd ../utilities
installVersions ${utilities_versions[@]}

cd ../file-service
installVersions ${file_service_versions[@]}

cd ../json-schema
installVersions ${json_schema_versions[@]}

cd ../microservice_framework
