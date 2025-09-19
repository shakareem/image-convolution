import convolution.serialConvolve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun allWorkersConvolve(image: Array<DoubleArray>, kernel: Array<DoubleArray>): Array<DoubleArray> {
    require(kernel.size % 2 == 1 && kernel[0].size % 2 == 1) { "Kernel dimensions must be odd" }
    val height = image.size
    val width = image[0].size
    val output = Array(height) { DoubleArray(width) }
    val workers = Runtime.getRuntime().availableProcessors()
    coroutineScope {
        val chunkSize = (height + workers - 1) / workers
        val jobs = (0 until workers).map { workerId ->
            launch(Dispatchers.Default) {
                val start = workerId * chunkSize
                val end = minOf(start + chunkSize, height)
                if (start < end) {
                    val part = serialConvolve(image.sliceArray(start until end), kernel)
                    for (i in part.indices) {
                        output[start + i] = part[i]
                    }
                }
            }
        }
        jobs.joinAll()
    }
    return output
}
