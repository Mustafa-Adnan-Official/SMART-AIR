package com.example.smartair.utils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class SimpleDateFormat {
    public static String getCurrentDateId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(formatter);
    }

}
