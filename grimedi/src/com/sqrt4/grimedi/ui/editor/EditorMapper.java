package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.model.ColorMap;
import com.sqrt.liblab.model.GrimBitmap;
import com.sqrt.liblab.model.GrimFont;
import com.sqrt.liblab.model.Material;

import java.util.HashMap;
import java.util.Map;

public class EditorMapper {
    private EditorMapper(){}

    public static Map<Class<? extends LabEntry>, EditorPanel<? extends LabEntry>> mapping = new HashMap<Class<? extends LabEntry>, EditorPanel<? extends LabEntry>>();

    public static <T extends LabEntry> void map(Class<T> modelType, EditorPanel<T> panel) {
        mapping.put(modelType, panel);
    }

    public static <T extends LabEntry> EditorPanel<T> unmap(Class<T> modelType) {
        return (EditorPanel<T>) mapping.remove(modelType);
    }

    public static void registerDefaults() {
        map(GrimFont.class, new FontView());
        map(GrimBitmap.class, new ImageView());
        map(ColorMap.class, new ColorMapView());
        map(Material.class, new MaterialView());
    }

    public static EditorPanel editorPanelForProvider(EntryDataProvider selected) {
        EntryCodec<?> codec = CodecMapper.codecForProvider(selected);
        if(codec == null)
            return null;
        return mapping.get(codec.getEntryClass());
    }

    static {
        EditorMapper.registerDefaults();
    }
}