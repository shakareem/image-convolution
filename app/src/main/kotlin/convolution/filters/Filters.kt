package filters

val ID = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(0.0, 1.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0)
)

val SHIFTLEFT = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(1.0, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0)
)

val SHIFTRIGHT = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 1.0),
    doubleArrayOf(0.0, 0.0, 0.0)
)

val BLUR = arrayOf(
    doubleArrayOf(1.0, 1.0, 1.0),
    doubleArrayOf(1.0, 1.0, 1.0),
    doubleArrayOf(1.0, 1.0, 1.0)
)

val BLACK = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0)
)

val allFilters = arrayOf(ID, SHIFTLEFT, SHIFTRIGHT, BLUR, BLACK)
