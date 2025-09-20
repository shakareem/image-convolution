import convolution.convolvePixel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

suspend fun parallelConvolve(
    image: Array<DoubleArray>,
    kernel: Array<DoubleArray>,
    dispatchStrategy: suspend (output: Array<DoubleArray>, kernel: Array<DoubleArray>) -> Unit
): Array<DoubleArray> {
    require(kernel.size % 2 == 1 && kernel[0].size % 2 == 1) { "Kernel dimensions must be odd" }
    val output = Array(image.size) { DoubleArray(image[0].size) }
    coroutineScope {
        dispatchStrategy(output, kernel)
    }
    return output
}

suspend fun pixelwiseStrategy(image: Array<DoubleArray>, kernel: Array<DoubleArray>, output: Array<DoubleArray>) {
    coroutineScope {
        for (y in 0 until image.size) {
            for (x in 0 until image[0].size) {
                launch(Dispatchers.Default) {
                    output[y][x] = convolvePixel(image, kernel, y, x)
                }
            }
        }
    }
}

suspend fun columnStrategy(image: Array<DoubleArray>, kernel: Array<DoubleArray>, output: Array<DoubleArray>) {
    coroutineScope {
        for (y in 0 until image.size) {
            launch(Dispatchers.Default) {
                for (x in 0 until image[0].size) {
                    output[y][x] = convolvePixel(image, kernel, y, x)
                }
            }
        }
    }
}

suspend fun rowsStrategy(image: Array<DoubleArray>, kernel: Array<DoubleArray>, output: Array<DoubleArray>) {
    coroutineScope {
        for (x in 0 until image[0].size) {
            launch(Dispatchers.Default) {
                for (y in 0 until image.size) {
                    output[y][x] = convolvePixel(image, kernel, y, x)
                }
            }
        }
    }
}

suspend fun allWorkersStrategy(image: Array<DoubleArray>, kernel: Array<DoubleArray>, output: Array<DoubleArray>) {
    val imageHeight = image.size
    val imageWidth = image[0].size
    val numProcessors = Runtime.getRuntime().availableProcessors()
    val rowsPerChunk = (imageHeight + numProcessors - 1) / numProcessors

    coroutineScope {
        for (chunk in 0 until numProcessors) {
            val startRow = chunk * rowsPerChunk
            val endRow = min(startRow + rowsPerChunk, imageHeight)

            if (startRow >= imageHeight) {
                launch(Dispatchers.Default) {
                    for (y in startRow until endRow) {
                        for (x in 0 until imageWidth) {
                            output[y][x] = convolvePixel(image, kernel, y, x)
                        }
                    }
                }
            }
        }
    }
}
