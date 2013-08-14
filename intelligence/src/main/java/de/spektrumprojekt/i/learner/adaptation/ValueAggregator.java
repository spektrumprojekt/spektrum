package de.spektrumprojekt.i.learner.adaptation;

public interface ValueAggregator {

    public void add(double value, double countWeight);

    public double getValue();
}
