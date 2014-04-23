package de.spektrumprojekt.i.evaluation.measure.top;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.i.evaluation.measure.EvaluatorDataPoint;
import de.spektrumprojekt.i.evaluation.measure.EvaluatorOutput;
import de.spektrumprojekt.i.evaluation.rank.InternalMessageRank;
import de.spektrumprojekt.i.evaluation.rank.InternalMessageRankComparator;
import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluatorConfiguration;
import de.spektrumprojekt.i.evaluation.stats.IntegerWrapper;
import de.spektrumprojekt.persistence.simple.UserMessageIdentifier;

public class TopRankMessageComputer implements Computer {

    private final static Logger LOGGER = LoggerFactory.getLogger(TopRankMessageComputer.class);

    public static int compare(Date cal1, Date cal2, int field) {
        switch (field) {
        case Calendar.DAY_OF_YEAR:
            if (DateUtils.isSameDay(cal1, cal2)) {
                return 0;
            }
            break;
        case Calendar.WEEK_OF_YEAR:
            long week1 = cal1.getTime() / DateUtils.MILLIS_PER_DAY / 7;
            long week2 = cal2.getTime() / DateUtils.MILLIS_PER_DAY / 7;
            if (week1 == week2) {
                return 0;
            }
            break;
        case Calendar.MONTH:
            long m1 = cal1.getTime() / DateUtils.MILLIS_PER_DAY / 28;
            long m2 = cal2.getTime() / DateUtils.MILLIS_PER_DAY / 28;
            if (m1 == m2) {
                return 0;
            }
            break;
        default:
            throw new UnsupportedOperationException(field + " not implemented.");
        }
        if (cal1.getTime() - cal2.getTime() > 0) {
            return 1;
        }
        return -1;
    }

    public static long getAbsoluteFieldValue(Date date, int field) {
        switch (field) {
        case Calendar.DAY_OF_YEAR:
            return date.getTime() / DateUtils.MILLIS_PER_DAY;
        case Calendar.WEEK_OF_YEAR:
            return date.getTime() / DateUtils.MILLIS_PER_DAY / 7;
        case Calendar.MONTH:
            return date.getTime() / DateUtils.MILLIS_PER_DAY / 28;
        default:
            throw new UnsupportedOperationException(field + " not implemented.");
        }
    }

    static String getNameForCalendarField(int field) {
        switch (field) {
        case Calendar.DAY_OF_YEAR:
            return "Day";
        case Calendar.WEEK_OF_YEAR:
            return "Week";
        case Calendar.MONTH:
            return "Month";

        default:
            throw new UnsupportedOperationException(field + " not implemented.");
        }
    }

    private StatsPerUserTimeBin statsPerUserTimeBin;

    private Map<String, List<InternalMessageRank>> userToRank = new HashMap<String, List<InternalMessageRank>>();
    private String basicFilename;

    private File allMessageRanksFile;

    // list of the ".eval" files that can be used for the
    private final Collection<String> evaluatorOutputFilenames = new HashSet<String>();

    private final List<String> commentsOfRanking = new ArrayList<String>();

    private EvaluatorOutput evaluatorOutput;

    private Map<UserMessageIdentifier, EvaluatorDataPoint> dataPoints;
    private Date firstPublicationDate;

    private final EvaluatorConfiguration evaluatorConfiguration;

    private List<TopRankDefinition> topRankDefinitions = new ArrayList<TopRankDefinition>();

    private final boolean cutoffRanksWithSameRelevanceScore;

    private boolean writeRanksToFile;

    public TopRankMessageComputer(EvaluatorConfiguration evaluatorConfiguration,
            File allMessageRanksFile, String evaluatorOutputFileName)
            throws IOException {
        this.evaluatorConfiguration = evaluatorConfiguration;
        this.allMessageRanksFile = allMessageRanksFile;
        this.basicFilename = evaluatorOutputFileName;

        this.evaluatorOutputFilenames.add(evaluatorOutputFileName);
        this.cutoffRanksWithSameRelevanceScore = evaluatorConfiguration
                .isCutoffRanksWithSameRelevanceScore();

        this.setupDefinitions();

    }

