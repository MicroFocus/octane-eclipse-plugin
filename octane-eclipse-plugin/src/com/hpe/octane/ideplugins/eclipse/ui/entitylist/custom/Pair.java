package com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom;

public class Pair<F, S> {
    
    public F first; //first member of pair
    public S second; //second member of pair

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
    
}