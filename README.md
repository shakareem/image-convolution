# Image Convolution — Task 3: Pipeline Processing

This branch extends Task 2 with a **streaming pipeline** for processing datasets of images.

## Pipeline Architecture

```
readDataset ──[buffer]──► processImages ──[buffer]──► writeDataset
 (producer)                (processor)                (consumer)
```

- **readDataset**: reads files sequentially, sends to buffered channel
- **processImages**: processes each image in a separate coroutine on `Dispatchers.Default`
- **writeDataset**: writes results sequentially
- **Backpressure**: buffered channels prevent unbounded memory growth

## Usage

### Single image

```bash
./gradlew run --args="-i input.bmp -o output/ -f blur -m ALLPROCESSORS"
```

### Dataset (directory)

```bash
./gradlew run --args="-i images/ -o processed/ -f sharpen -m ROWS -b 8"
```

### CLI Arguments

```
-i  Input file or directory (required)
-o  Output directory (default: .)
-f  Filter name: id, black, blur, sharpen, edge, left, right (required)
-m  Mode: SERIAL, ROWS, COLUMNS, ALLPROCESSORS, GRID (default: ALLPROCESSORS)
-b  Buffer size for pipeline channels (default: 8)
```

## Running Tests

```bash
./gradlew test
```

## Benchmarking

```bash
./gradlew benchmark
```

Results are saved to `docs/benchmark_task3.csv`.

## Performance Analysis

See [analysis/task3.md](analysis/task3.md) for full benchmark results, backpressure analysis,
memory pressure estimates, and comparison with Task 1 and Task 2.
