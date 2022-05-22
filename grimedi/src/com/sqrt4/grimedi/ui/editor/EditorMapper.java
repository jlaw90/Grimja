

/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of GrimEdi.
 *
 *     GrimEdi is free software: you can redistribute it and/or modify
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

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.entry.audio.Audio;
import com.sqrt.liblab.entry.graphics.GrimBitmap;
import com.sqrt.liblab.entry.graphics.GrimFont;
import com.sqrt.liblab.entry.model.ColorMap;
import com.sqrt.liblab.entry.model.GrimModel;
import com.sqrt.liblab.entry.model.Material;
import com.sqrt.liblab.entry.model.anim.Animation;
import com.sqrt.liblab.entry.video.Video;
import com.sqrt.liblab.io.DataSource;

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
        if(!mapping.isEmpty())
            return; // already done
        map(GrimFont.class, new FontView());
        map(GrimBitmap.class, new ImageView());
        map(ColorMap.class, new ColorMapView());
        map(Material.class, new MaterialView());
        map(GrimModel.class, new ModelView());
        map(Animation.class, new AnimationEditor());
        map(Audio.class, new AudioEditor());
        map(Video.class, new VideoViewer());
    }

    public static EditorPanel editorPanelForProvider(DataSource selected) {
        EntryCodec<?> codec = CodecMapper.codecForProvider(selected);
        if(codec == null)
            return null;
        return mapping.get(codec.getEntryClass());
    }

    static {
        EditorMapper.registerDefaults();
    }
}
