scalaVersion := "3.3.1"
name := "scala-io"
organization := "ai.numind"

val djlVersion = "0.26.0"

libraryDependencies ++= Seq(
    "io.qdrant" % "client"  % "1.7.1",
    "ai.djl" % "api"  % djlVersion,
    "ai.djl" % "basicdataset"  % djlVersion,
    "ai.djl.llama" % "llama"  % djlVersion,
    "ai.djl.pytorch" % "pytorch-engine"  % djlVersion,
    "ai.djl.huggingface" % "tokenizers"  % djlVersion,
)