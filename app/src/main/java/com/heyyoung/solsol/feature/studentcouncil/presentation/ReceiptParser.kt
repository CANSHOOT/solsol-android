package com.heyyoung.solsol.feature.studentcouncil.presentation

data class ReceiptFields(
    val merchant: String?, // 가맹점(추정)
    val date: String?,     // yyyy-MM-dd
    val total: String?     // "12,340원"
)

object ReceiptParser {

    // 금액 추출: ₩, \, 콤마 허용
    private val amountRegex = Regex("""(?<![A-Za-z0-9])(?:₩|\\)?\s*([0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)\s*(원|KRW)?(?![A-Za-z0-9])""")

    // 날짜 후보
    private val dateRegexes = listOf(
        Regex("""\b(20\d{2}|\d{2})[.\-\/]\s*\d{1,2}[.\-\/]\s*\d{1,2}(?:\s+\d{1,2}:\d{2}(?::\d{2})?)?\b"""),
        Regex("""\b(20\d{2}|\d{2})\s*년\s*\d{1,2}\s*월\s*\d{1,2}\s*일?(?:\s+\d{1,2}:\d{2}(?::\d{2})?)?\b""")
    )

    // 총액 키워드(1순위 / 2순위)
    private val topTotalKeywords    = listOf("총액", "총금액", "합계")
    private val otherTotalKeywords  = listOf("받을금액", "받은금액", "결제금액", "승인금액", "청구금액", "합계금액", "총결제금액", "총지불금액", "카드승인금액", "부가세포함", "VAT포함")

    // 날짜 힌트
    private val dateLineHints = listOf("결제", "승인", "거래", "매출", "이용", "일시", "영수일자", "판매일자")

    // 가맹점 키워드/필터
    private val merchantKeys   = listOf("상호", "가맹점명", "상호명", "업체명")
    private val merchantHints  = listOf("커피", "카페", "식당", "치킨", "피자", "베이커리", "마트", "메가", "MGC")
    private val headerNoise    = listOf("영수증","고객용","매출전표","신용카드","세금계산서","현금영수증","사업자","사업자등록","가맹점번호","대표자","전화","주소","TEL","Fax","FAX","문의","영업시간","영수증번호","승인번호","카드번호","POS","BILL")

    // 메타/전화/주문 라인 배제
    private val metaLineTokens = listOf("TEL","전화","POS","BILL","사업자","등록","주문","대기","번호","바코드","FSY","QR","코드")

    private val phoneRegex = Regex("""\d{2,4}-\d{3,4}-\d{4}""")

    fun parse(full: String): ReceiptFields {
        val rawLines = full.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val lines = rawLines.map(::normalizeLine)

        val total    = extractTotal(lines, full)
        val date     = extractDate(lines)
        val merchant = extractMerchant(lines)

        return ReceiptFields(merchant = merchant, date = date, total = total)
    }

    // ───────────────────────────── 총액 ─────────────────────────────
    private fun extractTotal(lines: List<String>, full: String): String? {
        data class Cand(val idx: Int, val amount: Int, val priority: Int)

        fun compact(s: String) = s.replace(Regex("""\s+|[：:]"""), "")
        fun hasKeywordAround(i: Int, keys: List<String>): Boolean {
            val here = compact(lines[i])
            val prev = if (i > 0) compact(lines[i - 1]) else ""
            val next = if (i + 1 < lines.size) compact(lines[i + 1]) else ""
            val combos = listOf(here, prev + here, here + next, prev + here + next)
            return combos.any { c -> keys.any { kw -> c.contains(kw) } }
        }

        fun isMetaLine(line: String): Boolean {
            val low = line.lowercase()
            if (metaLineTokens.any { low.contains(it.lowercase()) }) return true
            if (phoneRegex.containsMatchIn(line)) return true
            return false
        }

        fun collectWindow(i: Int, priority: Int): List<Cand> {
            val out = mutableListOf<Cand>()
            for (k in 0..2) { // 해당 줄 + 아래 2줄에서 금액 수집
                val idx = i + k
                if (idx >= lines.size) break
                val line = lines[idx]
                if (isMetaLine(line)) continue
                amountRegex.findAll(line).forEach { m ->
                    val raw = m.groupValues[1]
                    val amt = normalizeAmount(raw) ?: return@forEach
                    val prefer = ("," in line) || ("원" in line)
                    // 콤마/원 없는 큰 숫자(바코드/ID) 배제
                    if (!prefer && raw.length >= 6) return@forEach
                    out += Cand(idx, amt, priority)
                }
            }
            return out
        }

        val c1 = lines.indices
            .filter { hasKeywordAround(it, topTotalKeywords) }
            .flatMap { collectWindow(it, priority = 2) }

        val c2 = lines.indices
            .filter { hasKeywordAround(it, otherTotalKeywords) }
            .flatMap { collectWindow(it, priority = 1) }

        val picked = (c1 + c2).maxWithOrNull(compareBy<Cand>({ it.priority }).thenBy { it.amount }.thenBy { it.idx })
        if (picked != null) return formatAmount(picked.amount)

        // 최후 수단: 메타 라인 제외 + 콤마/원이 있는 금액 중 최댓값
        val fallback = lines
            .filterNot(::isMetaLine)
            .flatMap { line ->
                amountRegex.findAll(line).map { it.groupValues[1] to line }
            }
            .mapNotNull { (raw, line) ->
                val ok = ("," in line) || ("원" in line)
                if (!ok) null else normalizeAmount(raw)
            }
            .maxOrNull()

        return fallback?.let { formatAmount(it) }
    }

