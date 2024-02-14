package inference

import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import scala.util.Using

object Inference {

    val weights: Array[Array[Float]] = ???
    val features: Array[Float] = ???

    Using.resource(NDManager.newBaseManager()) { manager =>
        val ndModel = manager.create(weights)

        val mat = manager.create(features, new Shape(1, features.length))
        val logp = mat.dot(ndModel)

        val probas = logp.softmax(0)
        probas.toFloatArray
    }
}
