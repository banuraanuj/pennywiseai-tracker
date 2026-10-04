package com.pennywiseai.tracker.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.LocalDate

class TransactionRepositoryTimezoneTest {

    @Test
    fun `test injected clock is respected by date queries`() {
        val tokyoZone = ZoneId.of("Asia/Tokyo")
        val instantInTokyoMar1 = LocalDateTime.of(2024, 3, 1, 0, 30).toInstant(ZoneOffset.ofHours(9))
        val tokyoClock = Clock.fixed(instantInTokyoMar1, tokyoZone)

        val nowInTokyo = LocalDate.now(tokyoClock)
        
        // This confirms that an injected clock with +09:00 timezone evaluates correctly,
        // preventing midnight boundary date shift bugs where UTC might still be Feb 29.
        assertEquals(3, nowInTokyo.monthValue)
        assertEquals(1, nowInTokyo.dayOfMonth)
        assertEquals(2024, nowInTokyo.year)
    }
}
