package uk.gov.tomakehurst.wiremock.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class DeleteFileScheduler {

    public void deleteOldFiles() {

        Timer t = new Timer(true);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 01);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date d = cal.getTime();
        t.scheduleAtFixedRate(new DeleteFileTask(), d, 24 * 60 * 60 * 1000);
    }
}
