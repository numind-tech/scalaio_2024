package chatbot

import ai.djl.ModelException
import ai.djl.inference.Predictor
import ai.djl.llama.engine.LlamaInput
import ai.djl.llama.engine.LlamaTranslatorFactory
import ai.djl.llama.jni.Token
import ai.djl.llama.jni.TokenIterator
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import ai.djl.translate.TranslateException

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.Set
import scala.util.Using

object Chatbot extends App {

    val name = "LLM"
    val modelId = "TheBloke/Mistral-7B-Instruct-v0.2-GGUF";
    val quantMethod = "Q4_K_M";
    // val modelId = "TinyLlama/TinyLlama-1.1B-Chat-v0.6"
    // val quantMethod = "Q4_0"

    val url = s"djl://ai.djl.huggingface.gguf/$modelId/0.0.1/$quantMethod"

    val criteria = Criteria.builder()
                        .setTypes(classOf[LlamaInput], classOf[TokenIterator])
                        .optModelUrls(url)
                        .optEngine("Llama")
                        .optOption("number_gpu_layers", "43")
                        .optTranslatorFactory(new LlamaTranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();

    val system =
                s"""|This is demo for DJL Llama.cpp engine.
                   |
                   |
                   |$name: Hello.  How may I help you today?""".stripMargin


    val param = new LlamaInput.Parameters();
        param.setTemperature(0.7f);
        param.setPenalizeNl(true);
        param.setMirostat(2);
        param.setAntiPrompt(Array("User: "));

        val in = new LlamaInput();
        in.setParameters(param);

        val reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Using.resource (criteria.loadModel()) { model =>
            Using.resource(model.newPredictor()) {predictor =>
            System.out.print(system);
            val prompt = new StringBuilder(system);
            val exitWords = Set.of("exit", "bye", "quit");
            while (true) {
                print("\nUser: ");
                val input = reader.readLine().trim();
                if (exitWords.contains(input.toLowerCase(Locale.ROOT))) {
                    println("Goodbye!")
                    System.exit(0)
                }
                print(s"$name: ");
                prompt.append("\nUser: ").append(input).append(s"\n$name: ");
                in.setInputs(prompt.toString());
                val it = predictor.predict(in);
                while (it.hasNext()) {
                    val token = it.next();
                    print(token.getText());
                    prompt.append(token.getText());
                }
            }
        }
    }

}
