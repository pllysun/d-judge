# 使用官方 OpenJDK 镜像作为基础镜像
FROM openjdk:21

# 设置工作目录
WORKDIR /app

# 将编译并打包后的应用程序 JAR 文件拷贝到容器中
COPY target/d-judge-0.0.1-SNAPSHOT.jar /app/d-judge-0.0.1-SNAPSHOT.jar

# 挂载数据库文件和日志文件目录
VOLUME /app/db
VOLUME /app/logs
VOLUME /app/file

# 暴露应用程序使用的端口
EXPOSE 6005
EXPOSE 5005

# 启动应用程序
CMD ["java", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","d-judge-0.0.1-SNAPSHOT.jar"]
