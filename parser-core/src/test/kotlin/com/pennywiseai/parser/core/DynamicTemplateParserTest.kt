package com.pennywiseai.parser.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class DynamicTemplateParserTest {

    private val parser = DynamicTemplateParser()

    @Test
    fun `test simple amount extraction`() {
        val template = "Paid Rs {AMOUNT}"
        val sms = "Paid Rs 500.50"
        
        val result = parser.parse(sms, "TEST", 123L, template)
        
        assertNotNull(result)
        assertEquals(BigDecimal("500.50"), result?.amount)
        assertEquals(TransactionType.EXPENSE, result?.type)
    }

    @Test
    fun `test full template extraction`() {
        val template = "Acct {ACCOUNT} debited by Rs. {AMOUNT} towards {MERCHANT} ref {REFERENCE}. Bal: {BALANCE}"
        val sms = "Acct XX1234 debited by Rs. 1,050.00 towards Starbucks Coffee ref TXN998877. Bal: 10,000.50"
        
        val result = parser.parse(sms, "TEST", 123L, template, TransactionType.EXPENSE)
        
        assertNotNull(result)
        assertEquals(BigDecimal("1050.00"), result?.amount)
        assertEquals("Starbucks Coffee", result?.merchant)
        assertEquals("1234", result?.accountLast4)
        assertEquals("TXN998877", result?.reference)
        assertEquals(BigDecimal("10000.50"), result?.balance)
        assertEquals(TransactionType.EXPENSE, result?.type)
    }

    @Test
    fun `test regex escaping logic`() {
        // SMS contains literal brackets and dots which could break naive regex
        val template = "Your a/c [no: {ACCOUNT}] was charged ${'$'}{AMOUNT} at {MERCHANT}.?"
        val sms = "Your a/c [no: 5566] was charged $20.00 at Amazon.?"
        
        val result = parser.parse(sms, "TEST", 123L, template)
        
        assertNotNull(result)
        assertEquals(BigDecimal("20.00"), result?.amount)
        assertEquals("5566", result?.accountLast4)
        assertEquals("Amazon", result?.merchant)
    }
    
    @Test
    fun `test flexible whitespace`() {
        val template = "Paid {AMOUNT} to {MERCHANT}"
        // The real SMS has double spaces or tabs
        val sms = "Paid   50.00 \t to     Jio"
        
        val result = parser.parse(sms, "TEST", 123L, template)
        
        assertNotNull(result)
        assertEquals(BigDecimal("50.00"), result?.amount)
        assertEquals("Jio", result?.merchant)
    }

    @Test
    fun `test missing amount returns null`() {
        val template = "Paid {AMOUNT}"
        val sms = "Paid nothing" // Fails to match the numeric regex
        
        val result = parser.parse(sms, "TEST", 123L, template)
        
        assertNull(result)
    }
}