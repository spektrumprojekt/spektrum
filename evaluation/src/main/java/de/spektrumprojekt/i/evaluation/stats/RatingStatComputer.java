package de.spektrumprojekt.i.evaluation.stats;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.i.evaluation.MessageDataSetProvider;
import de.spektrumprojekt.i.evaluation.configuration.Configuration;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRatingCreationDateComparator;
import de.spektrumprojekt.i.evaluation.measure.EvaluatorDataPoint;
import de.spektrumprojekt.i.evaluation.measure.EvaluatorOutput;

public class RatingStatComputer implements Computer {

    public class SpektrumRatingWithInteractionLevel extends SpektrumRating {

        private final InteractionLevel interactionLevel;

        public SpektrumRatingWithInteractionLevel(SpektrumRating clone,
                InteractionLevel interactionLevel) {
            super(clone);
            this.interactionLevel = interactionLevel;
        }

        public InteractionLevel getInteractionLevel() {
            return interactionLevel;
        }

    }

    private final static org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(RatingStatComputer.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(MessageDataSetProvider provider, String relativeFileDir)
            throws Exception {
        try {
            String outFileDir = Configuration.INSTANCE.getEvaluationResultsDir()
                    + File.separator + relativeFileDir;
            RatingStatComputer stat = new RatingStatComputer(provider);
            stat.run();
            // out
            stat.out(outFileDir);
        } finally {
            if (provider != null) {
                provider.close();
            }
        }

    }

    private final String evalFilenameWithIL;
    public Map<Long, UserTimeRatingStat> dayStats = new HashMap<Long, UserTimeRatingStat>();

    private Map<String, UserRatingStat> userStats = new HashMap<String, UserRatingStat>();

    private Calendar startDate;
    private long startDay;

    private long endDay;
    private long startMonth;

    private long endMonth;
    private List<UserRatingStat> sortedUserRatingStats;

    private List<UserTimeRatingStat> sortedDayRatingStats;

    private final MessageDataSetProvider dataSetProvider;

    private boolean commentHeader = false;
    private String headerNameSep = "-";

    public RatingStatComputer(MessageDataSetProvider dataSetProvider)
            throws Exception {

        dataSetProvider.init();

        this.dataSetProvider = dataSetProvider;
        this.evalFilenameWithIL = Configuration.INSTANCE.getEvaluationDirectoryFull()
                + File.separator
                + Configuration.INSTANCE.getProperties().getProperty(
                        "evaluation.rating.eval.result.with.all.ratings");
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    private UserTimeRatingStat getDayStat(Date publicationDate, long day) {
        UserTimeRatingStat stat = dayStats.get(day);
        if (stat == null) {
            stat = new UserTimeRatingStat();
            stat.dateField = day;
            stat.date = publicationDate;
            dayStats.put(day, stat);
        }
        return stat;
    }

    private UserRatingStat getStat(String userId) {
        UserRatingStat stat = userStats.get(userId);
        if (stat == null) {
            stat = new UserRatingStat();
            stat.userId = userId;
            userStats.put(userId, stat);
        }
        return stat;
    }

    public void out(String ratingOutBaseFilename) throws IOException {
        String fnameUser = ratingOutBaseFilename + "-overall.ratingstats";
        String fnameDay = ratingOutBaseFilename + "-daily.ratingstats";
        String fnameUserPerMonth = ratingOutBaseFilename + "-userPerMonth.ratingstats";

        outMonthStats(fnameUser);
        outDayStats(fnameDay);
        outUserPerMonthStats(fnameUserPerMonth);

    }

    private void outDayStats(String fnameDay) throws IOException {
        List<String> lines;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        lines = new ArrayList<String>();
        IntegerWrapper headerCount = new IntegerWrapper();
        headerCount.value = 1;
        String header = commentHeader ? "# " : "";
        header = header
                + headerCount.value++ + "_day "
                + headerCount.value++ + "_dayStr "
                + RatingCount.toHeaderString("", headerCount);
        header = header.replace("_", this.headerNameSep);

        lines.add(header);
        lines.add("# StartDate: " + format.format(this.startDate.getTime()));
        for (UserTimeRatingStat stat : sortedDayRatingStats) {
            lines.add(stat.dateField + " " + format.format(stat.date) + " "
                    + stat.ratingCount.toString());
        }

        FileUtils.writeLines(new File(fnameDay), lines);
        LOGGER.info("Wrote day rating statistics to {}.", fnameDay);
    }

    private void outMonthStats(String fnameUser) throws IOException {
        List<String> lines = new ArrayList<String>();

        String header = commentHeader ? "#" : "";
        header = header + UserRatingStat.toHeaderString(startMonth, endMonth);
        header = header.replace("_", this.headerNameSep);
        lines.add(header);
        lines.add("# StartDate: " + this.startDate.getTime());
        for (UserRatingStat stat : sortedUserRatingStats) {
            lines.add(stat.toString(startMonth, endMonth));
        }

        FileUtils.writeLines(new File(fnameUser), lines);
        LOGGER.info("Wrote user rating statistics to {}.", fnameUser);
    }

    /**
     * Out the positive, negative rating per month with the user being columns
     * 
     * @param fnameUser
     * @throws IOException
     */
    private void outUserPerMonthStats(String fnameUser) throws IOException {
        List<String> lines = new ArrayList<String>();

        lines.add("# StartDate: " + this.startDate.getTime());

        SimpleDateFormat format = new SimpleDateFormat("MMM-yyyy");

        IntegerWrapper col = new IntegerWrapper();
        col.value = 1;

        StringBuilder header = new StringBuilder();
        header.append(commentHeader ? "#" : "");
        header.append(col.value++ + "_monthId ");
        header.append(col.value++ + "_monthName ");

        for (UserRatingStat stat : sortedUserRatingStats) {
            header.append(col.value++ + "_" + stat.userId + "_positive ");
            header.append(col.value++ + "_" + stat.userId + "_normal ");
            header.append(col.value++ + "_" + stat.userId + "_negative ");
        }
        String headerS = header.toString().replace("_", this.headerNameSep);
        lines.add(headerS);
        lines.add("# Run time: " + new Date());

        for (long i = startMonth; i <= endMonth; i++) {
            int currentMonth = (int) (i - startMonth);

            StringBuilder line = new StringBuilder();
            line.append(currentMonth + " ");

            Calendar cal = new GregorianCalendar();
            cal.setTime(this.startDate.getTime());
            cal.add(Calendar.MONTH, currentMonth);

            line.append(format.format(cal.getTime()) + " ");

            for (UserRatingStat stat : sortedUserRatingStats) {
                UserTimeRatingStat ts = stat.getMonthStat(currentMonth);
                line.append(ts.ratingCount.getSingleCount4All().pos + " ");
                line.append(ts.ratingCount.getSingleCount4All().normal + " ");
                line.append(ts.ratingCount.getSingleCount4All().neg + " ");
            }
            lines.add(line.toString());
        }

        FileUtils.writeLines(new File(fnameUser), lines);
        LOGGER.info("Wrote user per month rating statistics to {}.", fnameUser);
    }

    @Override
    public void run() throws IOException {

        LOGGER.info("Starting computing rating statistics ...");

        List<SpektrumRating> ratings = dataSetProvider.getRatings();

        EvaluatorOutput evaluatorOutput = new EvaluatorOutput();
        evaluatorOutput.read(this.evalFilenameWithIL);
        LOGGER.info("Read {} evaluator datapoints.", evaluatorOutput.getOverallItems());

        Map<String, EvaluatorDataPoint> dataPoints = evaluatorOutput
                .createIdentifierToDataPointMap();

        List<SpektrumRatingWithInteractionLevel> ratingsWithILs = new ArrayList<RatingStatComputer.SpektrumRatingWithInteractionLevel>(
                ratings.size());
        for (SpektrumRating rating : ratings) {

            EvaluatorDataPoint dataPoint = dataPoints.get(rating.getIdentifier());
            if (dataPoint == null) {
                String msg = "No evaluator datapoint for " + rating.getIdentifier()
                        + " Does the input file contains all values?";
                LOGGER.error(msg);
                // throw new RuntimeException(msg);
                continue;
            }
            if (dataPoint.getFeatures().getInteractionLevel() == null) {
                LOGGER.error("No interaction level for " + dataPoint + " " + rating.getIdentifier());
                continue;
            }

            SpektrumRatingWithInteractionLevel ratingWithIL = new SpektrumRatingWithInteractionLevel(
                    rating, dataPoint.getFeatures().getInteractionLevel());
            ratingsWithILs.add(ratingWithIL);
        }

        Collections.sort(ratingsWithILs, new SpektrumRatingCreationDateComparator());

        startDate = null;
        startDay = 0;
        startMonth = 0;
        endMonth = 0;
        for (SpektrumRatingWithInteractionLevel rating : ratingsWithILs) {
            Date publicationDate = rating.getMessage().getPublicationDate();

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(publicationDate);
            long currenMonth = (long) cal.get(Calendar.MONTH) + 12 * cal.get(Calendar.YEAR);
            long currentDay = (long) cal.get(Calendar.DAY_OF_YEAR) + 365 * cal.get(Calendar.YEAR);
            if (startDate == null) {
                startDate = cal;
                startDay = currentDay;
                startMonth = currenMonth;
                endDay = currentDay;
                endMonth = currenMonth;
            }
            endMonth = Math.max(endMonth, currenMonth);
            endDay = Math.max(endDay, currentDay);
            UserRatingStat stat = getStat(rating.getUserGlobalId());
            UserTimeRatingStat dayStat = getDayStat(publicationDate, currentDay - startDay);

            stat.integrate(rating.getInterest(), rating.getInteractionLevel(), currenMonth
                    - startMonth);
            dayStat.ratingCount.integrate(rating.getInterest(), rating.getInteractionLevel());

        }

        sortAndUpdateUsers();
        sortAndUpdateDays();

        LOGGER.info("Finished computing rating statistics for {} users.", this.userStats.size());

        int messagesWithImage = 0;
        int messagesWithAttachment = 0;
        int images = 0;
        int all = 0;

        Iterator<Message> messages = dataSetProvider.getMessageIterator();
        while (messages.hasNext()) {
            Message message = messages.next();

            boolean hasAttach = false, hasImage = false;
            for (MessagePart messagePart : message.getMessageParts()) {
                if (messagePart.isImageAttachment()) {
                    images++;
                    hasImage = true;
                }
                if (messagePart.isAttachment()) {
                    hasAttach = true;
                    all++;
                }
            }
            if (hasAttach) {
                messagesWithAttachment++;
            }
            if (hasImage) {
                messagesWithImage++;
            }
        }

        LOGGER.info(
                "Attachments: Overall={} Messages With Attachment={} Images={} Images With Attachment={}",
                all, images, messagesWithAttachment, messagesWithImage);
    }

    private void sortAndUpdateDays() {
        sortedDayRatingStats = new ArrayList<UserTimeRatingStat>(
                this.dayStats.values());

        Collections.sort(sortedDayRatingStats, new Comparator<UserTimeRatingStat>() {

            @Override
            public int compare(UserTimeRatingStat o1, UserTimeRatingStat o2) {
                long diff = o1.dateField - o2.dateField;
                if (diff == 0) {
                    return 0;
                }
                if (diff > 0) {
                    return 1;
                }
                return -1;
            }
        });

    }

    private void sortAndUpdateUsers() {
        sortedUserRatingStats = new ArrayList<UserRatingStat>(
                this.userStats.values());

        Collections.sort(sortedUserRatingStats, new Comparator<UserRatingStat>() {

            @Override
            public int compare(UserRatingStat o1, UserRatingStat o2) {
                return -(o1.ratingCount.overall() - o2.ratingCount.overall());
            }
        });

        char userId = 'A';
        for (UserRatingStat stat : sortedUserRatingStats) {
            stat.userId = "" + userId++;
        }
    }
}
