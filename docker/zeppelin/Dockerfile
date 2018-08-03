############################################################
# ZEPPELIN
############################################################

# start with the base ubuntu:16.04 image
FROM ubuntu:16.04

# install java 8
RUN apt-get update && \
    apt-get -y install software-properties-common && \
    add-apt-repository ppa:webupd8team/java && \
    apt-get update && \
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections && \
    apt-get -y install oracle-java8-installer

# install spark 2.2
RUN wget http://apache.mirror.digitalpacific.com.au/spark/spark-2.2.0/spark-2.2.0-bin-hadoop2.7.tgz && \
    tar xvzf spark-2.2.0-bin-hadoop2.7.tgz && \
    mv spark-2.2.0-bin-hadoop2.7 spark && \
    rm spark-2.2.0-bin-hadoop2.7.tgz

# install zeppelin 0.8
RUN wget http://apache.mirror.digitalpacific.com.au/zeppelin/zeppelin-0.8.0/zeppelin-0.8.0-bin-netinst.tgz && \
    tar xvzf zeppelin-0.8.0-bin-netinst.tgz && \
    mv zeppelin-0.8.0-bin-netinst zeppelin && \
    rm zeppelin-0.8.0-bin-netinst.tgz

# zeppelin config
COPY conf/ /zeppelin/conf

# entrypoint script
COPY entrypoint.sh /
RUN chmod 755 /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