    private void computeDateOrder(int calenderField) {
        // if cutoffRanksWithSameRelevanceScore is true force the comparator to not compare to equal
        // (0). instead it wil use the message id to sort. this way a sorted list will always be in
        // order for multiple runs. otherwise random behavior may occur.
        Comparator<InternalMessageRank> comp = new InternalMessageRankComparator(calenderField,
                cutoffRanksWithSameRelevanceScore);

        for (List<InternalMessageRank> ranks : this.userToRank.values()) {
            Collections.sort(ranks, comp);
            int order = 0;
            InternalMessageRank last = null;

            for (InternalMessageRank rank : ranks) {
                if (last != null
                        && compare(rank.getPublicationDate(), last.getPublicationDate(),
                                calenderField) != 0) {
                    // new time bin (day/week)
                    order = 0;
                    last = null;
                }

                // if the last score has the same score as the current, and we do not want to cut
                // off ranks with same relevance take the order of the last one
                if (last != null && !cutoffRanksWithSameRelevanceScore
                        && rank.getScore() == last.getScore()) {
                    rank.setOrderRank(last.getOrderRank());
                } else {
                    rank.setOrderRank(order);
                }
                if (last != null && last.getScore() < rank.getScore()) {
                    throw new RuntimeException(
                            "The last rank cannot have a lower score as the current! last=" + last
                                    + " rank=" + rank);
                }
                order++;
                last = rank;
            }
        }

    }

    private int determineTimeBin(Date date, int calendarField, long firstTimeBin) {
        long fieldValue = getAbsoluteFieldValue(date, calendarField);
        return (int) (fieldValue - firstTimeBin);
    }

    private void filterAndStoreOrderedRanksAndDataPoints(TopRankDefinition topRankDefinition,
            long firstTimeBin)
            throws IOException {
        EvaluatorOutput filteredEvaluatorOutput = new EvaluatorOutput();
        filteredEvaluatorOutput.getDescriptions().addAll(commentsOfRanking);

        // actually this is just a debugging purpose
        List<String> lines = new ArrayList<String>();
        for (String comment : commentsOfRanking) {
            lines.add("#" + comment);
        }

        for (List<InternalMessageRank> ranks : this.userToRank.values()) {

            for (InternalMessageRank rank : ranks) {

                EvaluatorDataPoint dp = this.dataPoints.get(new UserMessageIdentifier(rank
                        .getUserId(),
                        rank.getMessageId()));
                if (dp != null) {
                    // TODO we could do this in a separate method
                    dp.setTimeBin(determineTimeBin(rank.getPublicationDate(),
                            topRankDefinition.getCalendarField(),
                            firstTimeBin));

                }

                if (rank.getOrderRank() <= topRankDefinition.getTopN()) {

                    if (dp != null) {
                        filteredEvaluatorOutput.getElements().add(dp);
                    }
                    lines.add(rank.toParseableString());

                }
            }

        }

        double letTheComputedBePositive = 0.9d;
        Map<String, IntegerWrapper> relevantCount = new HashMap<String, IntegerWrapper>();
        for (EvaluatorDataPoint dataPoint : this.dataPoints.values()) {
            if (dataPoint.getTarget() > letTheComputedBePositive) {
                String key = dataPoint.getTimeBinUserKey();
                IntegerWrapper iw = relevantCount.get(key);
                if (iw == null) {
                    iw = new IntegerWrapper();
                    relevantCount.put(key, iw);
                }
                iw.value++;
            }
        }
        filteredEvaluatorOutput.addTimebinRelevantCounts(relevantCount);

        int skippedValues = getNumberOfFilteredRanks()
                - filteredEvaluatorOutput.getElements().size();

        filteredEvaluatorOutput.getDescriptions().add(
                "userToRank.values.size: " + this.userToRank.values().size());

        filteredEvaluatorOutput.getDescriptions().add(
                "getNumberOfFilteredRanks(): " + getNumberOfFilteredRanks());

        filteredEvaluatorOutput.getDescriptions().add(
                "filteredEvaluatorOutput.dataPoints.size: "
                        + filteredEvaluatorOutput.getElements().size());
        filteredEvaluatorOutput.getDescriptions().add("skippedValues: " + skippedValues);

        String ranksOutputFilename = topRankDefinition.getRanksFilename(this.basicFilename);
        String evalOutputFilename = topRankDefinition.getEvalsFilename(this.basicFilename);

        filteredEvaluatorOutput.write(evalOutputFilename);

        // actually this is just a debugging purpose
        if (writeRanksToFile) {
            LOGGER.debug("Write ranks to " + ranksOutputFilename + " ...");

            OutputStream os = null;
            os = new BufferedOutputStream(new FileOutputStream(new File(ranksOutputFilename)),
                    100 * 1024 * 1024);

            IOUtils.writeLines(lines, null, os, (Charset) null);
            IOUtils.closeQuietly(os);

            LOGGER.debug("Wrote ranks to " + ranksOutputFilename + ".");
        }
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " cutoffRanksWithSameRelevanceScore: " + this.cutoffRanksWithSameRelevanceScore
                + " isTopRankMeasureOnlyUseDataPointsAvailableForEvaluation:"
                + this.evaluatorConfiguration
                        .isTopRankMeasureOnlyUseDataPointsAvailableForEvaluation();
    }

