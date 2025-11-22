FROM maven:3.9.9-eclipse-temurin-21

#ENV variable for container
ENV TEST_PROFILE=api
ENV APIBASEURL=http://{REPLACE_TO_CORRECT}:4111
ENV UIBASEURL=http://{REPLACE_TO_CORRECT}:3000

#container working directory
WORKDIR /app

#copy pom file
COPY pom.xml .

# download depenedecy and cache them
RUN mvn dependency:go-offline

##copying whole project
COPY . .

USER root

CMD ["bash", "-c", "\
set +e; \
mkdir -p /app/logs; \
{ \
echo '>>> Running tests with profile: ${TEST_PROFILE}'; \
mvn test -P ${TEST_PROFILE}; \
echo '>>> Running surefire-report:report'; \
mvn -DskipTests=true surefire-report:report; \
} 2>&1 | tee /app/logs/run.log; \
echo 'DONE' \
"]