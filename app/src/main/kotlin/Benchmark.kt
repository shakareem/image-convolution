
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.style.Styler
import pipeline.Mode
import pipeline.processDataset
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun diagonalMatrix(size: Int): Array<DoubleArray> {
    return Array(size) { row ->
        DoubleArray(size) { col ->
            if (row == col) 1.0 else 0.0
        }
    }
}

fun main() {
    val projectDir = Paths.get("").toAbsolutePath().toString()
    val inputDir = File(projectDir, "src/test/resources")
    // generateDataset(inputDir)
    if (!inputDir.exists() || inputDir.listFiles().isNullOrEmpty()) {
        error("Input directory not found or empty: ${inputDir.absolutePath}")
    }
    val outputDir = File("build/benchmark_output")
    if (!outputDir.exists()) outputDir.mkdirs()

    val kernel = diagonalMatrix(11)

    val modes = Mode.values().toList()
    val bufferSizes = listOf(1, 4, 8, 16, 32)
    val repeats = 5

    val results = mutableListOf<Triple<String, Int, Long>>()

    for (mode in modes) {
        if (mode == Mode.PIXELWISE) {
            continue
        } else if (mode == Mode.SERIAL) {
            val times = mutableListOf<Long>()
            repeat(repeats) {
                val time = measureTimeMillis {
                    processDataset(inputDir, outputDir, kernel, mode, 1)
                }
                println("Run ${it + 1}: $time ms")
                times.add(time)
            }

            val avgTime = times.average().toLong()
            println("Average: $avgTime ms\n")
            results.add(Triple(mode.name, 1, avgTime))
            continue
        }

        for (buffer in bufferSizes) {
            println("Benchmarking $mode with buffer $buffer ...")

            val times = mutableListOf<Long>()
            repeat(repeats) {
                val time = measureTimeMillis {
                    processDataset(inputDir, outputDir, kernel, mode, buffer)
                }
                println("Run ${it + 1}: $time ms")
                times.add(time)
            }

            val avgTime = times.average().toLong()
            println("Average: $avgTime ms\n")
            results.add(Triple(mode.name, buffer, avgTime))
        }
    }

    val csvFile = File(outputDir, "benchmark.csv")
    csvFile.printWriter().use { out ->
        out.println("mode,buffer,time_ms")
        results.forEach { (mode, buffer, time) ->
            out.println("$mode,$buffer,$time")
        }
    }

    val chart = CategoryChartBuilder().width(1000).height(600)
        .title("Image Processing Benchmark")
        .xAxisTitle("Mode")
        .yAxisTitle("Time (ms)")
        .build()

    chart.styler.legendPosition = Styler.LegendPosition.InsideNE

    bufferSizes.forEach { buffer ->
        val subset = results.filter { it.second == buffer }
        val modeLabels = subset.map { it.first }
        val times = subset.map { it.third }
        chart.addSeries("buffer=$buffer", modeLabels, times)
    }

    BitmapEncoder.saveBitmap(chart, File(outputDir, "benchmark.png").path, BitmapEncoder.BitmapFormat.PNG)
    println("Benchmark completed. CSV saved to benchmark.csv, graph saved to benchmark.png")
}

fun generateDataset(outDir: File) {
    if (!outDir.exists()) outDir.mkdirs()

    val size = 32
    val types = listOf("gradient", "checker", "circle", "noise")

    var counter = 1
    for (type in types) {
        repeat(4) { variant -> // 4 варианта каждого типа → 16 изображений
            val name = "${type}_$variant.bmp"
            generateImage(size, name, outDir) { x, y ->
                when (type) {
                    "gradient" -> {
                        val v = ((255.0 * x / size) + variant * 4).toInt().coerceIn(0, 255)
                        Color(v, v, v)
                    }
                    "checker" -> {
                        val v = if ((x / 4 + y / 4 + variant) % 2 == 0) 255 else 0
                        Color(v, v, v)
                    }
                    "circle" -> {
                        val cx = size / 2
                        val cy = size / 2
                        val r = size / 3
                        val dist = (x - cx) * (x - cx) + (y - cy) * (y - cy)
                        val v = if (dist < r * r) 255 else 0
                        Color(v, v, v)
                    }
                    "noise" -> {
                        val v = (Random.nextInt(256) + variant * 4).coerceIn(0, 255)
                        Color(v, v, v)
                    }
                    else -> Color(0, 0, 0)
                }
            }
            counter++
        }
    }

    println("Generated 16 test images in: ${outDir.absolutePath}")
}

fun generateImage(size: Int, name: String, outDir: File, pixel: (x: Int, y: Int) -> Color) {
    val img = BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY)
    for (y in 0 until size) {
        for (x in 0 until size) {
            img.setRGB(x, y, pixel(x, y).rgb)
        }
    }
    ImageIO.write(img, "bmp", File(outDir, name))
}
