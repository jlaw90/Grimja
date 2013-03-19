package com.sqrt4.grimedi.util;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class FilterableComboBoxModel<E> extends FilterableListModel<E> implements ComboBoxModel<E> {
    private E selected;

    public FilterableComboBoxModel(List<E> data) {
        super(data);
    }

    public void setSelectedItem(Object anItem) {
        if(filtered.contains(anItem))
            selected = (E) anItem;
        else
            selected = filtered.isEmpty()? null: filtered.get(0);
    }

    public Object getSelectedItem() {
        return selected;
    }

    public void applyFilters() {
        super.applyFilters();
    }
}