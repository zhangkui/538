# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 拷贝Maven配置（使用阿里云镜像加速）
COPY settings.xml /usr/share/maven/ref/settings.xml

# 先拷贝pom.xml，利用Docker缓存加速
COPY pom.xml .
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline -B

# 拷贝源码并构建
COPY src ./src
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制jar文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
