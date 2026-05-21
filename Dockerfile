FROM node:20-alpine AS css
WORKDIR /app

COPY package.json package-lock.json* postcss.config.js tailwind.config.js ./
RUN npm ci

COPY src/main/frontend ./src/main/frontend
COPY src/main/resources/templates ./src/main/resources/templates
COPY src/main/java ./src/main/java
COPY scripts ./scripts
RUN npm run build:css

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
COPY --from=css /app/src/main/resources/static/css/site.css ./src/main/resources/static/css/site.css
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
