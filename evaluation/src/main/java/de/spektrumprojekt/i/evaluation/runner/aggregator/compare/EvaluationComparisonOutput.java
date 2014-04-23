package de.spektrumprojekt.i.evaluation.runner.aggregator.compare;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class EvaluationComparisonOutput extends
        SpektrumParseableElementFileOutput<EvaluationRunResult> {

    public EvaluationComparisonOutput() {
        super(EvaluationRunResult.class);
    }

    @Override
    protected EvaluationRunResult createNewElement(String line) {
        return new EvaluationRunResult(line);
    }

    @Override
    protected String getHeader() {
        return EvaluationRunResult.getColumnHeaders();
    }

    public void setContinuousIndex() {
        int i = 1;
        for (EvaluationRunResult element : this.getElements()) {
            element.setIndex(i++);
        }
    }
}