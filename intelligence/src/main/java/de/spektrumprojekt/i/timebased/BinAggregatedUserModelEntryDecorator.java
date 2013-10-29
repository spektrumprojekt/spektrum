package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;

public class BinAggregatedUserModelEntryDecorator extends UserModelEntry {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param entriesToAggregate
     *            needs to be ordered, oldest first for correct starttime
     * @return aggregated Entry
     */
    private static UserModelEntryTimeBin aggragateEntries(
            LinkedList<UserModelEntryTimeBin> entriesToAggregate) {
        // fist entry is oldest
        UserModelEntryTimeBin aggregatedBin = new UserModelEntryTimeBin(entriesToAggregate
                .getFirst().getTimeBinStart());
        float scoreCount = 0;
        float scoreSum = 0;
        for (UserModelEntryTimeBin originalBin : entriesToAggregate) {
            scoreCount += originalBin.getScoreCount();
            scoreSum += originalBin.getScoreSum();
        }
        aggregatedBin.setScoreCount(scoreCount);
        aggregatedBin.setScoreSum(scoreSum);
        return aggregatedBin;
    }

    private static LinkedList<UserModelEntryTimeBin> aggregateTimeBins(
            Collection<UserModelEntryTimeBin> timeBins, int binAggragationCount) {
        LinkedList<UserModelEntryTimeBin> result = new LinkedList<UserModelEntryTimeBin>();
        LinkedList<UserModelEntryTimeBin> entries = new LinkedList<UserModelEntryTimeBin>();
        entries.addAll(timeBins);
        // order newest last
        Collections.sort(entries, new Comparator<UserModelEntryTimeBin>() {
            @Override
            public int compare(UserModelEntryTimeBin o1, UserModelEntryTimeBin o2) {
                if (o1.getTimeBinStart() - o2.getTimeBinStart() > 0) {
                    return 1;
                }
                if (o1.getTimeBinStart() - o2.getTimeBinStart() == 0) {
                    return 0;
                }
                return -1;
            }
        });
        // entries to aggregate to one bin
        LinkedList<UserModelEntryTimeBin> entriesToAggregate = new LinkedList<UserModelEntryTimeBin>();
        int entriesCount = entries.size();
        for (int i = entriesCount - 1; i >= 0; i--) {
            entriesToAggregate.addFirst(entries.removeLast());
            if (entriesToAggregate.size() == binAggragationCount) {
                result.addFirst(aggragateEntries(entriesToAggregate));
                entriesToAggregate.clear();
            }
        }
        return result;
    }

    private UserModelEntry entry;

    private final int binAggragationCount;

    public BinAggregatedUserModelEntryDecorator(int binAggragationCount) {
        super();
        this.binAggragationCount = binAggragationCount;
    }

    @Override
    public void addTimeBinEntry(UserModelEntryTimeBin timeBin) {
        entry.addTimeBinEntry(timeBin);
    }

    @Override
    public boolean addToTimeBinEntriesHistory(UserModelEntryTimeBin e) {
        return entry.addToTimeBinEntriesHistory(e);
    }

    @Override
    public void consolidate() {
        entry.consolidate();
    }

    @Override
    public void consolidateByTimeBins() {
        entry.consolidateByTimeBins();
    }

    public int getBinAggragationCount() {
        return binAggragationCount;
    }

    public UserModelEntry getEntry() {
        return entry;
    }

    @Override
    public String getGlobalId() {
        return entry.getGlobalId();
    }

    @Override
    public Long getId() {
        return entry.getId();
    }

    @Override
    public float getScoreCount() {
        return entry.getScoreCount();
    }

    @Override
    public ScoredTerm getScoredTerm() {
        return entry.getScoredTerm();
    }

    @Override
    public float getScoreSum() {
        return entry.getScoreSum();
    }

    @Override
    public Collection<UserModelEntryTimeBin> getTimeBinEntries() {
        return aggregateTimeBins(entry.getTimeBinEntries(), binAggragationCount);

    }

    @Override
    public Collection<UserModelEntryTimeBin> getTimeBinEntriesHistory() {
        return aggregateTimeBins(entry.getTimeBinEntriesHistory(), binAggragationCount);
    }

    @Override
    public UserModelEntryTimeBin getUserModelEntryTimeBinByStartTime(long timeBinStartTime) {
        LinkedList<UserModelEntryTimeBin> entries = new LinkedList<UserModelEntryTimeBin>();
        entries.addAll(entry.getTimeBinEntries());
        // order newest last
        Collections.sort(entries, new Comparator<UserModelEntryTimeBin>() {
            @Override
            public int compare(UserModelEntryTimeBin o1, UserModelEntryTimeBin o2) {
                if (o1.getTimeBinStart() - o2.getTimeBinStart() > 0) {
                    return 1;
                }
                if (o1.getTimeBinStart() - o2.getTimeBinStart() == 0) {
                    return 0;
                }
                return -1;
            }
        });
        // entries to aggregate to one bin
        LinkedList<UserModelEntryTimeBin> entriesToAggregate = new LinkedList<UserModelEntryTimeBin>();
        int entriesCount = entries.size();
        for (int i = 0; i < entriesCount; i++) {
            // timeBinStart found
            if (entries.get(i).getTimeBinStart() == timeBinStartTime) {
                for (int j = 0; j < binAggragationCount; j++) {
                    int index = j + i;
                    if (index < entries.size()) {
                        entriesToAggregate.addLast(entries.get(index));
                    }
                }
                return aggragateEntries(entriesToAggregate);
            }
        }
        return null;
    }

    @Override
    public boolean isAdapted() {
        return entry.isAdapted();
    }

    @Override
    public UserModelEntryTimeBin removeUserModelEntryTimeBin(long timeBinStartTime) {
        return entry.removeUserModelEntryTimeBin(timeBinStartTime);
    }

    @Override
    public void setAdapted(boolean adapted) {
        entry.setAdapted(adapted);
    }

    public void setEntry(UserModelEntry entry) {
        this.entry = entry;
    }

    @Override
    public void setId(Long id) {
        entry.setId(id);
    }

    @Override
    public void setScoreCount(float scoreCount) {
        entry.setScoreCount(scoreCount);
    }

    @Override
    public void setScoredTerm(ScoredTerm scoredTerm) {
        entry.setScoredTerm(scoredTerm);
    }

    @Override
    public void setScoreSum(float scoreSum) {
        entry.setScoreSum(scoreSum);
    }

    @Override
    public String toString() {
        return "BinAggregatedUserModelEntryDecorator[binAggragationCount=" + binAggragationCount
                + ", entry=" + entry.toString() + "]";
    }

}
