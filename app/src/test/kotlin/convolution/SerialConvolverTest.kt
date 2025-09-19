package convolution

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SerialConvolverTest {
    private val smallWidth = 10
    private val smallHeight = 10

    @Test
    fun `convolve white image with zero kernel becomes black`() {
        val image = Array(10) { DoubleArray(10) { 255.0 } }

        val blackenKernel = arrayOf(doubleArrayOf(0.0))
        val blackened = SerialConvolver().convolve(image, blackenKernel)

        assertTrue(blackened.all { row -> row.all { it == 0.0 } })
    }
}
