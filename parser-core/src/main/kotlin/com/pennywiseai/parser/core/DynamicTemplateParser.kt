package com.pennywiseai.parser.core

import java.math.BigDecimal

/**
 * Evaluates custom, user-defined SMS templates.
 * 
 * Supports the following tags:
 * {AMOUNT} - Extracts decimal numbers (e.g. 1,000.50)
 * {MERCHANT} - Extracts a string up to the next literal character in the template
 * {ACCOUNT} - Extracts digits and masking characters (e.g. XX1234)
 * {REFERENCE} - Extracts alphanumeric reference numbers
 * {BALANCE} - Extracts the remaining balance amount
 */
class DynamicTemplateParser {

    companion object {
        private const val AMOUNT_TAG = "{AMOUNT}"
        private const val MERCHANT_TAG = "{MERCHANT}"
        private const val ACCOUNT_TAG = "{ACCOUNT}"
        private const val REFERENCE_TAG = "{REFERENCE}"
        private const val BALANCE_TAG = "{BALANCE}"

        // Safe regex parts for each tag type
        private const val REGEX_AMOUNT = """([0-9,]+(?:\.\d{1,2})?)"""
        // In unanchored templates, (.+?) can match just 1 character if it's at the end of the template.
        // E.g. `to {MERCHANT}` against `to Jio` will match `J`.
        // To fix this, if {MERCHANT} is at the end of the template, we want `(.*)` or `(.+)`.
        // Since we don't know its position at declaration time, we'll replace the generic tag
        // intelligently in `compileTemplate`.
        private const val REGEX_MERCHANT = """(.+?)"""
        private const val REGEX_MERCHANT_END = """(.+)"""
        private const val REGEX_ACCOUNT = """([X*\d]+)"""
        private const val REGEX_REFERENCE = """([A-Za-z0-9]+)"""
        private const val REGEX_BALANCE = """([0-9,]+(?:\.\d{1,2})?)"""

        /**
         * Compiles a user-friendly template string with tags into a Regex.
         * Automatically escapes literal regex characters (e.g. $ . ? () []).
         * Replaces contiguous whitespace with \s+ for robust matching.
         */
        fun compileTemplate(template: String): Regex {
            // First, protect our valid tags so they don't get regex-escaped
            val tagMap = mapOf(
                AMOUNT_TAG to "___TAG_AMOUNT___",
                MERCHANT_TAG to "___TAG_MERCHANT___",
                ACCOUNT_TAG to "___TAG_ACCOUNT___",
                REFERENCE_TAG to "___TAG_REFERENCE___",
                BALANCE_TAG to "___TAG_BALANCE___"
            )

            var protectedTemplate = template
            tagMap.forEach { (tag, replacement) ->
                protectedTemplate = protectedTemplate.replace(tag, replacement)
            }

            // Escape standard regex characters in the user's literal text
            // e.g. "Rs." becomes "Rs\."
            var escapedTemplate = ""
            for (char in protectedTemplate) {
                if (char.isWhitespace()) {
                    escapedTemplate += "\\s+"
                } else if (Regex("""[\\.^$*+?()\[\]{}|]""").matches(char.toString())) {
                    escapedTemplate += "\\$char"
                } else {
                    escapedTemplate += char
                }
            }

            // Clean up contiguous \\s+ combinations into a single \\s+
            escapedTemplate = escapedTemplate.replace(Regex("""(?:\\s\+)+"""), """\\s+""")

            // Restore the tags as actual capture groups
            val finalPattern = escapedTemplate
                .replace("___TAG_AMOUNT___", REGEX_AMOUNT)
                .replace("___TAG_MERCHANT___", REGEX_MERCHANT)
                .replace("___TAG_ACCOUNT___", REGEX_ACCOUNT)
                .replace("___TAG_REFERENCE___", REGEX_REFERENCE)
                .replace("___TAG_BALANCE___", REGEX_BALANCE)

            // Ensure the regex handles the end-of-string properly for trailing MERCHANT tags
            // without matching the entire string if it doesn't fit the rest.
            var regexString = finalPattern
            if (regexString.endsWith(REGEX_MERCHANT)) {
                regexString = regexString.removeSuffix(REGEX_MERCHANT) + REGEX_MERCHANT_END
            } else if (regexString.endsWith(REGEX_MERCHANT + """\s+""")) {
                regexString = regexString.removeSuffix(REGEX_MERCHANT + """\s+""") + REGEX_MERCHANT_END + """\s*"""
            }

            // Make the regex unanchored so it can match substrings if the user template isn't exactly the full SMS
            return Regex(regexString, RegexOption.IGNORE_CASE)
        }
    }

    fun parse(
        smsBody: String,
        sender: String,
        timestamp: Long,
        template: String,
        forcedType: TransactionType? = null
    ): ParsedTransaction? {
        if (template.isBlank()) return null

        val compiledRegex = try {
            compileTemplate(template.trim())
        } catch (e: Exception) {
            return null // Invalid template syntax
        }

        val matchResult = compiledRegex.find(smsBody.trim()) ?: return null

        var amount: BigDecimal? = null
        var merchant: String? = null
        var account: String? = null
        var reference: String? = null
        var balance: BigDecimal? = null

        // We need to map the matched groups back to their specific tags.
        // We do this by scanning the original template to see which order the tags appeared in.
        val tagsInOrder = Regex("""\{(AMOUNT|MERCHANT|ACCOUNT|REFERENCE|BALANCE)\}""")
            .findAll(template)
            .map { it.value }
            .toList()

        // The first capture group is index 1
        tagsInOrder.forEachIndexed { index, tag ->
            val capturedString = matchResult.groups[index + 1]?.value ?: return@forEachIndexed
            when (tag) {
                AMOUNT_TAG -> amount = parseAmount(capturedString)
                MERCHANT_TAG -> merchant = capturedString.trim()
                ACCOUNT_TAG -> account = extractLast4Digits(capturedString)
                REFERENCE_TAG -> reference = capturedString.trim()
                BALANCE_TAG -> balance = parseAmount(capturedString)
            }
        }

        if (amount == null) return null

        return ParsedTransaction(
            amount = amount,
            type = forcedType ?: TransactionType.EXPENSE, // Default to expense if not provided
            merchant = merchant,
            reference = reference,
            accountLast4 = account,
            balance = balance,
            smsBody = smsBody,
            sender = sender,
            timestamp = timestamp,
            bankName = "Custom Template",
            currency = "INR", // Assuming base currency for custom templates unless we add a tag later
            isFromCard = false
        )
    }

    private fun parseAmount(amountStr: String): BigDecimal? {
        return try {
            BigDecimal(amountStr.replace(",", ""))
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun extractLast4Digits(accountStr: String): String? {
        val digitsOnly = accountStr.replace(Regex("""\D"""), "")
        return if (digitsOnly.length >= 4) {
            digitsOnly.takeLast(4)
        } else if (digitsOnly.isNotEmpty()) {
            digitsOnly
        } else {
            null
        }
    }
}