package convolution

interface Convolver {
    fun convolve(image: Array<DoubleArray>, kernel: Array<DoubleArray>): Array<DoubleArray>
}
