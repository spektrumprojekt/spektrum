package de.spektrumprojekt.i.evaluation.runner.timetracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
import de.spektrumprojekt.i.evaluation.twitter.umap2011.TwitterMessageDatasetProvider;

public class MessageTimeBinCreator<Stats extends Object> {

    private final static Logger LOGGER = Logger.getLogger(MessageTimeBinCreator.class);

    public static void main(String[] args) throws Exception {
        TwitterMessageDatasetProvider twitterMessageDatasetProvider = null;
        try {
            LOGGER.info("TimeBinCreator ...");

            twitterMessageDatasetProvider = new TwitterMessageDatasetProvider();
            twitterMessageDatasetProvider.init();

            Collection<Message> mes = twitterMessageDatasetProvider.getMessages();

            LOGGER.info("messages for " + mes.size());

            StringBuilder sb = new StringBuilder();

            MessageTimeBinCreator<?> generator = new MessageTimeBinCreator<Object>(
                    new TimeBinConfig(TimeBinMode.MONTH, TimeBinMode.DAY));
            generator.generate(new ArrayList<Message>(mes), true);
            generator.dump(sb);

            LOGGER.debug("\n" + sb.toString());
            LOGGER.info("TimeBinCreator sucess.");
        } finally {
            twitterMessageDatasetProvider.close();
            LOGGER.info("TimeBinCreator end.");
        }
    }

    private Date firstOfAll = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();

    private TimeBinConfig timeBinConfig;

    private List<TimeBin<Message, Stats>> bins = new ArrayList<TimeBin<Message, Stats>>();

    public MessageTimeBinCreator(TimeBinConfig timeBinConfig) {
        this.timeBinConfig = timeBinConfig;
    }

    public Date computeNewTimeBinDate(Date date, TimeBinMode mode, boolean add) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        int field;
        int value;
        switch (mode) {
        case DAY:
            field = Calendar.DAY_OF_YEAR;
            value = 1;
            break;
        case WEEK:
            field = Calendar.DAY_OF_YEAR;
            value = 7;
            break;
        case MONTH:
            field = Calendar.MONTH;
            value = 1;
            break;
        case QUARTAL:
            field = Calendar.MONTH;
            value = 3;
            break;
        case HALFYEAR:
            field = Calendar.MONTH;
            value = 6;
            break;
        case YEAR:
            field = Calendar.YEAR;
            value = 1;
            break;
        default:
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        if (!add) {
            value = -value;
        }
        cal.add(field, value);
        return cal.getTime();
    }

    public TimeBin<Message, Stats> determineFirstBinDate(Date first) {
        TimeBin<Message, Stats> bin = new TimeBin<Message, Stats>();
        bin.setStart(computeNewTimeBinDate(firstOfAll, timeBinConfig.getTimeBinLength(), false));
        bin.setEnd(firstOfAll);

        while (true) {
            bin = getNextBin(bin);
            if (bin.matches(first)) {
                return bin;
            }
        }
    }

    private void distributeMessages(List<Message> messages) {
        for (TimeBin<Message, Stats> currentBin : this.bins) {
            for (Message mes : messages) {
                if (currentBin.matches(mes.getPublicationDate())) {
                    currentBin.getItems().add(mes);
                }
            }
        }
    }

    public void dump(StringBuilder sb) {
        for (TimeBin<Message, Stats> bin : bins) {
            sb.append(bin + " " + bin.getItems().size() + "\n");
        }
    }

    public void generate(List<Message> messages, boolean sort) {
        if (sort) {
            Collections.sort(messages, MessagePublicationDateComperator.INSTANCE);
        }
        TimeBin<Message, Stats> firstBin = determineFirstBinDate(messages.get(0)
                .getPublicationDate());
        firstBin.setIndex(0);
        generateBins(firstBin, messages.get(messages.size() - 1).getPublicationDate());

        distributeMessages(messages);
    }

    private void generateBins(TimeBin<Message, Stats> firstBin, Date lastDate) {
        TimeBin<Message, Stats> currentBin = firstBin;
        while (!currentBin.matches(lastDate)) {
            currentBin = getNextBin(currentBin);
            this.bins.add(currentBin);
        }
    }

    public List<TimeBin<Message, Stats>> getBins() {
        return bins;
    }

    public TimeBin<Message, Stats> getNextBin(TimeBin<Message, Stats> bin) {
        TimeBin<Message, Stats> next = new TimeBin<Message, Stats>();
        next.setIndex(bin.getIndex() + 1);

        next.setEnd(computeNewTimeBinDate(bin.getEnd(), timeBinConfig.getTimeBinIncrement(), true));
        next.setStart(computeNewTimeBinDate(bin.getStart(), timeBinConfig.getTimeBinIncrement(),
                true));
        return next;
    }
}
