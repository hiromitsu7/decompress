package com.github.hiromitsu7.decompress;

import org.junit.jupiter.api.Test;

class MainTest {

  @Test
  void test() {
    String[] args = { "src/test/resources/example.tar.gz" };
    Main.main(args);
  }

}
