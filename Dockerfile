# Coklu asamali build: Maven + JDK 21 ile derle, sonra sadece JRE 21 iceren
# daha kucuk bir imajda calistir -- build araclari (Maven, kaynak kod) son
# imaja tasinmaz.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Once sadece pom.xml kopyalanip bagimliliklar indirilir -- kaynak kod
# degistiginde (pom.xml ayni kaldigi surece) bu katman Docker cache'inden
# gelir, her build'de tum bagimliliklari yeniden indirmez.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
# pom.xml <finalName>rs-grid-service</finalName> sabitliyor -- surume gore
# degisen bir jar adiyla ugrasmaya gerek yok.
COPY --from=build /build/target/rs-grid-service.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
