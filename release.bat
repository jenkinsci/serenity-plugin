set MAVEN_OPTS=-Xms512m -Xmx1024m -XX:MaxPermSize=256m

rem mvn -DskipTests=true package -DdryRun=true
rem -B -Dresume=false -Dusername=michaelcouck -Dpassword=suineg
rem mvn release:rollback
rem svn --non-interactive

mvn -DskipTests=true -Dresume=false -Dusername=michaelcouck -Dpassword=suineg -DXms512m -DXmx1024m -DXX:MaxPermSize=256m release:clean release:prepare release:perform