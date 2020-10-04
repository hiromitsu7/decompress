package com.github.hiromitsu7.decompress;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainTest {

  @BeforeEach
  public void setUp() {
    deleteTempDir("src/test/resources/example.tar.gz-decompress");
    deleteTempDir("src/test/resources/example.zip-decompress");
  }

  @AfterEach
  public void tearDown() {
    deleteTempDir("src/test/resources/example.tar.gz-decompress");
    deleteTempDir("src/test/resources/example.zip-decompress");
  }

  @Test
  public void tar_gzを展開する() {
    String[] args = { "src/test/resources/example.tar.gz" };
    Main.main(args);
    assertTrue(new File("src/test/resources/example.tar.gz-decompress/example1/dir1/aaa").exists());
  }

  @Test
  public void zipを展開する() {
    String[] args = { "src/test/resources/example.zip" };
    Main.main(args);
    assertTrue(new File("src/test/resources/example.zip-decompress/example/example1/dir1/aaa").exists());
  }

  private void deleteTempDir(String filePath) {
    File f = new File(filePath);
    try {
      FileUtils.deleteDirectory(f);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
