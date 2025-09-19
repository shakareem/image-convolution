package convolution

class SerialConvolver : Convolver {
    override fun convolve(image: Array<DoubleArray>, kernel: Array<DoubleArray>): Array<DoubleArray> {
        val imageHeight = image.size
        val imageWidth = image[0].size
        val kernelHeight = kernel.size
        val kernelWidth = kernel[0].size
        val padHeight = kernelHeight / 2
        val padWidth = kernelWidth / 2

        val paddedImage = Array(imageHeight + 2 * padHeight) { DoubleArray(imageWidth + 2 * padWidth) }
        for (i in image.indices) {
            for (j in image[i].indices) {
                paddedImage[i + padHeight][j + padWidth] = image[i][j]
            }
        }

        val output = Array(imageHeight) { DoubleArray(imageWidth) }

        for (i in 0 until imageHeight) {
            for (j in 0 until imageWidth) {
                var sum = 0.0
                for (ki in 0 until kernelHeight) {
                    for (kj in 0 until kernelWidth) {
                        sum += kernel[ki][kj] * paddedImage[i + ki][j + kj]
                    }
                }
                output[i][j] = sum
            }
        }

        return output
    }
}
