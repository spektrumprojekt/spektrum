package de.spektrumprojekt.i.evaluation.recallstats;

import java.util.ArrayList;
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.spektrumprojekt.i.evaluation.measure.EvaluatorDataPoint;
import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;

public class StatsUserTimeBinTest {

    @Test
    public void test() throws Exception {

        StatsPerUserTimeBin stats = new StatsPerUserTimeBin();

        Collection<EvaluatorDataPoint> dps = new ArrayList<EvaluatorDataPoint>();

        EvaluatorDataPoint edp = new EvaluatorDataPoint(0, 1);
        edp.setUserGlobalId("1");
        edp.setTimeBin(9);
        dps.add(edp);

        edp = new EvaluatorDataPoint(0, 1);
        edp.setUserGlobalId("1");
        edp.setTimeBin(8);
        dps.add(edp);

        edp = new EvaluatorDataPoint(0, 0.9);
        edp.setUserGlobalId("1");
        edp.setTimeBin(8);
        dps.add(edp);

        stats.compute(dps);

        String json = stats.toJson();

        StatsPerUserTimeBin readStats = new StatsPerUserTimeBin();
        readStats.fromJson(json);

        Assert.assertEquals(readStats.get("1", 8).getNumberRatings(), 2);
        Assert.assertEquals(readStats.get("1", 8).getNumberPositiveReferences(), 1);

    }

}
