package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabEntry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class EntryCodec<T extends LabEntry> {
    private Map<String, T> cache = new HashMap<String, T>();

   public final T read(EntryDataProvider edp) throws IOException {
       if(cache.containsKey(edp.getName()))
           return cache.get(edp.getName());
       T t = _read(edp);
       if(t != null)
           cache.put(edp.getName(), t);
       return t;
   }

    protected abstract T _read(EntryDataProvider source) throws IOException;

    public abstract EntryDataProvider write(T source) throws IOException;

    public abstract String[] getFileExtensions();

    public abstract byte[][] getFileHeaders();

    public abstract Class<T> getEntryClass();
}