package io.confluent.demo;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringStreamsDemo {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(SpringStreamsDemo.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.run(args);

  }
}
