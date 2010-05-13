set MAVEN_OPTS=-Xms512m -Xmx1024m -XX:MaxPermSize=256m

# mvn -DskipTests=true package -DdryRun=true
# -B -Dresume=false -Dusername=michaelcouck -Dpassword=suineg
# mvn release:rollback
svn --non-interactive
mvn -DskipTests=true -Dresume=false -Dusername=michaelcouck -Dpassword=suineg -DXms512m -DXmx1024m -DXX:MaxPermSize=256m release:clean release:prepare release:perform