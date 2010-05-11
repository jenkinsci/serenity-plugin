set JAVA_HOME=C:/Java/jdk1.6.0_20
set HUDSON_HOME=D:/Eclipse/workspace/serenity/work
set MAVEN_OPTS=-Xms256m -Xmx512m -XX:MaxPermSize=128m -Dorg.mortbay.http.HttpRequest.maxFormContentSize=1000000
# -Djava.util.logging.config.file=src/main/META-INF/logging.properties
mvn hpi:run