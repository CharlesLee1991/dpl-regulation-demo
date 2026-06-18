FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -q

FROM tomcat:9.0-jdk11
# 기존 ROOT 앱 제거 후 regulation war 배포
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/regulation.war /usr/local/tomcat/webapps/ROOT.war

# UTF-8 기본 설정
RUN sed -i 's/port="8080"/port="8080" URIEncoding="UTF-8"/' \
    /usr/local/tomcat/conf/server.xml

EXPOSE 8080
CMD ["catalina.sh", "run"]
