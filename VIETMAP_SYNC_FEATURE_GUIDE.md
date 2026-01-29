# VietMap Sync Feature documentation

This document provides a guide on how to setup, run, test, and troubleshoot the **VietMap Data Sync Module**.

## 1. Feature Overview
The Sync Module allows administrators to fetch restaurant data from **VietMap API v3** and save it to the local database.
- **Search**: Scans for locations based on keyword and coordinates.
- **Duplicate Check**: Skips stores that already exist in the DB (by name).
- **Detail Fallback**: If `lat`/`lng` is null in search results, it automatically calls the *Place Detail API* to fetch precise coordinates.
- **Security**: API Keys are managed via `.env` file (not committed to git).

## 2. Setup & Installation

### A. Environment Variables
Create a `.env` file in the project root (if not exists) and add your keys:
```properties
VIETMAP_API_KEY_TILES=d55785f692bc3425968b647d18d7e7000132b36932bf7741
VIETMAP_API_KEY_SERVICES=7e8a843c9fae9a778a7f59ae4f5b8fa351a677301422804e
```
*Note: The application automatically loads this file at startup.*

### B. Database
The project uses PostgreSQL + PostGIS.
**Important**: Due to port conflicts on 5432, we configured the database to run on **port 5434**.
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/street_voice_db
```
Ensure your Docker container is running:
```sh
docker start street-voice-db-new
```

## 3. Running the Application

Recommended method (more stable than `mvn spring-boot:run`):
1.  **Build the JAR**:
    ```sh
    ./mvnw.cmd clean package -DskipTests
    ```
2.  **Run the JAR**:
    ```sh
    java -jar target/street-voice-backend-0.0.1-SNAPSHOT.jar
    ```

## 4. API Usage & Testing

### A. Admin Sync API
*   **Method**: `POST`
*   **URL**: `/api/v1/admin/sync-vietmap`
*   **Body** (JSON):
    ```json
    {
        "lat": 10.7607739,
        "lng": 106.7006542,
        "keyword": "ốc"
    }
    ```
*   **Response**: Returns the count of newly saved items.

### B. Postman Collection
A Postman collection is available for easy import:
*   File: `vietmap_sync_postman_collection.json`
*   **Usage**: Open Postman -> Import -> Select this file -> Run requests.

## 5. Troubleshooting Log (Recent Issues)

During development, we encountered and fixed the following critical issues. Reference this if you face similar problems.

### Issue 1: Database Port Conflict (Bind for 0.0.0.0:5432 failed)
**Symptom:** Docker container fails to start; Application fails to connect.
**Cause:** Port 5432 was occupied by another process/container.
**Fix:** Changed host port mapping to **5434** in Docker and updated `url` in `application.yaml`.

### Issue 2: `java-dotenv` Dependency Failure
**Symptom:** Maven build fails to resolve `io.github.cdimascio:java-dotenv`.
**Cause:** Network or artifact repository issues.
**Fix:** Removed the dependency. Implemented a **manual `.env` parser** in `StreetVoiceApplication.java` to load variables into System Properties at startup.

### Issue 3: Missing `RestClient` Bean (`NoSuchBeanDefinitionException`)
**Symptom:** Application fails to start, saying `RestClient.Builder` required a bean.
**Cause:** `pom.xml` had `spring-boot-starter-webmvc` (invalid) instead of `spring-boot-starter-web` and was using Spring Boot `4.x`.
**Fix:**
1.  Changed artifact to `spring-boot-starter-web`.
2.  Downgraded Spring Boot to **3.2.2** (stable) to ensure correct auto-configuration.

### Issue 4: Lombok Compilation Error (Version null)
**Symptom:** `Resolution of annotationProcessorPath dependencies failed: version can neither be null...`
**Cause:** `maven-compiler-plugin` configuration for Lombok was missing the `<version>` tag.
**Fix:** Added `<version>${lombok.version}</version>` to the plugin configuration in `pom.xml`.

### Issue 5: Unit Test `NoClassDefFoundError`
**Symptom:** Tests fail on `DefaultPrettyPrinter`.
**Fix:** Added `jackson-databind` dependency to `pom.xml`.
