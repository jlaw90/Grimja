package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.UnknownLabEntry;
import com.sqrt.liblab.io.DataSource;

import java.io.IOException;

public class DefaultCodec extends EntryCodec<UnknownLabEntry> {
    protected UnknownLabEntry _read(DataSource source) throws IOException {
        return new UnknownLabEntry(source);
    }

    public DataSource write(UnknownLabEntry source) throws IOException {
        return source.source; // easy
    }

    public String[] getFileExtensions() {
        return new String[0];
    }

    public Class<UnknownLabEntry> getEntryClass() {
        return UnknownLabEntry.class;
    }
}