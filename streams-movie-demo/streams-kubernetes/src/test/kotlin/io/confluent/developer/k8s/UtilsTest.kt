package io.confluent.developer.k8s

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
  @Test
  fun base64() {
    assertEquals("YWRtaW4=", "admin".toBase64())
  }

  @Test
  fun fromBase64() {
    assertEquals("admin", "YWRtaW4=".fromBase64())
  }
}
