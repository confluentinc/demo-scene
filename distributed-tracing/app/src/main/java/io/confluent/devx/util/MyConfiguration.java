package io.confluent.devx.util;

import org.springframework.context.annotation.Configuration;

import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.opentracing.util.GlobalTracer;

@Configuration
public class MyConfiguration {

  public MyConfiguration() {
    GlobalTracer.registerIfAbsent(new io.jaegertracing.Configuration("Spring Boot")
                                      .withSampler(SamplerConfiguration.fromEnv().withType("const").withParam(1))
                                      .withReporter(ReporterConfiguration.fromEnv())
                                      .getTracer());

  }
}