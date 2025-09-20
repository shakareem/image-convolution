package convolution

import allWorkersConvolve
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParallelConvolutionTest {
    private val smallWidth = 10
    private val smallHeight = 10

    @Test
    fun `parallel convolve white image with zero kernel is the same as serial`() {
        val image = Array(10) { DoubleArray(10) { 255.0 } }

        val blackenKernel = arrayOf(doubleArrayOf(0.0))
        val blackenedSerial = serialConvolve(image, blackenKernel)
        val blackenedParallel = runBlocking {
            allWorkersConvolve(image, blackenKernel)
        }
        assertTrue(blackenedParallel.contentDeepEquals(blackenedSerial))
    }
}
