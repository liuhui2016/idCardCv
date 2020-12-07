# 项目介绍
本项目是通过学习https://gitee.com/nbsl/idCardCv 后整合tess4j,不需要经过训练直接使用的,当然,你也可以进行训练后进行使用。该项目修改原有的需要安装opencv的过程，全部使用javaccp技术重构,通过javaccp引入需要的c++库进行开发。不需要安装opencv
新增的了前端控制识别区域的功能，新增了后端识别后验证 ，页面样式主要适应paid，重新修改了后面的识别过程，用户opencv进行图片优化和区域
选择，使用tess4j进行数字和x的识别
配合样式中的区域在后台裁剪相关区域图片
/idCardCv/src/main/resources/static/js/plugins/cropper/cropper.css
![前端效果页面](https://gitee.com/endlesshh/idCardCv/raw/master/img/1.jpg)
![前端效果页面](https://gitee.com/endlesshh/idCardCv/raw/master/img/2.jpg)
![前端效果页面](https://gitee.com/endlesshh/idCardCv/raw/master/img/3.jpg)
# 遇到问题
1、java.lang.UnsatisfiedLinkError: C:\Users\Administrator\.javacpp\cache\opencv-3.4.3-1.4.3-windows-x86_64.jar\org\bytedeco\javacpp\windows-x86_64\jniopencv_core.dll: Can't find dependent libraries
 我的问题是因为没有c++运行环境，我在img/vc_redist.x64.exe中添加了64位的运行环境

# 身份证号码识别
请求地址
http://localhost:8080/idCard/index
它基于openCV这个开源库。这意味着你可以获取全部源代码，并且移植到opencv支持的所有平台。
它是基于java开发。
它的识别率较高。图片清晰情况下，号码检测与识别准确率在90%以上。
 
 Required Software
------------
本版本在以下平台测试通过：
windows
* windows7 64bit
* jdk1.8.0_45
* junit 4
* opencv3.4.3
* javaccp1.5.3
* tess4j.4.5.1
* tesseract4.0.0
Linux
 censtos7 

# 项目更新
1、先前使用base64进行图片的上传比较缓慢，使用webuploader插件进行分片上传，网速慢的时候可以提升速度，尤其是paid浏览器使用。原页面改为idcard_bak.html。
2、原项目中有测试图片保存路径，统一更新到配置文档中。

# 在centso7或者docker-centos7中运行本项目

##1、docker镜像的制作

###1、系统镜像参考这篇文章制作

本项目镜像是参考这篇文章制作的，使用而来centos+jdk8
	https://blog.csdn.net/u012887259/article/details/110298464?utm_medium=distribute.pc_relevant.none-task-blog-title-2&spm=1001.2101.3001.4242
	
###2、基于上面的镜像，构建SpringBoot服务

####1、首先将 doc/build.sh Dockerfile restart_service.sh，resources/linux-x86-64/libept.so下的这些文件都放到和打成的jar的同一个目录下面，这些文件都是平级的。
####2、运行build.sh来构建autotest:v1.0镜像。
####3、使用restart_service.sh来运行镜像。首次启动报错，在运行一下就启动该镜像。
####4、使用docker logs -f -t --tail 100  镜像ID 来查看日志信息。
####5、请求 dockerIp:8080/idCard/index来识别身份证，根据报错信息修改libept.so文件名称，比如我的就需要libept.so.5这个文名，则将libept.so该名称为libept.so.5 ,相应的改动Dockerfile中 ADD libept.so.5 /usr/local/lib/。删除当前镜像，重新创建。

###3、docker构建SpringBoot服务
![运行效果](https://gitee.com/endlesshh/idCardCv/raw/master/img/4.png)
![运行效果](https://gitee.com/endlesshh/idCardCv/raw/master/img/5.png)
![运行效果](https://gitee.com/endlesshh/idCardCv/raw/master/img/6.png)	

 