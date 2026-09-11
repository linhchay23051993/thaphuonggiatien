package com.linhchay.thaphuonggiatien.utils

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/**
 * Thuật toán chuyển đổi Âm lịch ↔ Dương lịch của Hồ Ngọc Đức.
 *
 * Nguồn gốc: http://www.informatik.uni-leipzig.de/~duc/amlich/
 * Tác giả: Hồ Ngọc Đức
 * Port sang Kotlin bởi ThapHuongGiaTien project.
 *
 * Phạm vi chính xác: 1200 – 2199 CE.
 * Múi giờ mặc định: +7 (Việt Nam).
 */
object LunarSolarConverter {

    private const val PI_VALUE = PI

    /**
     * Kết quả chuyển đổi âm lịch → dương lịch.
     */
    data class SolarDate(
        val day: Int,
        val month: Int,
        val year: Int
    )

    /**
     * Kết quả chuyển đổi dương lịch → âm lịch.
     */
    data class LunarDate(
        val day: Int,
        val month: Int,
        val year: Int,
        val isLeapMonth: Boolean
    )

    // ========================================================================
    // Hàm hỗ trợ toán học
    // ========================================================================

    /**
     * Chuyển ngày dương lịch (dd/mm/yyyy) thành Julian Day Number.
     */
    fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    /**
     * Chuyển Julian Day Number thành ngày dương lịch [dd, mm, yyyy].
     */
    fun jdToDate(jd: Int): IntArray {
        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) { // After 5/10/1582, Gregorian calendar
            a = jd + 32044
            b = (4 * a + 3) / 146097
            c = a - (b * 146097) / 4
        } else {
            b = 0
            c = jd + 32082
        }
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + m / 10
        return intArrayOf(day, month, year)
    }

    /**
     * Tính ngày Sóc (New Moon) thứ k kể từ ngày 01/01/1900.
     * Trả về Julian Day Number của ngày Sóc tại múi giờ timeZone.
     */
    private fun getNewMoonDay(k: Int, timeZone: Double): Int {
        val t = k / 1236.85 // Time in Julian centuries from 1900 January 0.5
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI_VALUE / 180.0

        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr) // Mean new moon

        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3 // Sun's mean anomaly
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3 // Moon's mean anomaly
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3 // Moon's argument of latitude

        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2.0 * dr * m)
        c1 = c1 - 0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2.0 * mpr)
        c1 -= 0.0004 * sin(dr * 3.0 * mpr)
        c1 = c1 + 0.0104 * sin(dr * 2.0 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 = c1 - 0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2.0 * f + m))
        c1 = c1 - 0.0004 * sin(dr * (2.0 * f - m)) - 0.0006 * sin(dr * (2.0 * f + mpr))
        c1 = c1 + 0.0010 * sin(dr * (2.0 * f - mpr)) + 0.0005 * sin(dr * (2.0 * mpr + m))

        val deltaT: Double = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }

        val jdNew = jd1 + c1 - deltaT
        return floor(jdNew + 0.5 + timeZone / 24.0).toInt()
    }

    /**
     * Tính kinh độ Mặt Trời (Sun Longitude) tại Julian Day jdn.
     * Trả về cung hoàng đạo (0-11).
     */
    private fun getSunLongitude(jdn: Int, timeZone: Double): Double {
        val t = (jdn - 0.5 - timeZone / 24.0 - 2451545.0) / 36525.0 // Time in Julian centuries from 2000-01-01 12:00:00 GMT
        val t2 = t * t
        val dr = PI_VALUE / 180.0 // degree to radian

        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2 // mean anomaly, degree
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2 // mean longitude, degree

        var dl = (1.9146 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2.0 * m) + 0.00029 * sin(dr * 3.0 * m)

        var l = l0 + dl // true longitude, degree
        l *= dr
        l -= PI_VALUE * 2.0 * floor(l / (PI_VALUE * 2.0)) // Normalize to (0, 2*PI)

        return floor(l / PI_VALUE * 6.0)
    }

    /**
     * Tìm ngày Sóc bắt đầu tháng 11 âm lịch của năm yy (dương lịch).
     */
    private fun getLunarMonth11(yy: Int, timeZone: Double): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = (off / 29.530588853).toInt()
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm, timeZone).toInt() // sun longitude at local midnight
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    /**
     * Xác định tháng nhuận trong khoảng từ tháng 11 năm trước đến tháng 11 năm sau.
     * Trả về offset (0 = không nhuận, 1-12 = vị trí tháng nhuận).
     */
    private fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        val k = ((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var last: Int
        var i = 1 // We start with the month following lunar month 11
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone).toInt()
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone).toInt()
        } while (arc != last && i < 14)
        return i - 1
    }

    // ========================================================================
    // Hàm chuyển đổi chính
    // ========================================================================

    /**
     * Chuyển đổi ngày dương lịch → âm lịch.
     *
     * @param dd Ngày dương lịch
     * @param mm Tháng dương lịch
     * @param yy Năm dương lịch
     * @param timeZone Múi giờ (mặc định 7.0 cho Việt Nam)
     * @return [LunarDate] chứa ngày, tháng, năm âm lịch và cờ tháng nhuận
     */
    fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Double = 7.0): LunarDate {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = ((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        val lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = ((monthStart - a11) / 29)
        var lunarLeap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = true
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            return LunarDate(
                day = lunarDay,
                month = lunarMonth,
                year = lunarYear - 1,
                isLeapMonth = lunarLeap
            )
        }
        return LunarDate(
            day = lunarDay,
            month = lunarMonth,
            year = lunarYear,
            isLeapMonth = lunarLeap
        )
    }

    /**
     * Chuyển đổi ngày âm lịch → dương lịch.
     *
     * @param lunarDay Ngày âm lịch
     * @param lunarMonth Tháng âm lịch
     * @param lunarYear Năm âm lịch
     * @param lunarLeap Có phải tháng nhuận không (mặc định false)
     * @param timeZone Múi giờ (mặc định 7.0 cho Việt Nam)
     * @return [SolarDate] chứa ngày, tháng, năm dương lịch
     */
    fun convertLunar2Solar(
        lunarDay: Int,
        lunarMonth: Int,
        lunarYear: Int,
        lunarLeap: Boolean = false,
        timeZone: Double = 7.0
    ): SolarDate {
        val a11: Int
        val b11: Int
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1, timeZone)
            b11 = getLunarMonth11(lunarYear, timeZone)
        } else {
            a11 = getLunarMonth11(lunarYear, timeZone)
            b11 = getLunarMonth11(lunarYear + 1, timeZone)
        }
        val k = (0.5 + (a11 - 2415021.076998695) / 29.530588853).toInt()
        var off = lunarMonth - 11
        if (off < 0) {
            off += 12
        }
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11, timeZone)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) {
                leapMonth += 12
            }
            if (lunarLeap && lunarMonth != leapMonth) {
                // Invalid leap month request - return best guess
                val monthStart = getNewMoonDay(k + off, timeZone)
                val result = jdToDate(monthStart + lunarDay - 1)
                return SolarDate(result[0], result[1], result[2])
            } else if (lunarLeap || off >= leapOff) {
                off += 1
            }
        }
        val monthStart = getNewMoonDay(k + off, timeZone)
        val result = jdToDate(monthStart + lunarDay - 1)
        return SolarDate(result[0], result[1], result[2])
    }

    // ========================================================================
    // Tiện ích bổ sung: Can Chi
    // ========================================================================

    private val CAN = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val CHI = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    /**
     * Lấy tên Can Chi của năm âm lịch.
     */
    fun getCanChiYear(lunarYear: Int): String {
        val canIndex = (lunarYear + 6) % 10
        val chiIndex = (lunarYear + 8) % 12
        return "${CAN[canIndex]} ${CHI[chiIndex]}"
    }

    /**
     * Lấy tên Can Chi của ngày (dựa trên Julian Day Number).
     */
    fun getCanChiDay(jd: Int): String {
        val canIndex = (jd + 9) % 10
        val chiIndex = (jd + 1) % 12
        return "${CAN[canIndex]} ${CHI[chiIndex]}"
    }

    /**
     * Format ngày dương lịch thành chuỗi "dd/MM/yyyy".
     */
    fun formatSolarDate(solarDate: SolarDate): String {
        return "%02d/%02d/%04d".format(solarDate.day, solarDate.month, solarDate.year)
    }

    /**
     * Format ngày âm lịch thành chuỗi mô tả.
     */
    fun formatLunarDate(lunarDate: LunarDate): String {
        val leapStr = if (lunarDate.isLeapMonth) " (Nhuận)" else ""
        return "%02d/%02d/%04d%s".format(lunarDate.day, lunarDate.month, lunarDate.year, leapStr)
    }
}