    public Collection<String> getEvaluatorOutputFilenames() {
        return this.evaluatorOutputFilenames;
    }

    public int getNumberOfFilteredRanks() {
        int sum = 0;
        for (List<InternalMessageRank> ranks : userToRank.values()) {
            sum += ranks.size();
        }
        return sum;
    }

    private void initRanksFromEvaluationOutputOnly() throws IOException {

        this.commentsOfRanking.clear();
        this.commentsOfRanking.addAll(this.evaluatorOutput.getDescriptions());

        for (EvaluatorDataPoint dataPoint : this.evaluatorOutput.getElements()) {
            InternalMessageRank rank = new InternalMessageRank(dataPoint);

            // ignore author
            if (!evaluatorConfiguration.isValidMessageRankInteractionLevel(rank
                    .getInteractionLevel())) {
                LOGGER.debug("Skip rating since its not matchig the interaction levels. line="
                        + dataPoint);
                continue;
            }
            List<InternalMessageRank> userRanks = userToRank.get(rank.getUserId());
            if (userRanks == null) {
                userRanks = new ArrayList<InternalMessageRank>();
                this.userToRank.put(rank.getUserId(), userRanks);
            }
            userRanks.add(rank);
        }

    }

    public boolean isWriteRanksToFile() {
        return writeRanksToFile;
    }

