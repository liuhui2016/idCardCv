#!/bin/bash
basedir=$(cd `dirname $0`;pwd)
host_ip=`python -c "import socket;print([(s.connect(('8.8.8.8', 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1])"`
 
 
name='autotest'
image='autotest:v1.0'
docker stop $name
docker rm $name
#docker run -it --name $name $image /bin/bash
 
docker run -d --name $name -p 8080:8080 -h $name \
           --add-host=mysql_host:$host_ip \
           --add-host=mongo_host:$host_ip \
           --add-host=redis_host:$host_ip \
           $image
