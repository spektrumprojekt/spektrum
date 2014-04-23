SET JAVA_OPTS=-Dcom.communote.configuration.dir=E:/tlu/work/spektrum/communote-db-access/communote
SET JAVA_OPTS=%JAVA_OPTS% -Dcom.communote.mstream.log.dir=E:/tlu/work/spektrum/communote-db-access/communote/
SET JAVA_OPTS=%JAVA_OPTS% -Xms1024m -Xmx1024m -XX:MaxPermSize=256m -Djava.awt.headless=true

SET CP=communote-plugins-mstream-evaluation-0.1-SNAPSHOT-executable.jar

SET MAIN=com.communote.plugins.mystream.evaluation.communotedb.CommunoteDBAccess

java %JAVA_OPTS% -cp %CP% %MAIN%
