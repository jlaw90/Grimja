

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

package com.sqrt4.grimedi.util;

import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by James on 28/09/2014.
 */
public class AnimatedGifCreator {
    private static final ImageWriter gifEncoder;
    private static final ImageWriteParam defaultParams;

    private final ImageOutputStream dest;
    private final IIOMetadata metadata;

    public AnimatedGifCreator(ImageOutputStream dest, int imageType, float fps, boolean loop, int loopCount) throws IOException {
        this.dest = dest;
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

        metadata = gifEncoder.getDefaultImageMetadata(imageTypeSpecifier, defaultParams);
        String metaFormatName = metadata.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
        IIOMetadataNode gcen = findNode(root, "GraphicControlExtension");
        gcen.setAttribute("disposalMethod", "none");
        gcen.setAttribute("userInputFlag", "FALSE");
        gcen.setAttribute("transparentColorFlag", "FALSE");
        gcen.setAttribute("delayTime", Integer.toString((int) (100f / fps)));
        gcen.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode aepn = findNode(root, "ApplicationExtensions");
        IIOMetadataNode aen = new IIOMetadataNode("ApplicationExtension");
        aen.setAttribute("applicationID", "NETSCAPE");
        aen.setAttribute("authenticationCode", "2.0");

        int l = loop ? loopCount : 1;
        aen.setUserObject(new byte[]{0x1, (byte) (l & 0xff), (byte) ((l >> 8) & 0xff)});
        aepn.appendChild(aen);

        metadata.setFromTree(metaFormatName, root);
        gifEncoder.setOutput(dest);
        gifEncoder.prepareWriteSequence(null);
    }

    public void addFrame(RenderedImage i) throws IOException {
        gifEncoder.writeToSequence(new IIOImage(i, null, metadata), defaultParams);
    }

    public void finish() throws IOException {
        gifEncoder.endWriteSequence();
        gifEncoder.setOutput(null);
    }

    private static IIOMetadataNode findNode(IIOMetadataNode parent, String name) {
        for(Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if(n.getNodeName().equalsIgnoreCase(name))
                return (IIOMetadataNode) n;
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        parent.appendChild(node);
        return node;
    }

    static {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("gif");
        if (!writers.hasNext())
            throw new RuntimeException("GIF encoding not available");

        gifEncoder = writers.next();
        defaultParams = gifEncoder.getDefaultWriteParam();
    }
}