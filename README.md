Serenity is a Java code coverage and code metrics library, tool and Jenkins plugin, with dynamic byte code instrumentation, 
and introducing continuous profiling. Configuring Serenity with Jenkins is a snap, four easy steps.

1) Download: the Serenity and unpack it in the same directory as your build.xml or pom.xml.
2) Add properties: for which packages you want to generate code metrics for, and the adapters that you would like to use by adding this to the build.xml or pom.xml for

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