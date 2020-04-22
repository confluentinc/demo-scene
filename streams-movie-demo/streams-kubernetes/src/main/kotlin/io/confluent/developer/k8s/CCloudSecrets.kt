package io.confluent.developer.k8s

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newSecret
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import java.io.File

fun main() {

  val ccloudConfig = File("/Users/viktor/.ccloud/config").readText().toBase64()

  val client = DefaultKubernetesClient(Config.autoConfigure(null)).inNamespace("default")
  
  client.secrets().createOrReplace(newSecret { 
    type = "Opaque"
    metadata {
      name = "ccloud"
    }
    data = mapOf("config" to ccloudConfig)
  })

}