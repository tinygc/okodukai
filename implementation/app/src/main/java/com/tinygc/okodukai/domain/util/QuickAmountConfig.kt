package com.tinygc.okodukai.domain.util

object QuickAmountConfig {
    const val SLOT_COUNT = 8
    const val MIN_AMOUNT = 1
    const val MAX_AMOUNT = 99999

    enum class ValidationError {
        EMPTY,
        INVALID
    }

    val defaults: List<Int> = listOf(1, 5, 10, 50, 100, 500, 1000, 5000)

    fun isValid(values: List<Int>): Boolean {
        if (values.size != SLOT_COUNT) {
            return false
        }
        return values.all { it in MIN_AMOUNT..MAX_AMOUNT }
    }

    fun serialize(values: List<Int>): String {
        if (!isValid(values)) {
            return serialize(defaults)
        }
        return values.joinToString(",")
    }

    fun deserialize(raw: String?): List<Int> {
        if (raw.isNullOrBlank()) {
            return defaults
        }

        val parsed = raw.split(',').mapNotNull { token ->
            token.trim().toIntOrNull()
        }

        return if (isValid(parsed)) parsed else defaults
    }

    fun validateInputStrings(inputs: List<String>): ValidationError? {
        if (inputs.size != SLOT_COUNT || inputs.any { it.isBlank() }) {
            return ValidationError.EMPTY
        }

        val parsed = inputs.map { token -> token.trim().toIntOrNull() ?: return ValidationError.INVALID }
        return if (isValid(parsed)) null else ValidationError.INVALID
    }

    fun parseInputStrings(inputs: List<String>): List<Int>? {
        return if (validateInputStrings(inputs) == null) {
            inputs.map { it.trim().toInt() }
        } else {
            null
        }
    }
}
