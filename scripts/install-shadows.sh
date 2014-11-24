#!/bin/bash
#
# Build the shadows-core jar with the correct profiles.
#

set -e
mvn clean

(
cd robolectric-processor
mvn clean javadoc:javadoc source:jar install -DskipTests
)

(
cd robolectric-shadows/shadows-core
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-15 -DskipTests
)

(
cd robolectric-shadows/shadows-core
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-16 -DskipTests
)

(
cd robolectric-shadows/shadows-core
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-17 -DskipTests
)

(
cd robolectric-shadows/shadows-core
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-18 -DskipTests
)

(
cd robolectric-shadows/shadows-core
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-19 -DskipTests
)

mvn source:jar install
