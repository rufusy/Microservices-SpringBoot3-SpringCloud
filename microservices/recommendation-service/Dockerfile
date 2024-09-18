FROM eclipse-temurin:17.0.5_8-jre-focal as builder
WORKDIR /extracted
ADD ./build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

FROM eclipse-temurin:17.0.5_8-jre-focal
WORKDIR /application
COPY --from=builder /extracted/app/dependencies/ ./
COPY --from=builder /extracted/app/spring-boot-loader/ ./
COPY --from=builder /extracted/app/snapshot-dependencies/ ./
COPY --from=builder /extracted/app/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]


