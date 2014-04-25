package de.spektrumprojekt.i.evaluation.stats;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.observation.Interest;

public class RatingCount {

    public static String toHeaderString(String prefix, IntegerWrapper headerCount) {

        StringBuilder sb = new StringBuilder();
        String pre = "";
        prefix = "".equals(prefix) ? prefix : prefix + "_";
        for (InteractionLevelCombinationsEnum il : InteractionLevelCombinationsEnum.values()) {
            sb.append(pre);
            sb.append(StringUtils.join(new String[] {
                    headerCount.value++ + "_" + il.name() + "_" + prefix + "overall",
                    headerCount.value++ + "_" + il.name() + "_" + prefix + "pos",
                    headerCount.value++ + "_" + il.name() + "_" + prefix + "normal",
                    headerCount.value++ + "_" + il.name() + "_" + prefix + "neg"
            }, " "));
            pre = " ";
        }
        return sb.toString();

    }

    private final Map<InteractionLevelCombinationsEnum, SingleCount> counts = new HashMap<InteractionLevelCombinationsEnum, SingleCount>();

    public RatingCount() {

        for (InteractionLevelCombinationsEnum il : InteractionLevelCombinationsEnum.values()) {
            this.counts.put(il, new SingleCount());
        }
    }

    public SingleCount getSingleCount4All() {
        return this.counts.get(InteractionLevelCombinationsEnum.ALL);
    }

    public void integrate(Interest interest, InteractionLevel interactionLevel) {
        this.counts.get(InteractionLevelCombinationsEnum.ALL).integrate(interest);
        switch (interactionLevel) {
        case DIRECT:
            this.counts.get(InteractionLevelCombinationsEnum.DIRECT).integrate(interest);
            break;
        case INDIRECT:
            this.counts.get(InteractionLevelCombinationsEnum.INDIRECT).integrate(interest);
            this.counts.get(InteractionLevelCombinationsEnum.NOTDIRECT).integrate(interest);
            break;
        case NONE:
            this.counts.get(InteractionLevelCombinationsEnum.NONE).integrate(interest);
            this.counts.get(InteractionLevelCombinationsEnum.NOTDIRECT).integrate(interest);
            break;
        default:
            throw new RuntimeException(interactionLevel.toString());
        }

    }

    public int overall() {
        return getSingleCount4All().overall();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (InteractionLevelCombinationsEnum il : InteractionLevelCombinationsEnum.values()) {
            sb.append(prefix);
            sb.append(this.counts.get(il).toString());
            prefix = " ";
        }
        return sb.toString();
    }
}