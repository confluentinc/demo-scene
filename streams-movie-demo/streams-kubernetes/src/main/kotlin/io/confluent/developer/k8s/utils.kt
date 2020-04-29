package io.confluent.developer.k8s

import java.util.Base64.getDecoder
import java.util.Base64.getEncoder

fun String.toBase64() = getEncoder().encodeToString(this.toByteArray())

fun String.fromBase64() = String(getDecoder().decode(this))