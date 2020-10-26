package io.confluent.developer.webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@SpringBootApplication
public class WebserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebserviceApplication.class, args);
  }

}

@RestController
class MyRestController {

  @GetMapping("/hello")
  public String helloWorld() {
    final Instant now = Instant.now();
    
    return "hello from " + now;
  }
}