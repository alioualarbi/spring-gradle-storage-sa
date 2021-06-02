FROM openjdk:11-jdk AS build
WORKDIR /workspace/app

COPY . /workspace/app
RUN ./gradlew build
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM openjdk:11-jdk
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build /workspace/app/src/main/java/com/storage/SpringBootdockerStorage/client_secrets.json /workspace/app/build/client_secrets.json
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.storage.SpringBootdockerStorage.GoogleCloudStorage"]
