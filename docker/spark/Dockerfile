############################################################
# SPARK_STANDALONE
############################################################

# start with the base java:8-jre image (debian)
FROM java:8-jre

# prevent spark from running in background
ENV SPARK_NO_DAEMONIZE=true

# install spark 2.2
RUN wget https://archive.apache.org/dist/spark/spark-2.2.0/spark-2.2.0-bin-hadoop2.7.tgz && \
    tar xvzf spark-2.2.0-bin-hadoop2.7.tgz && \
    mv spark-2.2.0-bin-hadoop2.7 spark && \
    rm spark-2.2.0-bin-hadoop2.7.tgz

# install lynx
RUN apt update && \
    apt-get -y install lynx

# entrypoint script
COPY entrypoint.sh /
RUN chmod 755 /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
