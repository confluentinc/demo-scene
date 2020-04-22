package io.confluent.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.springframework.boot.Banner.Mode.OFF;
import static org.springframework.boot.WebApplicationType.SERVLET;

@SpringBootApplication
public class WebProducer {

  public static void main(String[] args) {
    new SpringApplicationBuilder(WebProducer.class)
        .web(SERVLET)
        .bannerMode(OFF)
        .run(args);
  }

}
