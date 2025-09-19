package convolution

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SerialConvolverTest {
    private val smallWidth = 10
    private val smallHeight = 10

    @Test
    fun `convolve white image with invert kernel becomes black`() {
        val image = Array(10) { DoubleArray(10) { 255.0 } }

        val invertKernel = arrayOf(doubleArrayOf(0.0))
        val inverted = SerialConvolver().convolve(image, invertKernel)

        val allBlack = inverted.all { row -> row.all { it == 0.0 } }
        assertTrue(allBlack, "После фильтра все пиксели должны быть черные")
    }
}
