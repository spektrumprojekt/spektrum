JAVA_OPTS="-Dcom.communote.configuration.dir=/home/COMMUNARDO01/tlu/evaluation/communote"
JAVA_OPTS="$JAVA_OPTS -Dcom.communote.mstream.log.dir=/home/COMMUNARDO01/tlu/evaluation/output/logs/"
JAVA_OPTS="$JAVA_OPTS -Xms1024m -Xmx1024m -XX:MaxPermSize=256m -Djava.awt.headless=true"

CP=communote-plugins-mstream-evaluation-0.1-SNAPSHOT-executable.jar

MAIN=com.communote.plugins.mystream.evaluation.communotedb.CommunoteDBAccess

java $JAVA_OPTS -cp $CP $MAIN > /home/COMMUNARDO01/tlu/evaluation/output/logs/evaluator-runner.out
