set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
# mvn -DskipTests=true package -DdryRun=true
# -B -Dresume=false -Dusername=michaelcouck -Dpassword=suineg
mvn release:prepare release:perform