# Using JDK8 for base image
FROM base_jdk8:v1.0
 
# author label
LABEL maintainer="Aron.li"
 
# install timezone gcc
ENV ENVIRONMENT DOCKER_PROD
RUN cd / && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
 
# cd /opt
WORKDIR /opt
 
# copy jar to /opt, and decompression
ADD idCardCv-1.jar /opt
ADD liblept.so.5 /usr/local/lib/ 
# exec java -jar idCardCv-1.jar
CMD ["java","-jar","idCardCv-1.jar"]
