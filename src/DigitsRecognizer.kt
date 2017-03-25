import java.io.File
import java.util.stream.IntStream

import java.util.concurrent.atomic.AtomicInteger

data class Observation (val label: String , val Pixels: IntArray)

typealias Distance = (IntArray, IntArray) -> Int
typealias Classifier = (IntArray) -> String
typealias Observations = Array<Observation>

fun observationData(csvData: String) : Observation
{
  val columns = csvData.split(',')
  val label = columns[0]
  val pixels = columns.subList(1, columns.lastIndex).map(String::toInt)

  return Observation(label, pixels.toIntArray())
}

fun reader(path: String) : Array<Observation>
{
  val observations = File(path).useLines { lines ->
      lines.drop(1).map(::observationData).toList().toTypedArray() }

  return observations
}

val manhattanDistance = { pixels1: IntArray, pixels2: IntArray ->   // Using zip and map
  val dist = pixels1.zip(pixels2)
      .map { p -> Math.abs(p.first - p.second) }
      .sum()
  dist
}

val manhattanDistanceImperative = fun(pixels1: IntArray, pixels2: IntArray) : Int
{
  var dist = 0

  for (i in 0 until pixels1.size)
    dist += Math.abs(pixels1[i] - pixels2[i])

  return dist
}

val manhattanDistanceParallel = fun(pixels1: IntArray, pixels2: IntArray) : Int
{
  val dist = AtomicInteger(0)

  IntStream.range(0, pixels1.size).parallel().forEach { i ->
    dist.addAndGet(Math.abs(pixels1[i] - pixels2[i])) }

  return dist.toInt()
}

val euclideanDistance = { pixels1: IntArray, pixels2: IntArray ->
    val dist = pixels1.zip(pixels2)
//      .map { p -> Math.pow((p.first - p.second).toDouble(), 2.0) }
      .map({ p -> val dist = (p.first - p.second); dist * dist })
      .sum()
  dist
}

fun classify(trainingSet: Array<Observation>, dist: Distance, pixels: IntArray) : String
{
  val observation =  trainingSet.minBy { (_, Pixels) -> dist(Pixels, pixels) }
  return observation!!.label
}

fun evaluate(validationSet: Array<Observation>, classifier: (IntArray) -> String) : Unit
{
  val average = validationSet
      .map({ (label, Pixels) -> if (classifier(Pixels) == label) 1.0 else 0.0 })
      .average()
  println("Correct: $average")
}
//    val count = validationSet.size
//    var sum = 0
//
//    for (i in 0 .. count - 1)
//    {
//      if (classifier(validationSet[i].Pixels) == validationSet[i].label)
//      {
//        sum += 1
//      }
//    }
//
//    println("Correct:  ${sum.toDouble() / count}")
//  }

//-----------------------------------------------------------------------------------------

fun main(args: Array<String>)
{
  val trainingPath = "./Data/trainingsample.csv"
  val trainingData = reader(trainingPath)

  val validationPath = "./Data/validationsample.csv"
  val validationData = reader(validationPath)

  val manhattanClassifier = fun(pixels: IntArray) : String
  {
    return classify(trainingData, manhattanDistanceImperative, pixels)
  }

  val startTime = System.currentTimeMillis()

  println("  Manhattan Kotlin")
  evaluate(validationData, manhattanClassifier)

  val endTime = System.currentTimeMillis()
  val elapsedTime = (endTime - startTime) / 1000.0

  println(">>> Elapsed time is: $elapsedTime sec")
}
