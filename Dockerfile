FROM docker.stackable.tech/stackable/hadoop:3.3.6-stackable0.0.0-dev

COPY --chown=stackable:stackable ./hdfs-opa-authorizer-1.0-SNAPSHOT-jar-with-dependencies.jar /stackable/hadoop/share/hadoop/tools/lib/
COPY --chown=stackable:stackable ./topology-provider-1.0-SNAPSHOT.jar /stackable/hadoop/share/hadoop/tools/lib/