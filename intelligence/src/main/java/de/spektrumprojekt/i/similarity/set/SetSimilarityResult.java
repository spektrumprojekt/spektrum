package de.spektrumprojekt.i.similarity.set;

import java.util.Collection;

import org.apache.commons.collections15.CollectionUtils;

public class SetSimilarityResult {

    private float sim;

    private int set1Size;

    private int set2Size;
    private int unionSize;
    private int intersectionSize;

    public SetSimilarityResult() {

    }

    public <E> SetSimilarityResult(Collection<? extends E> a, Collection<? extends E> b) {

        set1Size = a.size();
        set2Size = b.size();

        Collection<E> intersection = CollectionUtils.intersection(a, b);
        Collection<E> union = CollectionUtils.union(a, b);

        intersectionSize = intersection.size();
        unionSize = union.size();
    }

    public int getIntersectionSize() {
        return intersectionSize;
    }

    public int getSet1Size() {
        return set1Size;
    }

    public int getSet2Size() {
        return set2Size;
    }

    public float getSim() {
        return sim;
    }

    public int getUnionSize() {
        return unionSize;
    }

    public void setIntersectionSize(int intersectionSize) {
        this.intersectionSize = intersectionSize;
    }

    public void setSet1Size(int set1Size) {
        this.set1Size = set1Size;
    }

    public void setSet2Size(int set2Size) {
        this.set2Size = set2Size;
    }

    public void setSim(float sim) {
        this.sim = sim;
    }

    public void setUnionSize(int unionSize) {
        this.unionSize = unionSize;
    }

    @Override
    public String toString() {
        return "SetSimilarityResult [sim=" + sim + ", set1Size=" + set1Size + ", set2Size="
                + set2Size + ", unionSize=" + unionSize + ", intersectionSize=" + intersectionSize
                + "]";
    }

}