set MAVEN_OPTS=-Xms512m -Xmx1024m -XX:MaxPermSize=256m

# mvn -DskipTests=true package -DdryRun=true
# -B -Dresume=false -Dusername=michaelcouck -Dpassword=suineg
# mvn release:rollback

mvn -DskipTests=true -Dresume=false -DXms512m -DXmx1024m -DXX:MaxPermSize=256m release:prepare release:perform