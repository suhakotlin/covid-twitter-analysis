README.md
# COVID-19 Twitter Data Analysis  
Assignment 2 – Kotlin Functional Programming Project  
Author: 김수하 (suhakotlin)

---

## 프로젝트 개요

이 프로젝트는 Kaggle에서 제공한 Global COVID-19 Twitter Dataset을 활용하여  
각 국가(Australia, Brazil, India, Indonesia, Japan)의 COVID-19 관련 트윗을 분석하는 프로그램입니다.

분석 내용은 다음과 같습니다:

- 국가별 전체 트윗 수
- 국가별 월별 트윗 수(연-월 기준)
- 각 국가에서 트윗이 가장 활발했던 ‘피크 월(peak month)’ 분석

---

## 프로젝트 실행 방법

### 1. Gradle 프로젝트 빌드
이 프로젝트는 Gradle 기반 Kotlin 프로젝트입니다.  
IntelliJ 또는 터미널에서 다음 명령으로 실행할 수 있습니다.

```bash
./gradlew build
2. 프로그램 실행
데이터 디렉토리를 전달하지 않으면
기본적으로 src/main/resources 폴더에서 CSV 파일을 자동으로 읽습니다.
./gradlew run
또는 직접 실행:
kotlin -classpath build/classes/kotlin/main org.example.MainKt
3. 외부 데이터 경로 전달(옵션)
아래처럼 CSV 파일이 저장된 디렉토리를 인자로 전달할 수 있습니다.
./gradlew run --args="/path/to/dataset"
📁 데이터 파일 위치 및 준비 방법
✔ 데이터 파일 구성 (Kaggle 제공 CSV)
프로젝트는 다음과 같은 CSV 파일 5개를 사용합니다:
Australia.csv
Brazil.csv
India.csv
Indonesia.csv
Japan.csv
✔ 데이터 파일 저장 위치
프로젝트 내부:
src/main/resources/
Kaggle에서 받은 zip 파일을 다운로드 → 압축 해제 → 위 폴더에 그대로 넣으면 됩니다.
※ 데이터 파일은 과제 제출 시 GitHub에 포함하지 않아도 됩니다.

주요 실행 결과 예시
프로그램 실행 시 다음과 같이 분석 결과가 출력됩니다:
=== 국가별 트윗 수 ===
Australia : 485168 tweets
Brazil    : 491565 tweets
India     : 213722 tweets
Indonesia : 275886 tweets
Japan     : 484413 tweets

=== 국가별 월별 트윗 수 ===
Australia 2021-12 : 305482 tweets
Brazil    2021-12 : 129831 tweets
India     2020-10 : 33175 tweets
Indonesia 2021-12 : 43034 tweets
Japan     2021-12 : 120350 tweets

=== 국가별 피크 월 ===
Australia : 2021-12
Brazil    : 2021-12
India     : 2020-10
Indonesia : 2021-12
Japan     : 2021-12

AI 도구 활용 방
본 프로젝트는 다음과 같은 방식으로 AI 도구(ChatGPT)를 활용했습니다:
Kotlin 코드 구조 설계 도움
날짜 파싱 및 CSV 처리 로직 개선
Gradle 및 GitHub 업로드 과정 문제 해결
README.md 및 analysis.md 문서 작성 보조

프로젝트 구조
covid-twitter-analysis/
├─ src/
│  └─ main/
│     ├─ kotlin/
│     │   ├─ Main.kt
│     │   └─ Tweet.kt
│     └─ resources/
│         ├─ Australia.csv
│         ├─ Brazil.csv
│         ├─ India.csv
│         ├─ Indonesia.csv
│         └─ Japan.csv
├─ build.gradle.kts
├─ settings.gradle.kts
└─ README.md
