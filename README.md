Serenity is a Java code coverage and code metrics library, tool and Jenkins plugin, with dynamic byte code instrumentation, 
and introducing continuous profiling. Configuring Serenity with Jenkins is a snap, four easy steps.

1) Download: the [Serenity](https://github.com/jenkinsci/serenity-plugin/blob/master/releases/serenity-1.2.zip) and unpack 
it in the same directory as your build.xml or pom.xml.
2) Add properties: for which packages you want to generate code metrics for, and the adapters that you would like to use by 
adding this to the build.xml or pom.xml for

Ant builds:

<!-- Serenity system properties. -->
<sysproperty key="included.packages" value="your.package.name.here" />
<sysproperty key="included.adapters" value="coverage:complexity:dependency" />
Maven builds:

<properties>
	<included.packages>-Dincluded.packages=your.package.name.here</included.packages>
	<included.adapters>-Dincluded.adapters=coverage:complexity:dependency</included.adapters>
</properties>
3) Add agent: to the command line for Ant like this:

<!-- Serenity JVM command line. -->
<jvmarg line="-javaagent:serenity/serenity.jar" />
And for Maven like this:

<argLine>-javaagent:serenity/serenity.jar -Xms512m -Xmx1024m ${included.packages} ${included.adapters}</argLine>

4) Jenkins configuration: page for the project in Jenkins check the Serenity box at the bottom. First install 
the plugin from the Jenkins download/install plugin page.

And that is it, run as normal. The coverage and code metrics for all modules will be collected, aggregated and made 
available in the Jenkins GUI, in reports, graphs and visualization aids.

Please note that serenity, and coverage in general, is for unit tests, not integration tests. It is advisable to not use 
serenity in integration tests and enhancing code twice, once for transactions and dependency injection, and again for coverage
has some unusual side effects.

System properties that are available and can be set are:
1) included.packages - the package names that are to be collected, semi-colon separated list(mandatory)
2) included.adapters - the class adapters that will generate the collection data for the classes, semi-colon separated list(mandatory)
3) write.classes - whether the modified classes are to be written to the file system for later inspection, true or false(optional)
4) clean.classes - whether to delete the old class files on the file system before writing the new ones, true or false(optional)
5) included.jars - jar files that are to be included in the accumulation, for source files if the source is not added to the 
build jars during the build(optional)
6) dump - dumps the database to the output at the end of the job
7) delete - deletes the database file before the execution, this is to prevent integration jobs overwriting the unit test database. So 
if there are integration tests running directly after the unit tests, in the same directory then set this parameter to false for the 
integration tests.

Full example configuration of Serenity for an Ant build:
To enable the JavaAgent the Ant build script needs to be modified to include the agent as in the following:

<jvmarg line="-javaagent:serenity/serenity.jar" />

Below is the build.xml fragment from the FindBugs project running Serenity as an example.

``` xml
<target name="test" depends="junittests,jars">
	<echo>Running JUnit test cases for FindBugs...</echo>
	<junit fork="true" printsummary="true" showoutput="true" filtertrace="true" forkmode="once">
		<!-- Serenity system properties. -->
		<sysproperty key="included.packages" value="edu.umd.cs.findbugs" />
		<sysproperty key="included.adapters" value="coverage,complexity,dependency" />

        <!-- Serenity JVM command line. -->
		<jvmarg line="-javaagent:serenity/serenity.jar" />

		<formatter type="xml" />
		<classpath refid="tools.classpath" />
		<classpath>
			<pathelement path="${junitclasses.dir}" />
		</classpath>
		<!-- And run the tests. -->
		<batchtest todir="build/junit" haltonerror="false" haltonfailure="false">
			<fileset dir="${junitsrc.dir}">
				<include name="**/*Test*.java" />
			</fileset>
		</batchtest>
	</junit>
</target>

```

Full example configuration of Serenity for a Maven build:
As in the Ant configuration there are system parameters that can be set, and those that need to be set. Below 
is a fragment from a pom configured to run Serenity during the test phase of Maven:

```
<properties>
	<included.packages>-Dincluded.packages=com.google</included.packages>
	<included.adapters>-Dincluded.adapters=coverage:complexity:dependency</included.adapters>
</properties>
```

```
<build>
	...
	<plugins>
		...
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-surefire-plugin</artifactId>
			<configuration>
				<forkMode>once</forkMode>
				<argLine>-javaagent:serenity/serenity.jar -Xms512m -Xmx1024m ${included.packages} ${included.adapters}</argLine>
			</configuration>
		</plugin>
		...
	</plugins>
	...
</build>
```

As can be seen from the above, properties are defined for the Serenity properties and used in the command line for the Surefire plugin.

Additional technical details, configuration details and gotchas!
Serenity runs as a JavaAgent, as such the byte code is modified in memory. Serenity will generate statistics on coverage, 
complexity, (in)stability, abstractness and distance from main for the project depending on the adapters defined in the system property 
'included.adapters'. The instrumented classes can be written to the file system to be validated visually by setting a system property 
'write.classes'. The class files will be written to the 'serenity' folder.

The JUnit task in both Ant and Maven needs to be in forked mode, and typically with the option 'once' set to true. Starting a new 
JVM for every test will be very time consuming and data will be overwritten with each JVM, i.e. the results are undefined.

The command line for Ant and Maven must have no spaces or page breaks, i.e. one long command. The JVM doesn't like page breaks and 
complains bitterly about the agent not starting etc.

If no adapters are added, or the names for the adapters are wrongly spelled then no metrics will be generated for the classes. Generally 
all the adapters are added. The coverage adapter is the most sought after, and incidentally the most expensive for performance, but the 
others are not very expensive at all as it turns out so there is no harm in adding them too.

In the plugin, viewing of the source and the covered lines is desirable. To include source the source must be in the jars that are 
generated and are on the classpath, not in folders. Alternatively the source can be in the 'included.jars' property. The jars that are 
specified here must then contain not only the source but the class files too.

Serenity will maintain all the data in memory until the JVM for the unit tests shuts down. As such, depending on the size of the project, 
the memory needs to be set appropriately. This can be set on the command line for the Maven build and for Ant in the 'jvmarg' tag as in 
the following:

Maven
```
<argLine>-javaagent:serenity/serenity.jar -Xms512m -Xmx1024m ${included.packages} ${included.adapters}</argLine>
```

Ant
```
<jvmarg value="-XX:PermSize=256m" />
<jvmarg value="-XX:MaxPermSize=512m" />
<jvmarg value="-Xms768m" />
<jvmarg value="-Xmx1024m" />
```

Typically performance for the tests will be p = p * 3 with the coverage added. Generally however unit tests are fast and not performance 
sensitive.

This is a screen shot of the Serenity trend result and the report in Jenkins.

![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png "Main page")

Code tree and source coverage visualized.

![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png "Detail page")