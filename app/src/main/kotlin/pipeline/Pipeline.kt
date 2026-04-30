package pipeline

import convolution.allProcessorsConvolve
import convolution.columnsConvolve
import convolution.gridConvolve
import convolution.pixelwiseConvolve
import convolution.rowsConvolve
import convolution.serialConvolve
import images.Bitmap
import images.readImage
import images.writeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import java.io.File

enum class Mode { SERIAL, PIXELWISE, COLUMNS, ROWS, ALLPROCESSORS, GRID }

class Image(
    val bitmap: Bitmap,
    val name: String,
)

/**
 * Pipeline configuration.
 *
 * @param numReaders  number of parallel file readers (I/O bound)
 * @param numWorkers  number of parallel convolution workers (CPU bound); 0 = one per image
 * @param numWriters  number of parallel file writers (I/O bound)
 * @param bufferSize  capacity of each inter-stage channel (backpressure / memory limit)
 */
data class PipelineConfig(
    val numReaders: Int = 1,
    val numWorkers: Int = Runtime.getRuntime().availableProcessors(),
    val numWriters: Int = 1,
    val bufferSize: Int = 8,
)

fun processSingleFile(
    file: File,
    outDir: File,
    kernel: Bitmap,
    mode: Mode,
) {
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
    bufferSize: Int,
) = processDataset(inDirectory, outDirectory, kernel, mode, PipelineConfig(bufferSize = bufferSize))

fun processDataset(
    inDirectory: File,
    outDirectory: File,
    kernel: Bitmap,
    mode: Mode,
    config: PipelineConfig,
) = runBlocking {
    if (!outDirectory.exists()) outDirectory.mkdirs()

    val files = inDirectory.listFiles { f -> f.isFile }?.toList() ?: return@runBlocking

    // Channel between reader(s) and worker(s)
    // bufferSize limits how many decoded images sit in memory waiting for processing
    val readChannel = Channel<Image>(capacity = config.bufferSize)

    // Channel between worker(s) and writer(s)
    // bufferSize limits how many processed images sit in memory waiting to be written
    val writeChannel = Channel<Image>(capacity = config.bufferSize)

    // Semaphore to limit total images in-flight (read + processing + write)
    // Prevents readers from flooding memory when workers or writers are slow
    val inFlight = Semaphore(config.bufferSize * 2)

    // --- READERS ---
    // Divide file list among numReaders; each reader reads its slice sequentially
    val readerJobs =
        (0 until config.numReaders).map { readerId ->
            launch(Dispatchers.IO) {
                val slice = files.filterIndexed { idx, _ -> idx % config.numReaders == readerId }
                for (file in slice) {
                    inFlight.acquire() // block if too many images in flight
                    val bitmap = readImage(file.absolutePath)
                    readChannel.send(Image(bitmap, file.name))
                }
            }
        }

    // Close readChannel after all readers finish
    launch(Dispatchers.Default) {
        readerJobs.forEach { it.join() }
        readChannel.close()
    }

    // --- WORKERS ---
    // numWorkers coroutines each pull from readChannel and push to writeChannel
    // Each worker applies the convolution (possibly parallel internally via mode)
    val workerJobs =
        (0 until config.numWorkers).map {
            launch(Dispatchers.Default) {
                for (image in readChannel) {
                    val result = convolveWithMode(image.bitmap, kernel, mode)
                    writeChannel.send(Image(result, "convolved_" + image.name))
                }
            }
        }

    // Close writeChannel after all workers finish
    launch(Dispatchers.Default) {
        workerJobs.forEach { it.join() }
        writeChannel.close()
    }

    // --- WRITERS ---
    // numWriters coroutines each pull from writeChannel and write to disk
    val writerJobs =
        (0 until config.numWriters).map {
            launch(Dispatchers.IO) {
                for (image in writeChannel) {
                    writeImage(image.bitmap, File(outDirectory, image.name).path)
                    inFlight.release() // image fully processed, free the slot
                }
            }
        }

    // Wait for all writers to finish
    writerJobs.forEach { it.join() }
}

suspend fun convolveWithMode(
    image: Bitmap,
    kernel: Bitmap,
    mode: Mode,
): Bitmap =
    when (mode) {
        Mode.SERIAL -> serialConvolve(image, kernel)
        Mode.PIXELWISE -> pixelwiseConvolve(image, kernel)
        Mode.COLUMNS -> columnsConvolve(image, kernel)
        Mode.ROWS -> rowsConvolve(image, kernel)
        Mode.ALLPROCESSORS -> allProcessorsConvolve(image, kernel)
        Mode.GRID -> gridConvolve(image, kernel)
    }

// Extension for CoroutineScope-based usage (kept for compatibility)
@Suppress("unused")
fun CoroutineScope.readDatasetChannel(
    bufferSize: Int,
    directory: File,
): ReceiveChannel<Image> {
    val channel = Channel<Image>(capacity = bufferSize)
    launch(Dispatchers.IO) {
        try {
            directory.listFiles { f -> f.isFile }?.forEach { file ->
                val image = readImage(file.absolutePath)
                channel.send(Image(image, file.name))
            }
        } finally {
            channel.close()
        }
    }
    return channel
}
