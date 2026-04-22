# 1. 자바 실행 환경(JRE) 17 또는 21 버전을 가져옵니다. (사용 중인 JDK 버전에 맞추세요)
FROM eclipse-temurin:21-jre-alpine

# 2. 작업 디렉토리를 설정합니다.
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너 내부로 복사합니다.
# (./build/libs/*.jar는 Gradle 기준입니다. Maven은 ./target/*.jar로 수정하세요)
COPY build/libs/*.jar app.jar

# 4. 앱을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]