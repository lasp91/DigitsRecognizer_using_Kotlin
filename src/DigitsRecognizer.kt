import java.io.File

fun main(args: Array<String>)
{
  data class Observation (val label: String , val Pixels: IntArray)

  fun observationData(csvData: String) : Observation
  {
    val columns = csvData.split(',')
    val label = columns[0]
    val pixels = columns.subList(1, columns.lastIndex)
        .map { it -> it.toInt() }
    return Observation(label, pixels.toIntArray())
  }

  fun reader(path: String) : Array<Observation>
  {
    val lines = File(path)
        .readLines()
        .drop(1)
        .map { it -> observationData(it) }
    return lines.toTypedArray()
  }

  val trainingPath = "./Data/trainingsample.csv"
  val trainingData = reader(trainingPath)

  val validationPath = "./Data/validationsample.csv"
  val validationData = reader(validationPath)

  val manhattanDistance : (IntArray, IntArray) -> Int = { pixels1, pixels2 ->
    val sum = pixels1.zip(pixels2)
        .map { p -> Math.abs(p.first - p.second) }
        .sum()
    sum
  }

  val euclideanDistance : (IntArray, IntArray) -> Int = { pixels1, pixels2 ->
      val sum = pixels1.zip(pixels2)
        .map { p -> Math.pow(Math.abs(p.first - p.second).toDouble(), 2.0) }
        .sum()
    sum.toInt()
  }

  val classify : (Array<Observation>, (IntArray, IntArray) -> Int, IntArray) -> String = { trainingSet, dist, pixels ->
    val observation =  trainingSet.minBy { x -> dist(x.Pixels, pixels) }
    observation!!.label
  }

  val manhattanClassifier : (IntArray) -> String = { pixels ->
    classify(trainingData, manhattanDistance, pixels)
  }

  val evaluate : (Array<Observation>, (IntArray) -> String) -> Unit = { validationSet, classifier ->
    val count = validationSet.size
    val average = validationSet
        .sumBy { x -> if (classifier(x.Pixels) == x.label) { 1 } else { 0 } }
        .toDouble() / count
    println("Correct: $average")
  }

  val startTime = System.currentTimeMillis()

  println("  Manhattan Kotlin")
  evaluate(validationData, manhattanClassifier)

  val endTime = System.currentTimeMillis()
  val elapsedTime = (endTime - startTime) / 1000.0

  println(">>> Elapsed time is: $elapsedTime sec")
}
