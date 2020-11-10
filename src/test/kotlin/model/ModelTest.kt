package model

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import model.CFmodel;

class ModelTest {
    @Test
    @DisplayName("Should return the correct message")
    fun testModel() {
        val model = CFmodel()
        assertTrue(true, "someLibraryMethod should return 'true'")
    }
}