package search

import io.qdrant.client.*
import io.qdrant.client.grpc.Collections.*
import io.qdrant.client.grpc.Points.PointStruct
import io.qdrant.client.PointIdFactory.id
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorsFactory.vectors
import scala.jdk.CollectionConverters.*
import ai.djl.repository.zoo.Criteria
import ai.djl.Application
import ai.djl.huggingface.tokenizers.*
import ai.djl.huggingface.translator.TextEmbeddingTranslator

import java.nio.file.Path
import io.qdrant.client.grpc.Points.Vectors
import scala.util.Try
import io.qdrant.client.grpc.Points.SearchPoints
import io.qdrant.client.grpc.Points.WithPayloadSelector
import io.qdrant.client.grpc.Points.PayloadIncludeSelector

object Embeddings extends App {
  /* 
    Run the docker image with:
      docker run -it --rm -p 6333:6333 -p 6334:6334 qdrant/qdrant
   */

  System.setProperty("ai.djl.pytorch.graph_optimizer", "false")
  val home = System.getenv("HOME")
  val modelPath = Path.of(s"$home/.cache/numind-nlp/models/intfloat/e5-base-v2")

  val tokenizer = HuggingFaceTokenizer.newInstance("intfloat/e5-base-v2")
  val translator = TextEmbeddingTranslator
      .builder(tokenizer,Map("pooling" -> "mean", "normalize" -> "true").asJava)
      .build()

  val extractor = Criteria.builder()
    .optApplication(Application.NLP.TEXT_EMBEDDING)
    .optEngine("PyTorch")
    .setTypes(classOf[String], classOf[Array[Float]])
    .optModelPath(modelPath)
    .optTranslator(translator)
    .build()
    .loadModel()
    .newPredictor()


  println("Loaded model")
  val collection = "scala_io"
  val client = 
    new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())

  creatCollection()
  insertData()

  println("Collection created and initialized")
  
  chooseBooks("Alien invasion")
  chooseBooks("Sentimental novel")
  chooseBooks("Future")
  chooseBooks("Love")

  def creatCollection(): Unit = {
    Try {
      client.createCollectionAsync(collection,
        VectorParams.newBuilder()
          .setDistance(Distance.Cosine)
          .setSize(768)
          .build())
          .get()
    }
    ()
  }

  def insertData(): Unit = {
    val points = Data.texts.map { book =>
      val embeddings = extractor.predict(book.description)
      PointStruct.newBuilder()
        .setVectors(vectors(embeddings*))
        .setId(id(book.id))
        .putAllPayload(
          java.util.Map.of(
            "title", value(book.title),
            "description", value(book.description),
            "author", value(book.author),
            "year", value(book.year)
          )
        )
        .build()
    }
    client.upsertAsync(collection, points.asJava).get()
    ()
  }

  def chooseBooks(question: String): Unit = {
    println("\n\n")
    println(s"Asking about $question")
    val questionEmbeddings = extractor.predict(question)

    val answer = client.searchAsync(
      SearchPoints.newBuilder()
        .setCollectionName(collection)
        .setWithPayload(
          WithPayloadSelector.newBuilder().setInclude(
            PayloadIncludeSelector.newBuilder()
              .addFields("title").addFields("description")
              .build()
          ).build()
        )
        .setLimit(3)
        .addAllVector(questionEmbeddings.map(java.lang.Float.valueOf).toList.asJava)
        .build()
    ).get()

    answer.asScala.foreach { p =>
      println(s"""Score: ${p.getScore()}
      |id: ${p.getId().getNum()}, title: ${p.getPayloadOrThrow("title").getStringValue()}
      |${p.getPayloadOrThrow("description").getStringValue()}
      |-----------------------------------------------------------------------------------""".stripMargin)
    }
  }
}
