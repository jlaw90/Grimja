package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabEntry;

import java.io.IOException;

public interface EntryCodec<T extends LabEntry> {
    public T read(EntryDataProvider source) throws IOException;

    public EntryDataProvider write(T source) throws IOException;

    public String[] getFileExtensions();

    public byte[][] getFileHeaders();

    public Class<T> getEntryClass();
}