package net.automatalib.modelcheckers.m3c.transformer;

import java.util.Set;

public abstract class PropertyTransformer {

    protected boolean isMust = true;

    public PropertyTransformer() {
    }

    public abstract Set<Integer> evaluate(boolean[] input);

    public abstract PropertyTransformer compose(PropertyTransformer other);

    public boolean isMust() {
        return isMust;
    }

    public void setIsMust(boolean isMust) {
        this.isMust = isMust;
    }

}
