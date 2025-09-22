
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
    if (!inputDir.exists() || inputDir.listFiles().isNullOrEmpty()) {
        error("Input directory not found or empty: ${inputDir.absolutePath}")
    }
    val outputDir = File("build/benchmark_output")
    if (!outputDir.exists()) outputDir.mkdirs()

    val docsDir = File("../docs")
    if (!docsDir.exists()) docsDir.mkdirs()

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
            results.add(Triple(mode.name, -1, avgTime))
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

    val csvFile = File(docsDir, "benchmark.csv")
    csvFile.printWriter().use { out ->
        out.println("mode,buffer,time_ms")
        results.forEach { (mode, buffer, time) ->
            val bufferStr = if (buffer == -1) "" else buffer.toString()
            out.println("$mode,$bufferStr,$time")
        }
    }

    val chart = CategoryChartBuilder().width(1000).height(600)
        .title("Image Processing Benchmark")
        .xAxisTitle("Mode")
        .yAxisTitle("Time (ms)")
        .build()

    chart.styler.legendPosition = Styler.LegendPosition.InsideNE

    val allModes = results.map { it.first }.distinct()

    val serialTimes = allModes.map { mode ->
        results.find { it.first == "SERIAL" }?.third?.toDouble()
            ?.takeIf { mode == "SERIAL" } ?: Double.NaN
    }
    chart.addSeries("SERIAL", allModes, serialTimes)

    bufferSizes.forEach { buffer ->
        val times = allModes.map { mode ->
            results.find { it.first == mode && it.second == buffer }?.third?.toDouble() ?: Double.NaN
        }
        chart.addSeries("buffer=$buffer", allModes, times)
    }

    BitmapEncoder.saveBitmap(chart, File(docsDir, "benchmark.png").path, BitmapEncoder.BitmapFormat.PNG)
    println("Benchmark completed. CSV saved to benchmark.csv, graph saved to benchmark.png")
}