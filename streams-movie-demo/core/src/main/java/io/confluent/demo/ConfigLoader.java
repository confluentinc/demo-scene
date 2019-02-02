package io.confluent.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

  public static Properties loadConfig(final String configFile) {
    if (!Files.exists(Paths.get(configFile))) {
      try {
        throw new IOException(configFile + " not found.");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    final Properties cfg = new Properties();
    try (InputStream inputStream = new FileInputStream(configFile)) {
      cfg.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return cfg;
  }

}
