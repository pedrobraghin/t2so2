package com.br.src.logger;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.LocalDate;

public class Log {
  public static void saveLog(String message) {
    File log = new File(logName());
    LocalTime time = LocalTime.now();
    String formatedTime = time.toString();

    if (log.exists() == false) {
      try {
        log.createNewFile();
      } catch (IOException e) {
        System.err.println("Error creating log file: " + e.getMessage());
        System.err.println("Log name: " + logName());
      }
    }
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));
      String timeMessage = "# Time of occurence: " + formatedTime + "\n";
      writer.append(timeMessage);
      writer.append(message + "\n\n");
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static String logName() {
    LocalDate date = LocalDate.now();
    String logName = "logs/" + date.toString() + ".log";
    return logName;
  }
}
