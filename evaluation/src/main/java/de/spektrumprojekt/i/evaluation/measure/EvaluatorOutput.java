package de.spektrumprojekt.i.evaluation.measure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.stats.IntegerWrapper;

/**
 * The evaluator output contains {@link EvaluatorDataPoint}s which is a tupel of the message id, the
 * user id, the computed score and goal/target score. Hence there are only data points for messages
 * and user pairs where a target rating exists
 * 
 * 
 */
public class EvaluatorOutput extends SpektrumParseableElementFileOutput<EvaluatorDataPoint> {

    private static final String TB_USER_COUNT_SEPERATOR = " ";
    private static final String TB_USER_COUNT = "tbUserCount:";
    private long overallItems;
    private final static String MESSAGE_SIZE_CONFIG = "messages size";

    public EvaluatorOutput() {
        super(EvaluatorDataPoint.class);
    }

    public void addTimebinRelevantCounts(Map<String, IntegerWrapper> relevantCount) {

        for (Entry<String, IntegerWrapper> entry : relevantCount.entrySet()) {
            getDescriptions().add(
                    TB_USER_COUNT + entry.getKey() + TB_USER_COUNT_SEPERATOR
                            + entry.getValue().value);
        }
    }

    /**
     * Creates a map of {@link SpektrumRating#getIdentifier(String, String)} to evaluator data
     * points currently set in {@link #getElements()}
     * 
     * @return
     */
    public Map<String, EvaluatorDataPoint> createIdentifierToDataPointMap() {
        Map<String, EvaluatorDataPoint> outputs = new HashMap<String, EvaluatorDataPoint>();
        for (EvaluatorDataPoint out : this.getElements()) {

            String key = SpektrumRating.getIdentifier(out.getUserGlobalId(),
                    out.getMessageGlobalId());
            outputs.put(key, out);
        }
        return outputs;
    }

    @Override
    protected EvaluatorDataPoint createNewElement(String line) {
        return new EvaluatorDataPoint(line);
    }

    @Override
    public Class<EvaluatorDataPoint> getHandlingClass() {
        return EvaluatorDataPoint.class;
    }

    @Override
    protected String getHeader() {
        return EvaluatorDataPoint.getColumnHeaders();
    }

    public long getOverallItems() {
        return overallItems;
    }

    public Map<String, Integer> getTimebinRelevantCounts() {
        Map<String, Integer> relevantCount = new HashMap<String, Integer>();
        for (String desc : getDescriptions()) {
            if (desc.startsWith(TB_USER_COUNT)) {
                desc = desc.substring(TB_USER_COUNT.length()).trim();
                String[] vals = desc.split(TB_USER_COUNT_SEPERATOR);
                String key = vals[0];
                int val = Integer.parseInt(vals[1]);
                relevantCount.put(key, val);
            }
        }
        return relevantCount;
    }

    @Override
    public void read(String filename) throws IOException {
        super.read(filename);

        readOverallItems();
    }

    private void readOverallItems() {
        // look for sth like: # messages size: 8507
        overallItems = -1;

        String numStr = this.getDescriptionValue("messages size");
        if (numStr != null) {
            overallItems = Long.parseLong(numStr);
        }
        if (overallItems < 0) {
            throw new RuntimeException("Could not read overal items size. Is there a '#"
                    + MESSAGE_SIZE_CONFIG + ": 12345' included?");
        }
    }

}
