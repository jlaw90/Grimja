package com.sqrt4.grimedi.util;

import java.util.HashMap;
import java.util.Map;

public class CachedPredicate<T> implements Predicate<T> {
    private Predicate<T> delegate;
    private Map<T, Boolean> cache = new HashMap<T, Boolean>();

    public CachedPredicate(Predicate<T> delegate) {
        this.delegate = delegate;
    }

    public boolean accept(T t) {
        if(cache.containsKey(t))
            return cache.get(t);
        boolean res = delegate.accept(t);
        cache.put(t, res);
        return res;
    }
}