package inference

import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import scala.util.Using

object Inference extends App {

    val features: Array[Float] = Array()
    val weights: Array[Array[Float]] = Array() 

    Using.resource(NDManager.newBaseManager()) { manager =>
        val ndModel = manager.create(weights)

        val mat = manager.create(features, new Shape(1, features.length))
        val logp = mat.dot(ndModel)

        val probas = logp.softmax(0)
        val array = probas.toFloatArray
        val highPrecision = array.map(_.toDouble)
        val total = highPrecision.sum
        highPrecision.mapInPlace(_ / total)
    }
}
