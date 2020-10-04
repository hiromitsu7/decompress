package com.github.hiromitsu7.decompress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    String fileName = args[0];
    decompress(fileName);
  }

  /**
   * 圧縮ファイルを展開する
   * 
   * @param fileName 展開するファイル名
   */
  private static void decompress(String fileName) {
    logger.info("fileName: {}", fileName);

    File f = new File(fileName);
    f = decompressGzipAndRename(f);

    try (FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bis);) {
      ArchiveEntry entry = null;
      while ((entry = input.getNextEntry()) != null) {
        decompressZipAndTar(fileName, input, entry);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    } catch (ArchiveException e) {
      // 通常のファイルの場合にもArchiveExceptionがスローされる.その場合は無視可能
      logger.debug("ArchiveException: {}", e.getMessage());
    }
  }

  /**
   * ZIPファイルとTARファイルを展開する
   * 
   * @param dirName
   * @param input
   * @param entry
   * @throws IOException
   */
  private static void decompressZipAndTar(String fileName, ArchiveInputStream input, ArchiveEntry entry)
      throws IOException {
    String dirName = fileName + "-decompress";
    File newFile = new File(dirName, entry.getName());
    if (entry.isDirectory()) {
      File dir = newFile;
      if (!dir.exists()) {
        dir.mkdirs();
      }
    } else {
      File dir = new File(newFile.getParent());
      if (!dir.exists()) {
        dir.mkdirs();
      }
      OutputStream output = new FileOutputStream(newFile);
      IOUtils.copy(input, output);
      output.close();
      decompress(newFile.getAbsolutePath());
    }
  }

  /**
   * GZIPファイルは展開し、展開後のファイル名を返却する. GZIPファイルを展開した場合、複数のファイルが含まれることはない
   * 
   * @param f
   * @return
   */
  private static File decompressGzipAndRename(File f) {
    if (isGZipped(f)) {
      File target = new File(f.getAbsoluteFile() + "-ungzip");
      decompressGzip(f, target);
      try {
        Files.delete(f.toPath());
        f = target;
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return f;
  }

  /**
   * GZIPファイルを展開する
   * 
   * @param source
   * @param target
   */
  private static void decompressGzip(File source, File target) {
    try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source));
        FileOutputStream fos = new FileOutputStream(target)) {
      byte[] buffer = new byte[1024];
      int len;
      while ((len = gis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * GZIPファイルかどうかを判定する
   * 
   * @param f
   * @return
   */
  private static boolean isGZipped(File f) {
    try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
      int magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
      return magic == GZIPInputStream.GZIP_MAGIC;
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
