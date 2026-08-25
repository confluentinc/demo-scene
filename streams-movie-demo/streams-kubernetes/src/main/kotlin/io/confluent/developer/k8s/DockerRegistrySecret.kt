package io.confluent.developer.k8s

import io.fabric8.kubernetes.api.model.SecretBuilder
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main() {
  val jsonText = DockerRegistrySecret::class.java.classLoader.getResourceAsStream(".dockerconfig.json").bufferedReader().readText()

  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  // make sure updated `src/main/resources/.dockerconfig.json` with actual values
  val secret = SecretBuilder()
      .withNewMetadata()
        .withName("regcred")
      .endMetadata()
      .withType("kubernetes.io/dockerconfigjson")
      .withData(mapOf(".dockerconfigjson" to jsonText.toBase64()))
      .build()

  client.secrets().createOrReplace(secret)
}

class DockerRegistrySecret
