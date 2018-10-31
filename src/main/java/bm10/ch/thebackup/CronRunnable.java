package bm10.ch.thebackup;


import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CronRunnable extends TimerTask {
    int second;
    int minute;
    int hour;
    int day;
    int month;
    int week;

    public CronRunnable(String expression) {
        String[] parts = expression.split(" ");
        if (parts.length == 6 && expression.matches("([0-9]{1,2} |\\* ){5}([0-9]{1,2}|\\*)")) {
            this.second = parts[0].equals("*") ? -1 : Integer.parseInt(parts[0]);
            this.minute = parts[1].equals("*") ? -1 : Integer.parseInt(parts[1]);
            this.hour = parts[2].equals("*") ? -1 : Integer.parseInt(parts[2]);
            this.day = parts[3].equals("*") ? -1 : Integer.parseInt(parts[3]);
            this.month = parts[4].equals("*") ? -1 : Integer.parseInt(parts[4]);
            this.week = parts[5].equals("*") ? -1 : Integer.parseInt(parts[5]);
            if (this.second != -1 && (this.second < 0 || this.second > 59)) {
                throw new IllegalArgumentException("Error in cron task format \"" + expression + "\": Second (1) must be between 0 and 59");
            } else if (this.minute != -1 && (this.minute < 0 || this.minute > 59)) {
                throw new IllegalArgumentException("Error in cron task format \"" + expression + "\": Minute (2) must be between 0 and 59");
            } else if (this.hour == -1 || this.hour >= 0 && this.hour <= 23) {
                if (this.day == -1 || this.day >= 1 && this.day <= 31) {
                    if (this.month != -1 && (this.month < 1 || this.month > 12)) {
                        throw new IllegalArgumentException("Error in cron task format \"" + expression + "\": Month (5) must be between 1 and 12");
                    } else if (this.week == -1 || this.week >= 1 && this.week <= 7) {
                        (new Timer()).scheduleAtFixedRate(this, 0L, 1000L);
                    } else {
                        throw new IllegalArgumentException("Error in cron task format \"" + expression + "\": Day of the Week (6) must be between 1 and 7");
                    }
                } else {
                    throw new IllegalArgumentException("Error in cron task format \"" + expression + "\": Day of the Month (4) must be between 1 and 31");
                }
            } else {
                throw new IllegalArgumentException("Error in cron task format \"" + expression + "\": Hour of the Day (3) must be between 0 and 23");
            }
        } else {
            throw new IllegalArgumentException("Invalid cron task format \"" + expression + "\"");
        }
    }

    public void run() {
        Calendar cal = Calendar.getInstance();
        if (this.second == -1 || this.second == cal.get(13)) {
            if (this.minute == -1 || this.minute == cal.get(12)) {
                if (this.hour == -1 || this.hour == cal.get(11)) {
                    if (this.day == -1 || this.day == cal.get(5)) {
                        if (this.month == -1 || this.month == cal.get(2)) {
                            if (this.week == -1 || this.week == cal.get(7)) {
                                this.exec();
                            }
                        }
                    }
                }
            }
        }
    }

    public abstract void exec();
}
