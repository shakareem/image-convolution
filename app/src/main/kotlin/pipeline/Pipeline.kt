package pipeline

import convolution.allProcessorsConvolve
import convolution.columnsConvolve
import convolution.pixelwiseConvolve
import convolution.rowsConvolve
import convolution.serialConvolve
import images.Bitmap
import images.readImage
import images.writeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

enum class Mode { SERIAL, PIXELWISE, COLUMNS, ROWS, ALLPROCESSORS }

class Image(val bitmap: Bitmap, val name: String)

fun processSingleFile(
    file: File,
    outDir: File,
    kernel: Bitmap,
    mode: Mode
) {
    println("Processing ${file.path} -> ${outDir.path + file.name} with mode $mode")
    if (!outDir.exists()) outDir.mkdirs()
    val image = readImage(file.absolutePath)
    val result = runBlocking { convolveWithMode(image, kernel, mode) }
    writeImage(result, File(outDir, "convolved_" + file.name).path)
}

fun processDataset(
    inDirectory: File,
    outDirectory: File,
    kernel: Bitmap,
    mode: Mode,
    bufferSize: Int
) = runBlocking {
    val inputImages = readDataset(bufferSize, inDirectory)
    val outputImages = processImages(bufferSize, inputImages, kernel, mode)
    writeDataset(outDirectory, outputImages)
}

private suspend fun convolveWithMode(
    image: Bitmap,
    kernel: Bitmap,
    mode: Mode
): Bitmap {
    return when (mode) {
        Mode.SERIAL -> serialConvolve(image, kernel)
        Mode.PIXELWISE -> pixelwiseConvolve(image, kernel)
        Mode.COLUMNS -> columnsConvolve(image, kernel)
        Mode.ROWS -> rowsConvolve(image, kernel)
        Mode.ALLPROCESSORS -> allProcessorsConvolve(image, kernel)
    }
}

// producers close channels when they finish
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private fun CoroutineScope.readDataset(
    bufferSize: Int,
    directory: File
) = produce<Image>(capacity = bufferSize) {
    directory.listFiles { f -> f.isFile }?.forEach { file ->
        val image = readImage(file.absolutePath)
        send(Image(image, file.name))
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private fun CoroutineScope.processImages(
    bufferSize: Int,
    images: ReceiveChannel<Image>,
    kernel: Bitmap,
    mode: Mode
) = produce<Image>(capacity = bufferSize) {
    coroutineScope { // waitgroup
        for (image in images) {
            launch(Dispatchers.Default) {
                val result = convolveWithMode(image.bitmap, kernel, mode)
                send(Image(result, "convolved_" + image.name))
            }
        }
    }
}

private suspend fun writeDataset(
    directory: File,
    images: ReceiveChannel<Image>
) {
    if (!directory.exists()) directory.mkdirs()
    for (image in images) {
        val filePath = File(directory, image.name)
        writeImage(image.bitmap, filePath.path)
    }
}
