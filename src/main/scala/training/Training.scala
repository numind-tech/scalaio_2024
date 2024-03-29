package training

import ai.djl.Application
import ai.djl.Model
import ai.djl.ModelException
import ai.djl.basicdataset.tabular.CsvDataset
import ai.djl.basicdataset.tabular.utils.DynamicBuffer
import ai.djl.basicdataset.tabular.utils.Feature
import ai.djl.basicdataset.tabular.utils.Featurizer.DataFeaturizer
import ai.djl.engine.Engine
import ai.djl.metric.Metrics
import ai.djl.inference.*
import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.Vocabulary
import ai.djl.modality.nlp.bert.BertFullTokenizer
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDList
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import ai.djl.nn.Activation
import ai.djl.nn.Block
import ai.djl.nn.LambdaBlock
import ai.djl.nn.SequentialBlock
import ai.djl.nn.core.Linear
import ai.djl.nn.norm.Dropout
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.DefaultTrainingConfig
import ai.djl.training.EasyTrain
import ai.djl.training.ParameterStore
import ai.djl.training.Trainer
import ai.djl.training.TrainingResult
import ai.djl.training.dataset.RandomAccessDataset
import ai.djl.training.evaluator.Accuracy
import ai.djl.training.listener.SaveModelTrainingListener
import ai.djl.training.listener.TrainingListener
import ai.djl.training.loss.Loss
import ai.djl.training.util.ProgressBar
import ai.djl.translate.*
import ai.djl.ndarray.NDManager
import scala.util.*
import scala.collection.mutable
import scala.collection.JavaConverters.*
import java.util.Locale
import java.nio.file.*
import java.io.*
import org.apache.commons.csv.CSVFormat

object Training extends App {
    // For cuda 2.x, for versions 1.x or cpu, comment this line
    System.setProperty("PYTORCH_FLAVOR", "cu123")
    // System.setProperty("PYTORCH_FLAVOR", "cpu")
    System.setProperty("ai.djl.pytorch.graph_optimizer", "false")

    val outputDir: String = "target/models"
    val epochs = 2
    val batchSize = 32
    val limit = 200

    Files.createDirectories(Path.of(outputDir))
    Files.list(Path.of(outputDir)).forEach(Files.delete)

    val modelUrls = "https://resources.djl.ai/test-models/traced_distilbert_wikipedia_uncased.zip"

    val maxTokenLenght = 64

    val model: Model = Model.newInstance("AmazonReviewRatingClassification")

    val embedding = Criteria.builder()
        .optApplication(Application.NLP.WORD_EMBEDDING)
        .setTypes(classOf[NDList], classOf[NDList])
        .optModelUrls(modelUrls)
        .optEngine("PyTorch")
        .optProgress(new ProgressBar())
        .optOption("trainParam", "true")
        .build()
        .loadModel()

    def createNewLayer(block: Block) = {
        val classifier = new SequentialBlock()

        val lambda = new LambdaBlock({ ndList =>
            val data: NDArray = ndList.singletonOrThrow()
            val inputs = new NDList()
            inputs.add(data.toType(DataType.INT64, false))
            inputs.add(data.getManager.full(data.getShape, 1, DataType.INT64))
            inputs.add(
                data.getManager.arange(data.getShape.get(1))
                    .toType(DataType.INT64, false)
                    .broadcast(data.getShape))
            inputs
        })

        classifier.add(lambda)
        classifier.add(block)


        classifier
            .add(Linear.builder().setUnits(768).build()) // pre classifier
            .add(Activation.relu(_: NDList))
            .add(Dropout.builder().optRate(0.2f).build())
            .add(Linear.builder().setUnits(5).build()) // 5 star rating
            .addSingleton(_.get(":,0")) // follow HF classifier
    }

    model.setBlock(createNewLayer(embedding.getBlock()))

    val vocabulary = DefaultVocabulary.builder()
        .addFromTextFile(embedding.getArtifact("vocab.txt"))
        .optUnknownToken("[UNK]").build()

    val tokenizer = new BertFullTokenizer(vocabulary, true)

    val datasets = loadDataset().randomSplit(70, 30)
    val trainingSet = datasets(0)
    val validationSet = datasets(1)


