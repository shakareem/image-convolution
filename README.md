# Image Convolution — Task 2: Parallel Implementation

This branch extends Task 1 with **parallel convolution strategies** using Kotlin coroutines.

## Parallelization Strategies

| Mode | Description |
|------|-------------|
| `SERIAL` | Single-threaded (Task 1 baseline) |
| `ROWS` | One coroutine per row — cache-friendly (row-major access) |
| `COLUMNS` | One coroutine per column — cache-unfriendly (column-major access on row-major data) |
| `GRID` | 2D rectangular grid of blocks — configurable `numBlocksY × numBlocksX` |
| `ALLPROCESSORS` | Rows divided evenly across all CPU cores — minimal overhead |
| `PIXELWISE` | One coroutine per pixel — extreme overhead, for observation only |

## Usage

```bash
./gradlew run --args="-i input.bmp -o output.bmp -f blur -m ROWS"
```

### CLI Arguments

```
-i  Input image file (required)
-o  Output image file (default: output.bmp)
-f  Filter name: id, black, blur, sharpen, edge, left, right (required)
-m  Mode: SERIAL, ROWS, COLUMNS, ALLPROCESSORS (default: ALLPROCESSORS)
```

## Running Tests

```bash
./gradlew test
```

All parallel strategies are verified to produce identical results to the serial implementation.

## Benchmarking

```bash
./gradlew benchmark
```

Results are saved to `docs/benchmark_task2.csv`.

## Performance Analysis

See [analysis/task2.md](analysis/task2.md) for full benchmark results, cache locality analysis,
and comparison with Task 1 serial baseline.
