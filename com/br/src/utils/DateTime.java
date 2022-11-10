package com.br.src.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTime {

    public static String getDateTime() {
        String dateTime;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        dateTime = "[" + dtf.format(now) + "] ";
        return dateTime;
    }

}
