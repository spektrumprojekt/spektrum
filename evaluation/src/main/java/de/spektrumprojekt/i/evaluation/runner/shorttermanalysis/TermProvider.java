package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Collection;
import java.util.Date;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

/**
 * Provides terms of either messages or user model entries.
 * 
 * 
 */
public interface TermProvider extends ConfigurationDescriptable {

    public class Entry {
        private String term;
        private Date date;
        private float score;

        public Entry(String term, Date date, float score) {
            super();
            this.term = term;
            this.date = date;
            this.score = score;
        }

        public Date getDate() {
            return date;
        }

        public float getScore() {
            return score;
        }

        public String getTerm() {
            return term;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public void setTerm(String term) {
            this.term = term;
        }
    }

    public String getFileAppendix();

    public Collection<Entry> getTerms();

}
