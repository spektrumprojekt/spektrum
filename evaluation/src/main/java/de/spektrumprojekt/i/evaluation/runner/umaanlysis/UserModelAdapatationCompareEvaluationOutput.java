package de.spektrumprojekt.i.evaluation.runner.umaanlysis;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;

public class UserModelAdapatationCompareEvaluationOutput extends
        SpektrumParseableElementFileOutput<UserModelAdapatationCompareEvaluationElement> {

    public UserModelAdapatationCompareEvaluationOutput() {
        super(UserModelAdapatationCompareEvaluationElement.class);
    }

    @Override
    protected UserModelAdapatationCompareEvaluationElement createNewElement(String line) {
        return new UserModelAdapatationCompareEvaluationElement(line);
    }

    @Override
    protected String getHeader() {
        return UserModelAdapatationCompareEvaluationElement.getColumnHeaders();
    }

}