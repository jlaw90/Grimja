package com.sqrt.liblab;

import com.sqrt.liblab.io.DataSource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class LabCollection {
    public final List<LabFile> labs = new LinkedList<LabFile>();

    public LabFile addEmptyLab() {
        LabFile lf = new LabFile(this);
        labs.add(lf);
        return lf;
    }

    public void removeLab(LabFile lf) {
        labs.remove(lf);
    }

    LabFile addExisting(File f) throws IOException {
        LabFile lf = new LabFile(this, f);
        labs.add(lf);
        return lf;
    }

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

    public LabEntry findByName(String name) throws IOException {
        for (LabFile lf : labs) {
            LabEntry le = lf.findByName(name);
            if (le != null)
                return le;
        }
        return null;
    }

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