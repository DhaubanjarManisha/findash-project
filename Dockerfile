# --- Stage 1: Build ---
# Use the JDK version to compile
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy source code
COPY src/Main.java ./src/

# Compile
RUN javac src/Main.java

# --- Stage 2: Runtime ---
# Use the JRE version (smaller) to run
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r fintech && useradd -r -g fintech fintech

# Copy compiled classes from builder
COPY --from=builder /app/src/*.class ./src/

# Set defaults
ENV APP_VERSION="v1.0"
ENV BG_COLOR="#ADD8E6"

# Switch user
USER fintech

EXPOSE 8080

# Run the app
CMD ["java", "src.Main"]