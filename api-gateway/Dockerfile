# 与项目Java版本匹配的基础镜像
FROM openjdk:21-slim

# 将编译好的JAR包复制到容器中，并重命名为app.jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# 容器对外暴露的端口
# EXPOSE 8001

ENTRYPOINT ["java","-jar","/app.jar"]