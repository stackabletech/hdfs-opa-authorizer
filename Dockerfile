FROM docker.stackable.tech/stackable/hadoop:3.3.6-stackable0.0.0-dev

ADD ./target/hdfs-opa-authorizer-1.0-SNAPSHOT-jar-with-dependencies.jar /stackable/authorizer

#COPY target/hdfs-opa-authorizer-1.0-SNAPSHOT-jar-with-dependencies.jar /stackable/hadoop/share/hadoop/tools/lib/