# --- FASE 1: BUILD ---
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copia el archivo pom.xml y descarga las dependencias para aprovechar la caché de Docker.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el resto del código fuente.
COPY src ./src

# Compila y empaqueta la aplicación en un archivo JAR.
RUN mvn clean package -DskipTests

# --- FASE 2: PACKAGE (la imagen final) ---
# Usa una imagen base más ligera (JRTs) para la imagen final.
# Esto reduce drásticamente el tamaño de la imagen.
FROM openjdk:24-ea-jre-slim

# Establece el directorio de trabajo para la aplicación.
WORKDIR /app

# Copia el archivo JAR compilado desde la fase 'build'.
COPY --from=build /app/target/*.jar ./app.jar

# Expone el puerto por defecto de Spring Boot. Render utilizará esto para la configuración de su proxy.
EXPOSE 8080

# Comando para ejecutar la aplicación.
# Se usa 'exec' para pasar las señales del sistema (como SIGTERM para un apagado gracioso).
ENTRYPOINT ["java", "-jar", "app.jar"]