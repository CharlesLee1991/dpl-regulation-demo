FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -q

FROM tomcat:9.0-jdk17-temurin
# 기존 ROOT 앱 제거 후 regulation war 배포
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/regulation.war /usr/local/tomcat/webapps/ROOT.war

# UTF-8 기본 설정
RUN sed -i 's/port="8080"/port="8080" URIEncoding="UTF-8"/' \
    /usr/local/tomcat/conf/server.xml

# SQL Server 2012 접속을 위해 JDK 17의 TLSv1/TLSv1.1 비활성화 해제
# (SQL2012는 TLS1.0만 지원 → java.security의 disabledAlgorithms에서 제거)
RUN JS=$(find / -name java.security -path '*/conf/security/*' 2>/dev/null | head -1) && \
    echo "patching $JS" && \
    sed -i 's/\bTLSv1, TLSv1.1, //g; s/\bTLSv1, //g; s/\bTLSv1.1, //g' "$JS" && \
    grep "jdk.tls.disabledAlgorithms" "$JS" | head -1

EXPOSE 8080
CMD ["catalina.sh", "run"]
