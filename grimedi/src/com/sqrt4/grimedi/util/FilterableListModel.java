package com.sqrt4.grimedi.util;

import com.sqrt.liblab.EntryDataProvider;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FilterableListModel<T> implements ListModel<T> {
    private List<Predicate<T>> filters = new LinkedList<Predicate<T>>();
    private List<ListDataListener> listeners = new LinkedList<ListDataListener>();

    protected List<T> source;
    protected List<T> filtered = new LinkedList<T>();

    public FilterableListModel(List<T> data) {
        source = data;
        filtered = source;
    }

    public void clearFilters() {
        filters.clear();
    }

    public void addFilter(Predicate<T> pred) {
        filters.add(pred);
    }

    public void removeFilter(Predicate<T> pred) {
        filters.remove(pred);
    }

    public void applyFilters() {
        int oldSize = filtered.size();
        List<T> s = source;
        for (Predicate<T> p : filters) {
            List<T> n = new LinkedList<T>();
            for (T t : s)
                if (p.accept(t))
                    n.add(t);
            s = n;
        }
        filtered = s;
        fireFilterChange(oldSize, filtered.size());
    }

    private void fireFilterChange(final int oldSize, final int newSize) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<ListDataEvent> events = new LinkedList<ListDataEvent>();
                if (newSize < oldSize)
                    events.add(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, newSize, oldSize)); // removed..
                events.add(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, Math.min(oldSize, newSize))); // modified
                if (oldSize < newSize)
                    events.add(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, oldSize, newSize)); // added
                for (int i = 0; i < listeners.size(); i++) {
                    ListDataListener ldl = listeners.get(i);
                    for (ListDataEvent lde : events) {
                        switch (lde.getType()) {
                            case ListDataEvent.CONTENTS_CHANGED:
                                ldl.contentsChanged(lde);
                                break;
                            case ListDataEvent.INTERVAL_ADDED:
                                ldl.intervalAdded(lde);
                                break;
                            case ListDataEvent.INTERVAL_REMOVED:
                                ldl.intervalRemoved(lde);
                                break;
                        }
                    }
                }
            }
        });
    }

    public int getSize() {
        return filtered.size();
    }

    public T getElementAt(int index) {
        return filtered.get(index);
    }

    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }
}