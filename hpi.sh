#export JAVA_HOME=/usr/share/java
#export HUDSON_HOME=/usr/share/eclipse/workspace/serenity/work
#export MAVEN_OPTS="-Xms256m -Xmx512m -XX:MaxPermSize=128m -Dorg.mortbay.http.HttpRequest.maxFormContentSize=1000000"
# -Djava.util.logging.config.file=src/main/META-INF/logging.properties
mvn hpi:run