    // ───────────────────────────── 날짜 ─────────────────────────────
    private fun extractDate(lines: List<String>): String? {
        // 1) 힌트 라인 우선
        lines.forEach { line ->
            if (dateLineHints.any { line.contains(it) }) {
                dateRegexes.forEach { rx ->
                    rx.find(line)?.value?.let { raw -> normalizeDate(raw)?.let { return it } }
                }
            }
        }
        // 2) 전체 첫 매칭
        lines.forEach { line ->
            dateRegexes.forEach { rx ->
                rx.find(line)?.value?.let { raw -> normalizeDate(raw)?.let { return it } }
            }
        }
        return null
    }

    private fun normalizeDate(raw: String): String? {
        val s = raw.replace("년","-").replace("월","-").replace("일","")
            .replace(".","-").replace("/","-")
            .replace(Regex("""\s+"""), " ").trim()
        val m = Regex("""\b(\d{2,4})-(\d{1,2})-(\d{1,2})\b""").find(s) ?: return null
        var (y, mo, d) = m.destructured
        val year = when (y.length) { 2 -> "20%02d".format(y.toInt()); 3 -> "2$y"; else -> y }
        val mm = "%02d".format(mo.toInt().coerceIn(1, 12))
        val dd = "%02d".format(d.toInt().coerceIn(1, 31))
        return "$year-$mm-$dd"
    }

    // ───────────────────────────── 가맹점 ──────────────────────────
    private fun extractMerchant(lines: List<String>): String? {
        val top = lines.take(10)
        val ignoreForParen = listOf("주문","대기","번호")

        // (1) 괄호 안 브랜드: 주문(대기)번호 같은 건 제외
        val parenRegex = Regex("""\(([^)]+)\)""")
        top.forEach { line ->
            if (ignoreForParen.any { line.contains(it) }) return@forEach
            parenRegex.findAll(line).forEach { m ->
                val innerRaw = m.groupValues[1]
                val merged   = innerRaw.replace(" ", "")
                if (isBrandish(merged)) return canonicalizeBrand(merged)
            }
        }

        // (2) "상호/가맹점명:" 등
        top.forEach { line ->
            if (merchantKeys.any { line.contains(it) }) {
                Regex("""(상호|가맹점명|상호명|업체명)\s*[:：]\s*(.+)""")
                    .find(line)
                    ?.groupValues?.getOrNull(2)
                    ?.let { return canonicalizeBrand(cleanMerchant(it)) }

                val key = merchantKeys.firstOrNull { line.contains(it) }
                if (key != null) {
                    val after = line.substringAfter(key).trim()
                    if (after.isNotEmpty()) return canonicalizeBrand(cleanMerchant(after))
                }
            }
        }

        // (3) 상단부에서 금액/날짜/헤더성 제외한 짧은 한 줄
        val candidate = top.firstOrNull { l ->
            l.length in 2..30 &&
                    !amountRegex.containsMatchIn(l) &&
                    !dateRegexes.any { it.containsMatchIn(l) } &&
                    headerNoise.none { n -> l.contains(n, ignoreCase = true) }
        }
        return candidate?.let { canonicalizeBrand(cleanMerchant(it)) }
    }

    // 괄호 안 텍스트가 브랜드/상호로 보이는지
    private fun isBrandish(s: String): Boolean {
        if (s.length !in 2..30) return false
        val hasHint = merchantHints.any { s.contains(it, ignoreCase = true) }
        val hasHangul = Regex("""[가-힣]""").containsMatchIn(s)
        return hasHint || hasHangul
    }

    private fun canonicalizeBrand(s: String): String {
        val merged = s.replace(" ", "")
        // 흔한 OCR 교정: 메가MGC커피
        val mgc = Regex("""메가\s*M?\s*GC\s*커피""", RegexOption.IGNORE_CASE)
        if (mgc.containsMatchIn(merged)) return "메가MGC커피"
        return merged
    }

    private fun cleanMerchant(v: String): String =
        v.replace(Regex("""[(){}\[\]<>]"""), "")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()

    // ───────────────────────────── 유틸 ────────────────────────────
    private fun normalizeAmount(raw: String): Int? =
        raw.replace(",", "").toIntOrNull()

    private fun formatAmount(v: Int): String =
        "%,d원".format(v)

    private fun normalizeLine(line: String): String {
        var s = line
        // 키워드가 줄바꿈으로 쪼개져도 인식되도록 공백/콜론 보정
        s = s.replace(Regex("""총\s*액"""), "총액")
        s = s.replace(Regex("""총\s*금\s*액"""), "총금액")
        s = s.replace(Regex("""합\s*계"""), "합계")
        s = s.replace(Regex("""받\s*을\s*금?\s*액"""), "받을금액")
        s = s.replace(Regex("""받\s*은\s*금?\s*액"""), "받은금액")
        s = s.replace(Regex("""결\s*제\s*금\s*액"""), "결제금액")
        s = s.replace(Regex("""승\s*인\s*금\s*액"""), "승인금액")
        s = s.replace(Regex("""청\s*구\s*금\s*액"""), "청구금액")
        s = s.replace(Regex("""합\s*계\s*금\s*액"""), "합계금액")
        s = s.replace(Regex("""총\s*결\s*제\s*금\s*액"""), "총결제금액")
        s = s.replace(Regex("""총\s*지\s*불\s*금\s*액"""), "총지불금액")
        s = s.replace('：', ':')
        s = s.replace(Regex("""\s{2,}"""), " ").trim()
        return s
    }
}
