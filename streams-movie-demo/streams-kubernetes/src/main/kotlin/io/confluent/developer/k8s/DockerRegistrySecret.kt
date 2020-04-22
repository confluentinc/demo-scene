package io.confluent.developer.k8s

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newSecret
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main() {
  val jsonText = DockerRegistrySecret::class.java.classLoader.getResourceAsStream(".dockerconfig.json").bufferedReader().readText()

  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  // make sure updated `src/main/resources/.dockerconfig.json` with actual values
  client.secrets().createOrReplace(newSecret {
    data = mapOf(".dockerconfigjson" to jsonText.toBase64())
    type = "kubernetes.io/dockerconfigjson"
    metadata {
      name = "regcred"
    }
  })
}

class DockerRegistrySecret 