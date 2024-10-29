# 使用 openjdk:17 作为基础镜像
FROM openjdk:17

# 设置工作目录
WORKDIR /app

# 将本地的 JAR 文件和其他资源复制到容器内
COPY simple-client/target/simple-client-1.1.0.jar /app/app.jar
COPY libs/ /app/libs/
RUN mkdir -p /app/databasr

# 设置环境变量
ENV TZ=Asia/Shanghai

# 暴露端口（如果你的应用需要对外暴露端口）
EXPOSE 9399

# 启动应用的命令
ENTRYPOINT ["java", "-Djava.library.path=/app/libs/linux_x64/", "-jar", "/app/app.jar"]