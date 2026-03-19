package com.tinygc.okodukai.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickAmountConfigTest {

    @Test
    fun `default values are 8 amounts and in ascending order`() {
        val defaults = QuickAmountConfig.defaults

        assertEquals(8, defaults.size)
        assertEquals(listOf(1, 5, 10, 50, 100, 500, 1000, 5000), defaults)
    }

    @Test
    fun `serialize and deserialize round-trip`() {
        val values = listOf(2, 20, 200, 500, 1000, 3000, 5000, 9000)

        val encoded = QuickAmountConfig.serialize(values)
        val decoded = QuickAmountConfig.deserialize(encoded)

        assertEquals(values, decoded)
    }

    @Test
    fun `deserialize returns defaults when token count is not 8`() {
        val decoded = QuickAmountConfig.deserialize("1,5,10")

        assertEquals(QuickAmountConfig.defaults, decoded)
    }

    @Test
    fun `validate returns false when any value is out of range`() {
        assertFalse(QuickAmountConfig.isValid(listOf(0, 5, 10, 50, 100, 500, 1000, 5000)))
        assertFalse(QuickAmountConfig.isValid(listOf(1, 5, 10, 50, 100, 500, 1000, 100000)))
    }

    @Test
    fun `validate returns true for 8 values in range`() {
        assertTrue(QuickAmountConfig.isValid(listOf(1, 5, 10, 50, 100, 500, 1000, 5000)))
    }

    @Test
    fun `validate input strings returns empty when blank exists`() {
        val result = QuickAmountConfig.validateInputStrings(
            listOf("1", "5", "10", "50", "100", "500", "", "5000")
        )

        assertEquals(QuickAmountConfig.ValidationError.EMPTY, result)
    }

    @Test
    fun `validate input strings returns invalid when non numeric exists`() {
        val result = QuickAmountConfig.validateInputStrings(
            listOf("1", "5", "10", "50", "100", "500", "abc", "5000")
        )

        assertEquals(QuickAmountConfig.ValidationError.INVALID, result)
    }

    @Test
    fun `parse input strings returns parsed values when valid`() {
        val result = QuickAmountConfig.parseInputStrings(
            listOf("1", "5", "10", "50", "100", "500", "1000", "5000")
        )

        assertEquals(listOf(1, 5, 10, 50, 100, 500, 1000, 5000), result)
    }
}