    val listener: SaveModelTrainingListener = new SaveModelTrainingListener(outputDir)
    listener.setSaveModelCallback { trainer => 
        val result: TrainingResult = trainer.getTrainingResult
        val model: Model = trainer.getModel
        val accuracy: Float = result.getValidateEvaluation("Accuracy").toFloat
        model.setProperty("Accuracy", f"${accuracy}%.5f")
        model.setProperty("Loss", f"${result.getValidateLoss}%.5f")
        println("Model was saved")
    }

    val config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .addEvaluator(new Accuracy())
                    .addTrainingListeners(TrainingListener.Defaults.logging(outputDir)*)
                    .addTrainingListeners(listener)


    Using.resource(model.newTrainer(config)) { trainer =>
        trainer.setMetrics(new Metrics())
        trainer.initialize(new Shape(batchSize, maxTokenLenght))
        EasyTrain.fit(trainer, epochs, trainingSet, validationSet)
    }


    val translator = new Translator[String, Array[Float]] {
        override def processInput(
            ctx: TranslatorContext,
            input: String
        ): NDList = {
            val vocab = tokenizer.getVocabulary
            var tokens = tokenizer.tokenize(input.toLowerCase(Locale.ENGLISH))
            if (tokens.size > maxTokenLenght) {
            tokens = tokens.subList(0, maxTokenLenght)
            }
            tokens.add(0, "[CLS]")
            tokens.add("[SEP]")
            val features = tokens.asScala.map(vocab.getIndex(_)).toArray
            val  manager = ctx.getNDManager()
            new NDList(manager.create(features))
            
        }

        override def processOutput(
            ctx: TranslatorContext,
            list: NDList
        ): Array[Float] = {
            println(list.getShapes.mkString(","))
            list.singletonOrThrow().toType(DataType.FLOAT32, true).softmax(0).toFloatArray()
        }
    }


    val t = model.newPredictor(translator)
    println(t.predict("Hello, scala.io").mkString("[", ",", "]"))
    t.close()
    model.close()

    val tmp = Criteria.builder()
        .optApplication(Application.NLP.WORD_EMBEDDING)
        .setTypes(classOf[NDList], classOf[NDList])
        .optModelUrls(modelUrls)
        .optEngine("PyTorch")
        .optProgress(new ProgressBar())
        .optOption("trainParam", "false")
        .build()
        .loadModel()

    val reloaded = Model.newInstance("result")
    reloaded.setBlock(createNewLayer(tmp.getBlock()))
    tmp.close()

    reloaded.load(
        Path.of(outputDir), 
        "AmazonReviewRatingClassification", 
        Map("epoch" -> epochs.toString).asJava
    )

    val predictor = reloaded.newPredictor(translator)
    println(predictor.predict("Hello, scala.io").mkString("[", ",", "]"))

    def loadDataset() = {
        val amazonReview = "https://mlrepo.djl.ai/dataset/nlp/ai/djl/basicdataset/amazon_reviews/1.0/amazon_reviews_us_Digital_Software_v1_00.tsv.gz"
        val paddingToken = tokenizer.getVocabulary().getIndex("[PAD]")
        val featurizer = new DataFeaturizer {
            override def featurize(buf: DynamicBuffer, input: String): Unit = {
                val vocab = tokenizer.getVocabulary
                var tokens = tokenizer.tokenize(input.toLowerCase(Locale.ENGLISH))
                if (tokens.size > maxTokenLenght) {
                tokens = tokens.subList(0, maxTokenLenght)
                }
                buf.put(vocab.getIndex("[CLS]"))
                tokens.asScala.foreach(token => buf.put(vocab.getIndex(token)))
                buf.put(vocab.getIndex("[SEP]"))
            }
        }
        CsvDataset.builder()
            .optCsvUrl(amazonReview)
            .setCsvFormat(CSVFormat.TDF.builder().setQuote(null).setHeader().build())
            .setSampling(batchSize, true)
            .addFeature(new Feature("review_body", featurizer))
            .addLabel(new Feature("star_rating", (buf, data) => buf.put(data.toFloat - 1.0f)))
            .optDataBatchifier(
            PaddingStackBatchifier
                .builder()
                .optIncludeValidLengths(false)
                .addPad(0, 0, (m: NDManager) => m.ones(new Shape(1)).mul(paddingToken))
                .build()
            ).optLimit(limit)
            .build()
    }
}
