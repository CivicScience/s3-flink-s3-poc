FROM flink

ADD ./ /opt/flink/usrlib/

RUN mkdir -p /opt/flink/plugins/flink-s3-fs-hadoop
RUN ln -fs /opt/flink/opt/flink-s3-fs-hadoop-*.jar /opt/flink/plugins/flink-s3-fs-hadoop/.

ENV ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.16.0.jar

