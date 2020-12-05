package io.confluent.developer.wordcount

import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class IQRestController {
  
  @Autowired
  private lateinit var iqService: InteractiveQueryService
  
  @GetMapping("/iq/count/{word}")
  fun getCount(@PathVariable word: String): Long {
    val store =
      iqService.getQueryableStore("word-count-state-store", QueryableStoreTypes.keyValueStore<String, Long>())
    return store[word]
  }
}
