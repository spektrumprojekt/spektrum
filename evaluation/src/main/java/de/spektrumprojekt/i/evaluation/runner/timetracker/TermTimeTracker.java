package de.spektrumprojekt.i.evaluation.runner.timetracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationException;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationExecutionProvider;
import de.spektrumprojekt.i.evaluation.runner.processor.SimpleEvaluationProcessor;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

public class TermTimeTracker extends SimpleEvaluationProcessor {

    public class RunResult {

        public Map<String, Integer> termToCountMap = new HashMap<String, Integer>();
        public List<Message> messages = new ArrayList<Message>();
    }

    private final List<TimeBinConfig> timeBinConfigs = new ArrayList<TimeBinConfig>();

    private final static Logger LOGGER = LoggerFactory.getLogger(TermTimeTracker.class);

    public static final int ORDER = 100000;

    public TermTimeTracker(EvaluationExecutionProvider evaluationProvider) {
        super(evaluationProvider, ORDER);

        timeBinConfigs.add(new TimeBinConfig(TimeBinMode.MONTH, TimeBinMode.DAY));
        timeBinConfigs.add(new TimeBinConfig(TimeBinMode.MONTH, TimeBinMode.WEEK));
        timeBinConfigs.add(new TimeBinConfig(TimeBinMode.MONTH, TimeBinMode.MONTH));

        timeBinConfigs.add(new TimeBinConfig(TimeBinMode.WEEK, TimeBinMode.DAY));
        timeBinConfigs.add(new TimeBinConfig(TimeBinMode.WEEK, TimeBinMode.WEEK));

        timeBinConfigs.add(new TimeBinConfig(TimeBinMode.DAY, TimeBinMode.DAY));
    }

    @Override
    public void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {

    }

    @Override
    public void afterSingleRun() throws EvaluationException {

        Persistence persistence = this.getEvaluationExecutionProvider().getPersistence();

        LOGGER.info("Running TermTimeTracker with {} configs. ", timeBinConfigs.size());
        for (TimeBinConfig timeBinConfig : this.timeBinConfigs) {

            MessageTimeBinCreator<BinTermStats> creator = new MessageTimeBinCreator<BinTermStats>(
                    timeBinConfig);
            creator.generate(persistence.getMessages(new MessageFilter()), false);

            RunResult last = null;
            Set<String> allTerms = new HashSet<String>();
            int count = creator.getBins().size();
            int index = 0;
            for (TimeBin<Message, BinTermStats> bin : creator.getBins()) {
                last = run(bin, last, allTerms);

                int percent = 100 * ++index / count;
                LOGGER.debug(percent + "% done ... (" + timeBinConfig.getTimeBinLength() + "-"
                        + timeBinConfig.getTimeBinIncrement()
                        + ")");
            }

            try {
                output(timeBinConfig, creator);

            } catch (IOException e) {
                throw new EvaluationException(e);
            }
        }
    }

    private BinTermStats computeStats(RunResult lastE, RunResult currentE, Set<String> allTerms) {
        Map<String, Integer> last = new HashMap<String, Integer>(lastE.termToCountMap);
        Map<String, Integer> current = new HashMap<String, Integer>(currentE.termToCountMap);

        BinTermStats stats = new BinTermStats();
        stats.messageCount = currentE.messages.size();

        stats.uniqueTermCount = current.size();
        int count = 0;
        for (Integer v : current.values()) {
            count += v.intValue();
        }
        stats.termCount = count;

        for (String c : current.keySet()) {
            if (last.containsKey(c)) {
                stats.matchingTermCount++;
                last.remove(c);
            } else {
                stats.newTermCount++;
            }
        }

        stats.removedTermCount = last.size();

        for (String c : current.keySet()) {
            if (allTerms.contains(c)) {
                stats.matchingAllTermCount++;
            } else {
                stats.reallyNewTermCount++;
            }
        }
        return stats;

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    private void output(TimeBinConfig config,
            MessageTimeBinCreator<BinTermStats> creator) throws IOException {

        BinTermStatsOutput output = new BinTermStatsOutput();

        output.getDescriptions().add("TimeBinConfig: " + config);
        output.getDescriptions().add("Increment: " + config.getTimeBinIncrement());
        output.getDescriptions().add("Length: " + config.getTimeBinLength());

        for (TimeBin<Message, BinTermStats> bin : creator.getBins()) {

            output.getDescriptions().add(
                    bin.getIndex() + " " + bin.getTag() + " '" + bin.getStart() + "' '"
                            + bin.getEnd() + "'\n");

            if (bin.getTag() != null) {
                output.getElements().add(bin.getTag());
            }
        }

        String filename = this.getEvaluationExecutionProvider().getEvaluatorResultFilename()
                + "-" + config.getTimeBinLength().name()
                + "-" + config.getTimeBinIncrement().name()
                + ".ta";
        output.write(filename);
        LOGGER.info("Wrote {} term analysis to {} ", output.getElements().size(), filename);

    }

    private RunResult run(TimeBin<Message, BinTermStats> bin, RunResult last, Set<String> allTerms) {

        RunResult current = new RunResult();

        current.messages.addAll(bin.getItems());

        for (Message message : current.messages) {
            for (Term t : MessageHelper.getAllTerms(message)) {
                Integer count = current.termToCountMap.get(t.getValue());
                if (count == null) {
                    count = 0;
                }
                count++;
                current.termToCountMap.put(t.getValue(), count);
            }
        }

        if (last != null) {
            bin.setTag(computeStats(last, current, allTerms));
        }
        last = current;
        allTerms.addAll(current.termToCountMap.keySet());
        LOGGER.debug("allTerms size=" + allTerms.size());
        return last;
    }
}
