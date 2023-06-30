# 指定基础镜像
FROM centos:8

# 环境变量，Java安装目录
ENV JAVA_DIR=/usr/local

# 拷贝JDK文件
COPY ./openjdk-17+35_linux-x64_bin.tar.gz $JAVA_DIR/

# 安装
RUN cd $JAVA_DIR \
  && tar -zxf ./openjdk-17+35_linux-x64_bin.tar.gz

# 环境变量
ENV JAVA_HOME=$JAVA_DIR/jdk-17
ENV PATH=$PATH:$JAVA_HOME/bin