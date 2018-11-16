package io.confluent.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RatingsRestController {

  private KafkaTemplate<Long, String> kafkaTemplate;

  @RequestMapping(path = "/rating", method = POST)
  public String rateMovie(@RequestParam Long movieId, @RequestParam Double rating) {
    Rating userRating = new Rating(movieId, rating);
    System.out.println("userRating = " + userRating.toString());
    kafkaTemplate.send("raw-ratings", userRating.getMovieId(), Parser.toRawRating(userRating));
    return "👍";

  }

  @Autowired
  public void setKafkaTemplate(KafkaTemplate<Long, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }
}