    private void readRanks() throws IOException {
        FileReader fileReader = null;
        try {

            this.commentsOfRanking.clear();
            this.commentsOfRanking.addAll(this.evaluatorOutput.getDescriptions());

            fileReader = new FileReader(allMessageRanksFile);
            List<String> lines = IOUtils.readLines(fileReader);

            for (String line : lines) {
                // skip comments
                if (line.trim().startsWith("#")) {
                    continue;
                }
                InternalMessageRank rank = new InternalMessageRank(line);

                // ignore author
                if (!evaluatorConfiguration.isValidMessageRankInteractionLevel(rank
                        .getInteractionLevel())) {
                    LOGGER.debug("Skip rating since its not matchig the interaction levels. line="
                            + line);
                    continue;
                }
                List<InternalMessageRank> userRanks = userToRank.get(rank.getUserId());
                if (userRanks == null) {
                    userRanks = new ArrayList<InternalMessageRank>();
                    this.userToRank.put(rank.getUserId(), userRanks);
                }
                userRanks.add(rank);
            }
        } finally {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private void reset() throws IOException {
        this.userToRank.clear();
        this.statsPerUserTimeBin = null;
    }

    public void run() throws Exception {

        try {
            // read the evaluation data points, that is the tupels of message id, user id, computed
            // and
            // target score
            this.evaluatorOutput = new EvaluatorOutput();
            this.evaluatorOutput.read(basicFilename);

            dataPoints = new HashMap<UserMessageIdentifier, EvaluatorDataPoint>();
            for (EvaluatorDataPoint dp : this.evaluatorOutput.getElements()) {
                this.dataPoints.put(
                        new UserMessageIdentifier(dp.getUserGlobalId(), dp.getMessageGlobalId()),
                        dp);
            }

            for (TopRankDefinition definition : this.topRankDefinitions) {
                run(definition);
            }
        } catch (FileNotFoundException fe) {
            LOGGER.error("Could not compute top ranks, file not found: " + fe.getMessage(), fe);
        }
    }

    private synchronized void run(TopRankDefinition definition)
            throws IOException {
        LOGGER.debug("Starting TopRanks on " + definition.getEvalsFilename(this.basicFilename));

        this.reset();

        if (this.evaluatorConfiguration.isTopRankMeasureOnlyUseDataPointsAvailableForEvaluation()) {
            this.initRanksFromEvaluationOutputOnly();
        } else {
            // read all computed scores
            this.readRanks();
        }

        // configuration comment
        this.commentsOfRanking.add("# topCount: " + definition.getTopN() + " topPerCalendarField: "
                + definition.getCalendarField() + " ("
                + getNameForCalendarField(definition.getCalendarField()) + ")");

        this.computeDateOrder(definition.getCalendarField());

        for (EvaluatorDataPoint dataPoint : this.dataPoints.values()) {
            Date date = dataPoint.getPublicationDateAsDate();
            if (firstPublicationDate == null
                    || date.before(firstPublicationDate)) {
                firstPublicationDate = date;
            }
        }

        long firstTimeBin = firstPublicationDate == null ? 0 : getAbsoluteFieldValue(
                firstPublicationDate, definition.getCalendarField());

        for (EvaluatorDataPoint dataPoint : this.dataPoints.values()) {
            dataPoint.setTimeBin(determineTimeBin(
                    dataPoint.getPublicationDateAsDate(),
                    definition.getCalendarField(),
                    firstTimeBin));

        }
        statsPerUserTimeBin = new StatsPerUserTimeBin();
        statsPerUserTimeBin.compute(this.dataPoints.values());

        this.commentsOfRanking.add("# statsPerUserTimeBin: " + statsPerUserTimeBin.toJson());

        this.filterAndStoreOrderedRanksAndDataPoints(definition, firstTimeBin);

        LOGGER.info("Wrote TopRanks to " + definition.getEvalsFilename(this.basicFilename));
    }

    private void setupDefinitions() {
        this.topRankDefinitions.add(new TopRankDefinition(5, Calendar.DAY_OF_YEAR));
        this.topRankDefinitions.add(new TopRankDefinition(10, Calendar.DAY_OF_YEAR));
        this.topRankDefinitions.add(new TopRankDefinition(20, Calendar.DAY_OF_YEAR));
        this.topRankDefinitions.add(new TopRankDefinition(10, Calendar.WEEK_OF_YEAR));
        this.topRankDefinitions.add(new TopRankDefinition(20, Calendar.WEEK_OF_YEAR));

        for (TopRankDefinition definition : this.topRankDefinitions) {
            this.evaluatorOutputFilenames.add(definition.getEvalsFilename(this.basicFilename));
        }
    }

    public void setWriteRanksToFile(boolean writeRanksToFile) {
        this.writeRanksToFile = writeRanksToFile;
    }
}
