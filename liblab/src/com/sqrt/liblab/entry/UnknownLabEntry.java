package com.sqrt.liblab.entry;

import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.io.DataSource;

public class UnknownLabEntry extends LabEntry {
    public DataSource source;

    /**
     * Copies the details from the provided source.
     */
    public UnknownLabEntry(DataSource source) {
        super(source.container, source.getName());
        this.source = source;
    }
}