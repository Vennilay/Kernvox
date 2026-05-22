package com.vennilay.kernvox.ui.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class TimeFormatterTest {

    private lateinit var originalTimeZone: TimeZone
    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        originalLocale = Locale.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
        Locale.setDefault(originalLocale)
    }

    // region formatUptime

    @Test
    fun formatUptimeReturnsSecondsForLessThanOneMinute() {
        assertEquals("0s", formatUptime(0, "d", "h", "m", "s"))
        assertEquals("1s", formatUptime(1, "d", "h", "m", "s"))
        assertEquals("59s", formatUptime(59, "d", "h", "m", "s"))
    }

    @Test
    fun formatUptimeReturnsMinutesAtExactlyOneMinute() {
        assertEquals("1m", formatUptime(60, "d", "h", "m", "s"))
    }

    @Test
    fun formatUptimeShowsMinutesWithoutSecondsAbove60() {
        assertEquals("1m", formatUptime(90, "d", "h", "m", "s"))
        assertEquals("59m", formatUptime(3599, "d", "h", "m", "s"))
    }

    @Test
    fun formatUptimeShowsHoursAndMinutes() {
        assertEquals("1h 0m", formatUptime(3600, "d", "h", "m", "s"))
        assertEquals("1h 1m", formatUptime(3660, "d", "h", "m", "s"))
        assertEquals("2h 30m", formatUptime(9000, "d", "h", "m", "s"))
    }

    @Test
    fun formatUptimeShowsDaysHoursAndMinutes() {
        assertEquals("1d 0h 0m", formatUptime(86400, "d", "h", "m", "s"))
        assertEquals("1d 1h 1m", formatUptime(90061, "d", "h", "m", "s"))
        assertEquals("2d 12h 30m", formatUptime(2 * 86400 + 12 * 3600 + 30 * 60, "d", "h", "m", "s"))
    }

    @Test
    fun formatUptimeUsesProvidedUnitStrings() {
        val result = formatUptime(90061, " days", " hrs", " min", " sec")
        assertEquals("1 days 1 hrs 1 min", result)
    }

    // endregion

    // region formatBytes

    @Test
    fun formatBytesReturnsNoDataForNull() {
        assertEquals("N/A", formatBytes(null, "KB", "MB", "GB", "B", "N/A"))
    }

    @Test
    fun formatBytesFormatsZeroAsBytes() {
        assertEquals("0 B", formatBytes(0f, "KB", "MB", "GB", "B", "N/A"))
    }

    @Test
    fun formatBytesFormatsSmallValuesAsBytes() {
        assertEquals("1023 B", formatBytes(1023f, "KB", "MB", "GB", "B", "N/A"))
    }

    @Test
    fun formatBytesFormatsKilobytes() {
        assertEquals("1 KB", formatBytes(1024f, "KB", "MB", "GB", "B", "N/A"))
        assertEquals("1024 KB", formatBytes(1_048_575f, "KB", "MB", "GB", "B", "N/A"))
    }

    @Test
    fun formatBytesFormatsMegabytes() {
        assertEquals("1.0 MB", formatBytes(1_048_576f, "KB", "MB", "GB", "B", "N/A"))
        assertEquals("1.5 MB", formatBytes(1_572_864f, "KB", "MB", "GB", "B", "N/A"))
    }

    @Test
    fun formatBytesFormatsGigabytes() {
        assertEquals("1.0 GB", formatBytes(1_073_741_824f, "KB", "MB", "GB", "B", "N/A"))
        assertEquals("2.0 GB", formatBytes(2_147_483_648f, "KB", "MB", "GB", "B", "N/A"))
    }

    // endregion

    // region formatTimestamp

    @Test
    fun formatTimestampParsesIsoWithMicroseconds() {
        // UTC timezone set in @Before → output is also UTC
        assertEquals("07.04.2026 18:47", formatTimestamp("2026-04-07T18:47:11.844222Z"))
    }

    @Test
    fun formatTimestampParsesIsoWithMilliseconds() {
        assertEquals("01.01.2026 00:00", formatTimestamp("2026-01-01T00:00:00.000Z"))
    }

    @Test
    fun formatTimestampReturnsMalformedInputAsIs() {
        assertEquals("not-a-date", formatTimestamp("not-a-date"))
        assertEquals("", formatTimestamp(""))
    }

    @Test
    fun formatTimestampOutputMatchesExpectedPattern() {
        val result = formatTimestamp("2026-06-15T09:30:45.123456Z")
        // Pattern dd.MM.yyyy HH:mm — should be 16 chars like "15.06.2026 09:30"
        assertTrue("Expected 'dd.MM.yyyy HH:mm' format but got: $result", result.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}")))
    }

    // endregion
}
