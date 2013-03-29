package com.sqrt.liblab.codec;

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.LabEntry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public abstract class EntryCodec<T extends LabEntry> {
    private Map<String, WeakReference<T>> cache = new HashMap<String, WeakReference<T>>();

   public final T read(DataSource edp) throws IOException {
       String key = edp.getName();
       if(cache.containsKey(key)) {
           WeakReference<T> ref = cache.get(key);
           T t = ref.get();
           if(t == null)
               cache.remove(key);
           else
               return t;
       }
       T t = _read(edp);
       if(t != null)
           cache.put(key, new WeakReference<T>(t));
       return t;
   }

    protected abstract T _read(DataSource source) throws IOException;

    public abstract DataSource write(T source) throws IOException;

    public abstract String[] getFileExtensions();

    public abstract Class<T> getEntryClass();

    public static String tagToString(int tag) {
        return new String(new char[] {(char) ((tag >> 24)&0xff),
                (char) ((tag >> 16) & 0xff), (char) ((tag >> 8) & 0xff), (char) (tag & 0xff)});
    }
}