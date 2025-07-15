# --- FASE 1: BUILD ---
FROM maven:3.9.10-eclipse-temurin-24 AS builder

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /app

COPY . .

# Compila y empaqueta la aplicación en un archivo JAR.
RUN mvn clean package -DskipTests

# --- FASE 2: PACKAGE (la imagen final) ---
# Usa una imagen base más ligera (JRTs) para la imagen final.
# Esto reduce drásticamente el tamaño de la imagen.
FROM eclipse-temurin:24-jre

# Establece el directorio de trabajo para la aplicación.
WORKDIR /app

# Copia el archivo JAR compilado desde la fase 'build'.
COPY --from=builder /app/target/*.jar ./app.jar

# Expone el puerto por defecto de Spring Boot. Render utilizará esto para la configuración de su proxy.
EXPOSE 8080

# Comando para ejecutar la aplicación.
# Se usa 'exec' para pasar las señales del sistema (como SIGTERM para un apagado gracioso).
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=qa"]