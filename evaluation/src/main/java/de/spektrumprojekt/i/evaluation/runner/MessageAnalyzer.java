package de.spektrumprojekt.i.evaluation.runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;

public class MessageAnalyzer implements ConfigurationDescriptable {

    private final float letTheGoalBePositive;
    private final float letTheComputedBePositive;
    private final Date onlyAnalyzeAfter;

    private OutputStreamWriter writer;

    private final String name;

    private final String basicResultFileName;
    private boolean initalized;
    private final TermFrequencyComputer termFrequencyComputer;
    private final TermWeightComputer termWeightComputer;

    public MessageAnalyzer(TermWeightComputer termWeightComputer,
            TermFrequencyComputer termFrequencyComputer, String name,
            String basicResultFileName, float letTheGoalBePositive,
            float letTheComputedBePositive, Date onlyAnalyzeAfter) {
        if (termWeightComputer == null) {
            throw new IllegalArgumentException("termWeightComputer cannot be null!");
        }
        if (termFrequencyComputer == null) {
            throw new IllegalArgumentException("termFrequencyComputer cannot be null!");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null!");
        }
        if (basicResultFileName == null) {
            throw new IllegalArgumentException("basicResultFileName cannot be null!");
        }
        this.name = name;
        this.basicResultFileName = basicResultFileName;
        this.letTheGoalBePositive = letTheGoalBePositive;
        this.letTheComputedBePositive = letTheComputedBePositive;
        this.termFrequencyComputer = termFrequencyComputer;
        this.termWeightComputer = termWeightComputer;
        this.onlyAnalyzeAfter = onlyAnalyzeAfter;
    }

    public void analyze(UserMessageScore goal, UserSpecificMessageFeatureContext context) {
        checkInit();

        Message message = context.getMessage();
        if (onlyAnalyzeAfter != null && onlyAnalyzeAfter.after(message.getPublicationDate())) {
            return;
        }
        float computedRank = context.getMessageScore().getScore();
        MessageFeature mf = context.getFeature(Feature.CONTENT_MATCH_FEATURE);
        float cmfValue = 0;

        if (mf != null) {
            cmfValue = mf.getValue();
        }

        boolean predictionIsPositive = computedRank >= this.letTheComputedBePositive;
        boolean goalIsPositive = goal.getScore() >= this.letTheGoalBePositive;

        if (goalIsPositive && !predictionIsPositive) {

            String text = "";
            text += "author: " + message.getAuthorGlobalId() + "<br>";
            text += "messageId: " + message.getGlobalId() + "<br>";
            text += "date: " + message.getPublicationDate() + "<br>";
            for (MessagePart mp : message.getMessageParts()) {

                text += "<p>" + mp.getContent() + "</p>";
            }

            String summary = " ComputedRank: " + computedRank + " CMF (computed): " + cmfValue
                    + " Rating (goal): " + goal.getScore();
            String entries = "<br>" + context.getUserGlobalId();

            for (Entry<String, Map<Term, UserModelEntry>> userModelTypeEntries : context
                    .getMatchingUserModelEntries().entrySet()) {

                entries += "<br>User Model Type: " + userModelTypeEntries.getKey();
                for (Entry<Term, UserModelEntry> entry : userModelTypeEntries.getValue().entrySet()) {
                    float tw = termWeightComputer.determineTermWeight(
                            message.getMessageGroup() == null ? null : message.getMessageGroup()
                                    .getGlobalId(),
                            entry.getKey());
                    float overallTerms = termFrequencyComputer.getMessageCount(message
                            .getMessageGroup()
                            .getGlobalId());

                    entries += "<br>";
                    entries += "UME Score : " + entry.getKey().getValue() + " => ";
                    entries += entry.getValue().getScoredTerm().getWeight() + " c: "
                            + entry.getValue().getScoreSum()
                            + "s: " + entry.getValue().getScoreCount();
                    entries += " tw: " + tw;
                    entries += " tc: " + entry.getKey().getCount();
                    entries += " to: " + overallTerms;

                }
            }

            writeln("<tr><td>");
            writeln(text);
            writeln("</td><td>");
            writeln(summary);
            writeln(entries);
            writeln("</td>");
            writeln("</tr>");
        }
    }

    private void checkInit() {
        if (!initalized) {
            throw new java.lang.IllegalStateException("not yet initialized. call the init method!");
        }
    }

    public void close() throws IOException {
        checkInit();

        writeEnd();

        IOUtils.closeQuietly(writer);
    }

    /**
     * {@inheritDoc}
     */
    public String getConfigurationDescription() {
        return getClass().getSimpleName()
                + " letTheGoalBePositive: " + letTheGoalBePositive
                + " letTheComputedBePositive: " + letTheComputedBePositive
                + " onlyAnalyzeAfter: " + onlyAnalyzeAfter;
    }

    public Date getOnlyAnalyzeAfter() {
        return onlyAnalyzeAfter;
    }

    public void initWriteToFile() throws IOException {
        writer = new FileWriter(new File(basicResultFileName
                + "-messages.html"));
        writeStartup();
        initalized = true;
    }

    private void write(String s) {
        try {
            this.writer.write(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEnd() {
        writeln("</table></body></html>");
    }

    private void writeln(String s) {
        write(s + "\n");
    }

    private void writeStartup() {
        writeln("<html><body>");
        writeln("<h1>" + name + "</h1>");
        writeln("<p>" + this.getConfigurationDescription() + "</p>");
        writeln("<table border=\"1\">");
        writeln("<colgroup>");
        writeln("  <col width=\"45%\">");
        writeln("  <col width=\"45%\">");
        writeln("</colgroup>");
        writeln("<tr>");
        writeln("<th>");
        writeln("Messages");
        writeln("</th>");
        writeln("<th>");
        writeln("tc = Term Count<br>");
        writeln("tw = Term Weight<br>");
        writeln("to = Overall Terms per MG<br>");
        writeln("</th>");
        writeln("<(tr>");
    }
}
