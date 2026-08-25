package io.confluent.developer.k8s

import io.fabric8.kubernetes.api.model.SecretBuilder
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import java.io.File

fun main() {

  val ccloudConfig = File("/Users/viktor/.ccloud/config").readText().toBase64()

  val client = DefaultKubernetesClient(Config.autoConfigure(null)).inNamespace("default")

  val secret = SecretBuilder()
      .withType("Opaque")
      .withNewMetadata()
        .withName("ccloud")
      .endMetadata()
      .withData(mapOf("config" to ccloudConfig))
      .build()

  client.secrets().createOrReplace(secret)
}
