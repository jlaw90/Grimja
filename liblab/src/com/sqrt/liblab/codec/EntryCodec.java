/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This program is free software: you can redistribute it and/or modify
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

package com.sqrt.liblab.codec;

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.entry.LabEntry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * An <code>EntryCodec</code> performs conversion from binary data to an <code>Object</code> and, optionally
 * performs conversion from an <code>Object</code> back to a binary form
 *
 * This class cache's the objects that have been converted from binary data with <code>WeakReference</code>'s as to
 * avoid having to load the same object multiple times.  This policy will most likely be changed in future.  I can
 * forsee it causing havoc with modified files being lost and other problems.
 *
 * @param <T> The type of the <code>Object</code> that this codec understands
 */
public abstract class EntryCodec<T extends LabEntry> {
    private Map<String, WeakReference<T>> cache = new HashMap<String, WeakReference<T>>();

    /**
     * Reads an object from the specified <code>DataSource</code>
     * @param edp the source of the data from which we will decode our Object
     * @return the object
     * @throws IOException
     */
    public final T read(DataSource edp) throws IOException {
       String key = edp.container.toString() + "." + edp.getName();
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

    /**
     * Subclasses need to override this to perform the conversion from binary data to our model
     * @param source the datasource
     * @return the object
     * @throws IOException
     */
    protected abstract T _read(DataSource source) throws IOException;

    /**
     * Although not currently used, in future this will be how we write LAB files out: by requesting the codecs
     * to provide us with a DataSource so that we can calculate offsets, build the string table, etc.
     * @param source the model
     * @return a source for the binary representation of the model
     * @throws IOException
     */
    public abstract DataSource write(T source) throws IOException;

    /**
     * Simply returns the file extensions that this codec can handle
     * @return the file extensions we are interested in
     */
    public abstract String[] getFileExtensions();

    /**
     * The name of the format we convert to/from
     * (Optional for subclasses)
     * @return the name of the format we convert to/from
     */
    public String getFormatName() {
        // Todo: this was never implemented in subclasses, wtf was its intention?
        return null; // Optional, null by default
    }

    /**
     * The class of the model object, used to lookup the codec.
     * @return the class of the model object
     */
    public abstract Class<T> getEntryClass();

    /**
     * A helper method used in debugging to convert an integer to a 4-bit tag string
     * @param tag the integer
     * @return the 4 bytes of the integer converted to ASCII
     */
    protected static String tagToString(int tag) {
        return new String(new char[] {(char) ((tag >> 24)&0xff),
                (char) ((tag >> 16) & 0xff), (char) ((tag >> 8) & 0xff), (char) (tag & 0xff)});
    }
}