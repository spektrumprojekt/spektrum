package de.spektrumprojekt.i.evaluation.recallstats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class DateFormatTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DateFormatTest.class);

    @Test
    public void test1000() {

        DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
        DateFormat TIME_FORMAT = new SimpleDateFormat("HH:ss:mm");

        Date date = new Date();

        LOGGER.info(DATE_FORMAT.format(date) + " " + TIME_FORMAT.format(date));

    }

}
