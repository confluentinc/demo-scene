package io.confluent.developer.k8s

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.*
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main() {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  val appName = "streaming-movie-ratings"
  val defaultLabels = mapOf("app" to appName)

  val deployment = newDeployment {
    metadata {
      name = appName
      namespace = "default"
      labels = defaultLabels
    }

    spec {
      replicas = 1
      selector {
        matchLabels = defaultLabels
      }
      template {
        metadata {
          labels = defaultLabels
        }
        spec {
          imagePullSecrets = listOf(newLocalObjectReference {
            name = "regcred"
          })
          affinity {
            podAffinity {
              requiredDuringSchedulingIgnoredDuringExecution = listOf(newPodAffinityTerm {
                labelSelector {
                  topologyKey = "kubernetes.io/hostname"
                  matchExpressions = listOf(newLabelSelectorRequirement {
                    key = "app"
                    values = listOf(appName)
                    operator = "In"
                  })
                }
              })

            }
          }
          containers = listOf(
              newContainer {
                volumes = listOf(
                    newVolume {
                      name = "config"
                      secret = newSecretVolumeSource {
                        secretName = "ccloud"
                        items = listOf(newKeyToPath {
                          key = "config"
                          path = "config.ccloud"
                        })
                      }
                    }
                )
                volumeMounts = listOf(
                    newVolumeMount {
                      name = "config"
                      mountPath = "/var/config"
                      readOnly = true
                    })
                name = appName
                image = "gamov-docker.jfrog.io/dev/streaming-movie-ratings:latest"
                env = listOf(newEnvVar {
                  name = "JAVA_TOOL_OPTIONS"
                  value = "-DLOGLEVEL=INFO"
                })
              }
          )
        }
      }
    }
  }
  client.apps().deployments().createOrReplace(deployment)
}