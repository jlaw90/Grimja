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

    public void save(File dir) throws IOException {
        for(LabFile lf: labs) {
            lf.save(new File(dir, lf.getName() + ".lab"));
        }
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
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        LabCollection lc = new LabCollection();
        for (File f : labs)
            lc.addExisting(f);
        return lc;
    }

    public String toString() {
        return "LABs";
    }
}