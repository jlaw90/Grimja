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

package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.model.Texture;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.entry.model.*;
import com.sqrt.liblab.threed.Vector2f;
import com.sqrt.liblab.threed.Vector3f;
import com.sun.prism.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ModelCodec extends EntryCodec<GrimModel> {
    public GrimModel _read(DataSource source) throws IOException {
        if (source.getIntLE() != (('M' << 24) | ('O' << 16) | ('D' << 8) | 'L'))
            throw new IOException("Invalid model format (text format W.I.P)"); // Todo
        GrimModel mod = loadBinary(source);
        //exportWavefront(new File(source.getName() + ".obj"), mod);
        return mod;
    }

    private GrimModel loadBinary(DataSource source) throws IOException {
        int numMaterials = source.getIntLE();
        Material[] materials = new Material[numMaterials];
        for (int i = 0; i < numMaterials; i++) {
            String name = source.getString(32);
            materials[i] = (Material) source.container.container.findByName(name);
            if(materials[i] == null) {
                System.out.println("No material named: " + name);
            }
        }
        GrimModel model = new GrimModel(source.container, source.getName());
        String name = source.getString(32);
        source.skip(4);
        int numGeosets = source.getIntLE();
        for (int i = 0; i < numGeosets; i++)
            model.geosets.add(loadGeosetBinary(source, materials));
        source.skip(4);
        int numNodes = source.getIntLE();
        for(int i = 0; i < numNodes; i++)
            model.hierarchy.add(loadModelNodeBinary(source, model.geosets.get(0)));
        for(ModelNode node: model.hierarchy) {
            if(node.childIdx >= 0)
                node.child = model.hierarchy.get(node.childIdx);
            if(node.parentIdx >= 0)
                node.parent = model.hierarchy.get(node.parentIdx);
            if(node.siblingIdx >= 0)
                node.sibling = model.hierarchy.get(node.siblingIdx);
        }
        model.radius = source.getFloatLE();
        source.skip(36);
        model.off = source.getVector3f();
        return model;
    }

    private ModelNode loadModelNodeBinary(DataSource source, Geoset geoset) throws IOException {
        ModelNode node = new ModelNode();
        node.name = source.getString(64);
        node.flags = source.getIntLE();
        source.skip(4);
        node.type = source.getIntLE();
        int meshNum = source.getIntLE();
        node.mesh = meshNum < 0? null: geoset.meshes.get(meshNum);
        node.depth = source.getIntLE();
        boolean hasParent = source.getBoolean();
        int numChildren = source.getIntLE();
        boolean hasChild = source.getBoolean();
        boolean hasSibling = source.getBoolean();
        node.pivot = source.getVector3f();
        node.pos = source.getVector3f();
        node.pitch = source.getAngle();
        node.yaw = source.getAngle();
        node.roll = source.getAngle();

        source.skip(48);
        ModelNode parent, sibling, child;
        if(hasParent)
            node.parentIdx = source.getIntLE();
        if(hasChild)
            node.childIdx = source.getIntLE();
        if(hasSibling)
            node.siblingIdx = source.getIntLE();
        return node;
    }

    private Geoset loadGeosetBinary(DataSource source, Material[] materials) throws IOException {
        Geoset g = new Geoset();
        int numMeshes = source.getIntLE();
        for (int i = 0; i < numMeshes; i++)
            g.meshes.add(loadMeshBinary(source, materials));
        return g;
    }

    private Mesh loadMeshBinary(DataSource source, Material[] materials) throws IOException {
        Mesh m = new Mesh();
        m.name = source.getString(32);
        source.skip(4);
        m.geomMode = source.getIntLE();
        m.lightMode = source.getIntLE();
        m.texMode = source.getIntLE();
        int numVertices = source.getIntLE();
        int numTextureVerts = source.getIntLE();
        int numFaces = source.getIntLE();

        Vector3f[] vertices = new Vector3f[numVertices];
        float[] verticesI = new float[numVertices];
        Vector3f[] normals = new Vector3f[numVertices];
        Vector2f[] textureVerts = new Vector2f[numTextureVerts];
        for (int i = 0; i < numVertices; i++)
            vertices[i] =source.getVector3f();
        for (int i = 0; i < numTextureVerts; i++)
            textureVerts[i] = source.getVector2f();
        for (int i = 0; i < numVertices; i++)
            verticesI[i] = source.getFloatLE();
        source.skip(numVertices * 4);
        int[][] normalTemp = new int[numFaces][];
        for (int i = 0; i < numFaces; i++)
            m.faces.add(loadMeshFaceBinary(source, materials, vertices, normalTemp, i, textureVerts));
        for (int i = 0; i < numVertices; i++)
            normals[i] = source.getVector3f();
        for (int i = 0; i < numFaces; i++) {
            MeshFace mf = m.faces.get(i);
            for (int index : normalTemp[i])
                mf.normals.add(normals[index]);
        }
        m.shadow = source.getIntLE();
        source.skip(4);
        m.radius = source.getFloatLE();
        source.skip(24);

        return m;
    }

    private MeshFace loadMeshFaceBinary(DataSource source, Material[] materials,
                                        Vector3f[] vertexTable, int[][] normalTemp, int normalOff,
                                        Vector2f[] texVertexTable) throws IOException {
        source.skip(4);
        int type = source.getIntLE();
        int geo = source.getIntLE();
        int light = source.getIntLE();
        int tex = source.getIntLE();
        int numVertices = source.getIntLE();
        normalTemp[normalOff] = new int[numVertices];
        source.skip(4);
        boolean hasTexture = source.getBoolean();
        boolean hasMaterial = source.getBoolean();
        source.skip(12);
        float extraLight = source.getFloatLE();
        source.skip(12);
        Vector3f normal = source.getVector3f();
        MeshFace mf = new MeshFace();
        for (int i = 0; i < numVertices; i++) {
            int vid = source.getIntLE();
            mf.vertices.add(vertexTable[vid]);
            normalTemp[normalOff][i] = vid;
        }
        if (hasTexture) {
            for (int i = 0; i < numVertices; i++)
                mf.uv.add(texVertexTable[source.getIntLE()]);
        }
        if (hasMaterial) {
            int matIdx = source.getIntLE();
            Material mat = mf.material = materials[matIdx];
            if(tex >= 0)
                mf.texture = mat.textures.get(tex);
        }
        mf.extraLight = extraLight;
        mf.geo = geo;
        mf.light = light;
        mf.normal = normal;
        mf.type = type;
        return mf;
    }

    public void exportWavefront(File f, GrimModel model, ColorMap colorMap) throws IOException {
        // Todo: export MTL
        String name = f.getName();
        int idx = name.lastIndexOf('.');
        if(idx != -1)
            name = name.substring(0, idx);
        File mtlF = new File(f.getParentFile(), name + ".mtl");
        Map<Material, String> materialNames = writeMtl(mtlF, model, colorMap);
        PrintStream ps = new PrintStream(f);
        _vertexOff = 1;
        ps.println("o " + name);
        ps.println("mtllib " + name + ".mtl");
        printNode(model, model.hierarchy.get(0), new Vector3f(0, 0, 0), ps, materialNames);
        ps.close();
    }

    private Map<Material,String> writeMtl(File dest, GrimModel model, ColorMap map) throws IOException {
        PrintStream ps = new PrintStream(dest);
        Set<Material> materials = new HashSet<>();
        Map<Material, String> materialNames = new HashMap<>();
        // Find all the materials...
        for(ModelNode node: model.hierarchy) {
            if(node.mesh == null)
                continue;
            for(MeshFace face: node.mesh.faces) {
                if(face.material == null)
                    continue;
                materials.add(face.material);
            }
        }
        // Write all the materials & textures...
        for(Material mat: materials) {
            String name = mat.getName();
            int idx = name.lastIndexOf('.');
            if(idx != -1)
                name = name.substring(0, idx);
            materialNames.put(mat, name);
            ps.println("newmtl " + name);
            ps.println("Kd 1 1 1");
            ps.println("Ka 1 1 1");
            ps.println("illum 1");
            Texture t = mat.textures.get(0);
            BufferedImage bi = t.render(map);
            String texName = name + ".png";
            ImageIO.write(bi, "PNG", new File(dest.getParentFile(), texName));
            ps.println("map_Ka " + texName);
            ps.println("map_Kd " + texName);
            ps.println();
        }
        ps.close();
        return materialNames;
    }

    private void printNode(GrimModel model, ModelNode node, Vector3f offset, PrintStream ps, Map<Material,String> matNames) {
        Vector3f nodeOffset = offset.add(node.pos);

        // The mesh is offset by the pivot
        if(node.mesh != null)
            printMesh(model, node.mesh, nodeOffset.add(node.pivot), ps, matNames);
        if(node.child != null)
            printNode(model, node.child, nodeOffset, ps, matNames);
        if(node.sibling != null)
            printNode(model, node.sibling, offset, ps, matNames);
    }

    private int _vertexOff;
    private void printMesh(GrimModel model, Mesh mesh, Vector3f offset, PrintStream ps, Map<Material,String> matNames) {
        ps.println("g " + mesh.name);

        for(MeshFace face: mesh.faces) {
            Texture tex = null;
            if(face.texture != null)
                tex = face.texture;
            for (int i = 0; i < face.vertices.size(); i++) {
                Vector3f v = face.vertices.get(i);
                Vector3f n = face.normals.get(i);
                Vector2f t = face.uv.get(i);
                t = new Vector2f(t.x, -t.y);
                if(tex != null)
                    t = t.div(new Vector2f(tex.width, tex.height)); // map to 0-1 range...
                v = v.add(offset);
                ps.println("v " + v.x + " " + v.z + " " + v.y);
                ps.println("vn " + n.x + " " + n.z + " " + n.y);
                ps.println("vt " + t.x + " " + t.y);
            }
            if(face.material != null)
                ps.println("usemtl " + matNames.get(face.material));
            ps.print("f ");
            for(int i = 0; i < face.vertices.size(); i++) {
                ps.print(" " + _vertexOff + "/" + _vertexOff + "/" + _vertexOff);
                _vertexOff++;
            }
            ps.println();
        }
    }

    public DataSource write(GrimModel source) throws IOException {
        throw new UnsupportedOperationException(); // Todo: write encoder...
    }

    public String[] getFileExtensions() {
        return new String[]{"3do"};
    }

    public Class<GrimModel> getEntryClass() {
        return GrimModel.class;
    }
}