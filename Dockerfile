FROM maven:3.8.1-openjdk-11 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM tomcat:9.0.60-jdk11
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=builder /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# 將 Flag 檔案放置於系統根目錄 /flag.txt
COPY secret_zone/flag.txt /flag.txt

EXPOSE 8080
CMD ["catalina.sh", "run"]
