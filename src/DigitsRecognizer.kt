import java.io.File

data class Observation (val label: String , val Pixels: IntArray)

typealias Distance = (IntArray, IntArray) -> Int
typealias Classifier = (IntArray) -> String
typealias Observations = Array<Observation>

fun main(args: Array<String>)
{
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

  val trainingPath = "./Data/trainingsample.csv"
  val trainingData = reader(trainingPath)

  val validationPath = "./Data/validationsample.csv"
  val validationData = reader(validationPath)

  val manhattanDistance = { pixels1: IntArray, pixels2: IntArray ->   // Using zip and map
    val dist = pixels1.zip(pixels2)
        .map { p -> Math.abs(p.first - p.second) }
        .sum()
    dist
  }

  val manhattanDistance2 = fun(pixels1: IntArray, pixels2: IntArray) : Int // Using all imperative
  {
    var dist = 0

    for (i in 0 until pixels1.size)
    {
      dist += Math.abs(pixels1[i] - pixels2[i])
    }

    return dist
  }

  val euclideanDistance = { pixels1: IntArray, pixels2: IntArray ->
      val dist = pixels1.zip(pixels2)
//        .map { p -> Math.pow((p.first - p.second).toDouble(), 2.0) }
        .map({ p -> val dist = (p.first - p.second); dist * dist })
        .sum()
    dist
  }

  fun classify(trainingSet: Observations, dist: Distance, pixels: IntArray) : String
  {
    val observation =  trainingSet.minBy { x -> dist(x.Pixels, pixels) }
    return observation!!.label
  }

  val manhattanClassifier = fun(pixels: IntArray) : String
  {
    return classify(trainingData, manhattanDistance2, pixels)
  }

  fun evaluate(validationSet: Observations, classifier: Classifier) : Unit
  {
    val average = validationSet
        .map { x -> if (classifier(x.Pixels) == x.label) 1.0 else 0.0 }
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

  val startTime = System.currentTimeMillis()

  println("  Manhattan Kotlin")
  evaluate(validationData, manhattanClassifier)

  val endTime = System.currentTimeMillis()
  val elapsedTime = (endTime - startTime) / 1000.0

  println(">>> Elapsed time is: $elapsedTime sec")
}
