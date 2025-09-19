package images

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class ImageReaderWriter {
    fun readImage(filePath: String): Array<DoubleArray> {
        val img: Mat = Imgcodecs.imread(filePath)
        val gray = Mat()
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY)

        val height = gray.rows()
        val width = gray.cols()
        val array = Array(height) { DoubleArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                array[y][x] = gray.get(y, x)[0]
            }
        }
        return array
    }

    fun writeImage(image: Array<DoubleArray>, filePath: String) {
        val height = image.size
        val width = image[0].size
        val mat = Mat(height, width, CvType.CV_8U)

        for (y in 0 until height) {
            for (x in 0 until width) {
                mat.put(y, x, image[y][x].coerceIn(0.0, 255.0))
            }
        }
        Imgcodecs.imwrite(filePath, mat)
    }
}
