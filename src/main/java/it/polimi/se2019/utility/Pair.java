package it.polimi.se2019.utility;

import java.util.Objects;

public class Pair<T , S > {
    private T first;
    private S second;

    public Pair(T first, S second){
        this.first = first;
        this.second = second;
    }

    public void setFirst(T first){
        if(first == (null))
            throw new NullPointerException("Cannot have a null value in a pair");
        this.first = first;
    }

    public void setSecond(S second) {
        if(second == (null))
            throw new NullPointerException("Cannot have a null value in a pair");
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return first.equals(pair.first) &&
                second.equals(pair.second);
    }
}