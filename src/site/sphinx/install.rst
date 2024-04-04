.. meta::
   :description: Java Software Library for rewriting Big RDBMS Queries into Duck DB compatible queries.
   :keywords: java sql query transpiler DuckDB H2 BigQuery Snowflake Redshift

**********************************************
Installation of JSQLTranspiler
**********************************************

Git
===================
.. code:: Bash

   git clone https://github.com/starlake-ai/JSQLTranspiler.git
   cd JSQLTranspiler
   ./gradlew build


Maven Repo
===================

.. tab:: Maven Release

    .. code-block:: xml
        :substitutions:

        <dependency>
            <groupId>com.starlake-ai.jsqltranspiler</groupId>
            <artifactId>jsqltranspiler</artifactId>
            <version>|JSQLTRANSPILER_VERSION|</version>
        </dependency>

.. tab:: Maven Snapshot

    .. code-block:: xml
        :substitutions:

        <repositories>
            <repository>
                <id>jsqltranspiler-snapshots</id>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
                <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
            </repository>
        </repositories>
        <dependency>
            <groupId>com.starlake-ai.jsqltranspiler</groupId>
            <artifactId>jsqltranspiler</artifactId>
            <version>|JSQLTRANSPILER_SNAPSHOT_VERSION|</version>
        </dependency>

.. tab:: Gradle Stable

    .. code-block:: groovy
        :substitutions:

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'com.starlake-ai.jsqltranspiler:jsqltranspiler:|JSQLTRANSPILER_VERSION|'
        }

.. tab:: Gradle Snapshot

    .. code-block:: groovy
        :substitutions:

        repositories {
            maven {
                url = uri('https://s01.oss.sonatype.org/content/repositories/snapshots/')
            }
        }

        dependencies {
            implementation 'com.starlake-ai.jsqltranspiler:jsqltranspiler:|JSQLTRANSPILER_SNAPSHOT_VERSION|'
        }



Download
===================

Static Binaries
---------------------------------------------

.. list-table:: Static Binaries Direct Download Links
   :widths: 35 50 15
   :header-rows: 1

   * - Operating System
     - File
     - Size
   * - Java Stable Release
     - |JSQLTRANSPILER_STABLE_VERSION_LINK|
     - (80 kB)
   * - Java Development Snapshot
     - |JSQLTRANSPILER_SNAPSHOT_VERSION_LINK|
     - (80 kB)
   * - Java Fat JAR Devel. Snapshot
     - |JSQLTRANSPILER_FAT_SNAPSHOT_VERSION_LINK|
     - (1 MB)

.. note::

  On MacOS, grant an exception for a blocked app by clicking the Open Anyway button in the General pane of Security & Privacy preferences.

Native Dynamic Libraries
---------------------------------------------

   Coming soon.
