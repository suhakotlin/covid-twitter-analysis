package org.example

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.util.Locale

fun main(args: Array<String>) {

    // 1) 사용할 국가 목록
    val countries = listOf("Australia", "Brazil", "India", "Indonesia", "Japan")

    // 2) 실행 인자로 넘어온 데이터 디렉토리 (없으면 resources 사용)
    val baseDir: Path? = if (args.isNotEmpty()) Paths.get(args[0]) else null

    val allTweets = mutableListOf<Tweet>()

    println("=== COVID-19 Twitter 데이터 로딩 시작 ===")
    println("데이터 디렉토리 인자: ${baseDir ?: "src/main/resources (classpath)"}")
    println()

    for (country in countries) {
        val fileName = "$country.csv"
        val reader = openReader(baseDir, fileName)

        if (reader == null) {
            println("⚠ 파일을 열 수 없습니다: $fileName")
            continue
        }

        var added = 0

        reader.useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (line.isBlank()) return@forEachIndexed

                // 첫 줄이 헤더일 수 있으니, created_at 헤더면 스킵
                if (index == 0 && line.contains("created_at", ignoreCase = true)) {
                    return@forEachIndexed
                }

                val tweet = parseTweet(line, country)
                if (tweet != null) {
                    allTweets.add(tweet)
                    added++
                }
            }
        }

        println("✔ $country: ${added}개 로드")
    }

    println()
    println("=== 전체 유효 트윗 수: ${allTweets.size} ===")

    // 3) 국가별 트윗 수 집계
    printCountryCounts(allTweets)

    // 4) 국가별 월별 트윗 수 추이
    printMonthlyTrends(allTweets)

    // 5) 국가별 피크(트윗이 가장 많았던 달)
    printPeakMonths(allTweets)
}

/**
 * baseDir 인자가 있으면 그 디렉토리에서 파일을 열고,
 * 없으면 classpath(resources)에서 파일을 연다.
 */
fun openReader(baseDir: Path?, fileName: String): BufferedReader? {
    return if (baseDir != null) {
        val path = baseDir.resolve(fileName)
        if (!Files.exists(path)) return null
        Files.newBufferedReader(path)
    } else {
        val stream = {}::class.java.classLoader.getResourceAsStream(fileName) ?: return null
        BufferedReader(InputStreamReader(stream))
    }
}

/**
 * Kaggle CSV 한 줄 → Tweet 객체로 변환.
 *
 * CSV 구조(대략):
 *   0: index
 *   1: created_at (예: "Wed Dec 08 04:25:46 +0000 2021")
 *   2: text
 *   3: user_location ...
 *
 * → 우리는 "두 번째 컬럼(created_at)"을 날짜로 사용.
 *   콤마 위치 두 개를 찾아서 그 사이를 날짜로 본다.
 */
fun parseTweet(line: String, country: String): Tweet? {
    val firstComma = line.indexOf(',')
    if (firstComma == -1) return null

    val secondComma = line.indexOf(',', firstComma + 1)
    if (secondComma == -1) return null

    // 0번 컬럼: 인덱스 → 버림
    val datePart = line.substring(firstComma + 1, secondComma).trim()
    // 나머지 전체를 text로(정확한 텍스트 컬럼까지 안 맞아도 상관 없음)
    val textPart = line.substring(secondComma + 1).trim()

    // 혹시 헤더 줄이 여기 들어왔으면 스킵
    if (datePart.equals("created_at", ignoreCase = true)) {
        return null
    }

    val cleanedText = preprocessText(textPart)
    val parsedDate = parseDateSafely(datePart)

    return Tweet(
        rawDate = datePart,
        date = parsedDate,
        text = cleanedText,
        country = country
    )
}

/**
 * 트윗 텍스트 전처리:
 * - URL 제거
 * - # 기호 제거 (단어는 남김)
 * - 연속 공백 정리
 * - 소문자 통일
 */
fun preprocessText(text: String): String {
    var t = text

    // URL 제거
    t = t.replace(Regex("https?://\\S+"), " ")

    // 해시태그 기호만 제거 (#covid19 -> " covid19")
    t = t.replace("#", " ")

    // 연속 공백 하나로
    t = t.replace(Regex("\\s+"), " ")

    return t.trim().lowercase()
}

/**
 * 날짜 문자열을 여러 패턴으로 시도해서 LocalDate로 변환.
 * Kaggle/Twitter created_at 형식:
 *   예) "Wed Dec 08 04:25:46 +0000 2021"
 */
fun parseDateSafely(raw: String): LocalDate? {

    // 1) Twitter created_at 전용 포맷
    val twitterFormatter =
        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)

    try {
        val zdt = ZonedDateTime.parse(raw, twitterFormatter)
        return zdt.toLocalDate()
    } catch (_: Exception) {
        // 무시하고 아래 다른 포맷 시도
    }

    // 2) ISO 8601 (혹시 섞여 있을 경우 대비)
    try {
        return Instant.parse(raw)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } catch (_: Exception) {}

    try {
        return LocalDateTime.parse(raw).toLocalDate()
    } catch (_: Exception) {}

    // 3) 기타 예비 패턴들
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM/dd",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy"
    )

    for (p in patterns) {
        val f = DateTimeFormatter.ofPattern(p)
        try {
            return LocalDateTime.parse(raw, f).toLocalDate()
        } catch (_: Exception) {}

        try {
            return LocalDate.parse(raw, f)
        } catch (_: Exception) {}
    }

    // 어떤 패턴에도 안 맞으면 null
    return null
}

/**
 * 국가별 트윗 수 출력
 */
fun printCountryCounts(tweets: List<Tweet>) {
    println()
    println("=== 국가별 트윗 수 ===")

    val grouped = tweets.groupBy { it.country }

    for ((country, list) in grouped) {
        println("$country : ${list.size} tweets")
    }
}

/**
 * 국가별 월별 트윗 수 출력
 */
fun printMonthlyTrends(tweets: List<Tweet>) {
    println()
    println("=== 국가별 월별 트윗 수 (연-월 기준) ===")

    val valid = tweets.filter { it.date != null }

    // (country, YearMonth) 로 그룹화
    val grouped = valid.groupBy { tweet ->
        val ym = YearMonth.from(tweet.date!!)
        tweet.country to ym
    }

    // 정렬해서 보기 좋게 출력
    val sortedKeys = grouped.keys.sortedWith(
        compareBy<Pair<String, YearMonth>> { it.first }.thenBy { it.second }
    )

    for (key in sortedKeys) {
        val (country, ym) = key
        val count = grouped[key]?.size ?: 0
        println("$country $ym : $count tweets")
    }
}

/**
 * 각 국가별로 트윗이 가장 많았던 달(피크) 출력
 */
fun printPeakMonths(tweets: List<Tweet>) {
    println()
    println("=== 국가별 피크(트윗이 가장 많았던 달) ===")

    val valid = tweets.filter { it.date != null }

    val grouped = valid.groupBy { tweet ->
        val ym = YearMonth.from(tweet.date!!)
        tweet.country to ym
    }

    // country별로 다시 묶기
    val byCountry = grouped.entries.groupBy { it.key.first }

    for ((country, entries) in byCountry) {
        val peak = entries.maxByOrNull { it.value.size }
        if (peak != null) {
            val ym = peak.key.second
            val count = peak.value.size
            println("$country : $ym (${count} tweets)")
        }
    }
}
