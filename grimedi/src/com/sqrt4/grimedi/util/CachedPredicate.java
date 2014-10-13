

/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of GrimEdi.
 *
 *     GrimEdi is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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