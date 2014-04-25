package de.spektrumprojekt.i.evaluation.runner.timetracker;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class TimeBin<Items extends Object, Stats extends Object> implements
        Comparable<TimeBin<Items, Stats>> {

    private Date start;

    private Date end;

    private int index;

    private Collection<Items> items = new HashSet<Items>();

    private Stats tag;

    public int compareTo(TimeBin<Items, Stats> o) {
        if (this == o) {
            return 0;
        }
        if (o == null) {
            return -1;
        }
        // start of this is after the end
        if (start.getTime() >= o.getEnd().getTime()) {
            return 1;
        }
        if (end.getTime() <= o.getStart().getTime()) {
            return -1;
        }
        throw new IllegalArgumentException("Illegal bin " + o);
    }

    public Date getEnd() {
        return end;
    }

    public int getIndex() {
        return index;
    }

    public Collection<Items> getItems() {
        return items;
    }

    public Date getStart() {
        return start;
    }

    public Stats getTag() {
        return tag;
    }

    public boolean matches(Date date) {
        return start.getTime() <= date.getTime() && date.getTime() <= end.getTime();
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setItems(Collection<Items> items) {
        this.items = items;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setTag(Stats tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return this.index + " " + this.start + " - " + this.end;
    }

}
