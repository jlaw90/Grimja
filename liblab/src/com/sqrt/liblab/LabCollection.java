package com.sqrt.liblab;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.io.DataSource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * This class encapsulates a collection of LabFiles.  The reason for its existence is that some entries reference
 * entries that are contained within other LAB files.
 */
public class LabCollection {
    public final List<LabFile> labs = new LinkedList<LabFile>();

    /**
     * Creates an empty LAB file and adds it to the collection
     * @return the creation
     */
    public LabFile addEmptyLab() {
        LabFile lf = new LabFile(this);
        labs.add(lf);
        return lf;
    }

    /**
     * Removes the specified LAB file from this collection
     * @param lf the LAB file to remove
     */
    public void removeLab(LabFile lf) {
        labs.remove(lf);
    }

    LabFile addExisting(File f) throws IOException {
        LabFile lf = new LabFile(this, f);
        labs.add(lf);
        return lf;
    }

    /**
     * Returns all LabEntry's from all LabFiles that have a model of the specified type
     * @param type the type to search for
     * @param <T> the type to search for
     * @return a list containing the results
     */
    public <T extends LabEntry> List<DataSource> findByType(Class<T> type) {
        List<DataSource> res = new LinkedList<DataSource>();
        for (LabFile lf : labs) {
            List<DataSource> r = lf.findByType(type);
            if (r != null)
                res.addAll(r);
        }
        if (res.isEmpty())
            return null;
        Collections.sort(res, new Comparator<DataSource>() {
            public int compare(DataSource o1, DataSource o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return res;
    }

    /**
     * Searches all the LabFiles for an entry with the specified name and returns the first result
     * @param name the name to search for (case insensitive)
     * @return the entry we found, or null if there is none
     * @throws IOException
     */
    public LabEntry findByName(String name) throws IOException {
        for (LabFile lf : labs) {
            LabEntry le = lf.findByName(name);
            if (le != null)
                return le;
        }
        return null;
    }

    /**
     * Finds all LAB files in the specified directory and loads them into this collection
     * @param dir the directory to load the LAB files from
     * @return the collection
     * @throws IOException
     */
    public static LabCollection open(File dir) throws IOException {
        File[] labs = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".lab");
            }
        });
        if (labs.length == 0)
            throw new IOException("No LAB files found in the specified directory");

        Arrays.sort(labs, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        LabCollection lc = new LabCollection();
        for (File f : labs)
            lc.addExisting(f);
        return lc;
    }
}