package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.entry.model.*;
import com.sqrt.liblab.threed.Vector2;
import com.sqrt.liblab.threed.Vector3;

import java.io.*;

public class ModelCodec extends EntryCodec<GrimModel> {
    public GrimModel _read(EntryDataProvider source) throws IOException {
        if (source.readIntLE() != (('M' << 24) | ('O' << 16) | ('D' << 8) | 'L'))
            throw new IOException("Invalid model format (text format W.I.P)"); // Todo
        return loadBinary(source);
    }

    private GrimModel loadBinary(EntryDataProvider source) throws IOException {
        int numMaterials = source.readIntLE();
        Material[] materials = new Material[numMaterials];
        for (int i = 0; i < numMaterials; i++) {
            String name = source.readString(32);
            materials[i] = (Material) source.container.container.findByName(name);
            if(materials[i] == null) {
                System.out.println("No material named: " + name);
            }
        }
        GrimModel model = new GrimModel(source.container, source.getName());
        String name = source.readString(32);
        source.skip(4);
        int numGeosets = source.readIntLE();
        for (int i = 0; i < numGeosets; i++)
            model.geosets.add(loadGeosetBinary(source, materials));
        source.skip(4);
        int numNodes = source.readIntLE();
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
        model.radius = source.readFloat();
        source.skip(36);
        model.off = source.readVector3();
        return model;
    }

    private ModelNode loadModelNodeBinary(EntryDataProvider source, Geoset geoset) throws IOException {
        ModelNode node = new ModelNode();
        node.name = source.readString(64);
        node.flags = source.readIntLE();
        source.skip(4);
        node.type = source.readIntLE();
        int meshNum = source.readIntLE();
        node.mesh = meshNum < 0? null: geoset.meshes.get(meshNum);
        node.depth = source.readIntLE();
        int parentPtr = source.readIntLE();
        int numChildren = source.readIntLE();
        int childPtr = source.readIntLE();
        int siblingPtr = source.readIntLE();
        node.pivot = source.readVector3();
        node.pos = source.readVector3();
        node.pitch = source.readAngle();
        node.yaw = source.readAngle();
        node.roll = source.readAngle();

        source.skip(48);
        ModelNode parent, sibling, child;
        if(parentPtr != 0)
            node.parentIdx = source.readIntLE();
        if(childPtr != 0)
            node.childIdx = source.readIntLE();
        if(siblingPtr != 0)
            node.siblingIdx = source.readIntLE();
        return node;
    }

    private Geoset loadGeosetBinary(EntryDataProvider source, Material[] materials) throws IOException {
        Geoset g = new Geoset();
        int numMeshes = source.readIntLE();
        for (int i = 0; i < numMeshes; i++)
            g.meshes.add(loadMeshBinary(source, materials));
        return g;
    }

    private Mesh loadMeshBinary(EntryDataProvider source, Material[] materials) throws IOException {
        Mesh m = new Mesh();
        m.name = source.readString(32);
        source.skip(4);
        m.geomMode = source.readIntLE();
        m.lightMode = source.readIntLE();
        m.texMode = source.readIntLE();
        int numVertices = source.readIntLE();
        int numTextureVerts = source.readIntLE();
        int numFaces = source.readIntLE();

        Vector3[] vertices = new Vector3[numVertices];
        float[] verticesI = new float[numVertices];
        Vector3[] normals = new Vector3[numVertices];
        Vector2[] textureVerts = new Vector2[numTextureVerts];
        for (int i = 0; i < numVertices; i++)
            vertices[i] =source.readVector3();
        for (int i = 0; i < numTextureVerts; i++)
            textureVerts[i] = source.readVector2();
        for (int i = 0; i < numVertices; i++)
            verticesI[i] = source.readFloat();
        source.skip(numVertices * 4);
        int[][] normalTemp = new int[numFaces][];
        for (int i = 0; i < numFaces; i++)
            m.faces.add(loadMeshFaceBinary(source, materials, vertices, normalTemp, i, textureVerts));
        for (int i = 0; i < numVertices; i++)
            normals[i] = source.readVector3();
        for (int i = 0; i < numFaces; i++) {
            MeshFace mf = m.faces.get(i);
            for (int index : normalTemp[i])
                mf.normals.add(normals[index]);
        }
        m.shadow = source.readIntLE();
        source.skip(4);
        m.radius = source.readFloat();
        source.skip(24);

        return m;
    }

    private MeshFace loadMeshFaceBinary(EntryDataProvider source, Material[] materials,
                                        Vector3[] vertexTable, int[][] normalTemp, int normalOff,
                                        Vector2[] texVertexTable) throws IOException {
        source.skip(4);
        int type = source.readIntLE();
        int geo = source.readIntLE();
        int light = source.readIntLE();
        int tex = source.readIntLE();
        int numVertices = source.readIntLE();
        normalTemp[normalOff] = new int[numVertices];
        source.skip(4);
        int texPtr = source.readIntLE();
        int materialPtr = source.readIntLE();
        source.skip(12);
        float extraLight = source.readFloat();
        source.skip(12);
        Vector3 normal = source.readVector3();
        MeshFace mf = new MeshFace();
        for (int i = 0; i < numVertices; i++) {
            int vid = source.readIntLE();
            mf.vertices.add(vertexTable[vid]);
            normalTemp[normalOff][i] = vid;
        }
        if (texPtr != 0) {
            for (int i = 0; i < numVertices; i++)
                mf.uv.add(texVertexTable[source.readIntLE()]);
        }
        if (materialPtr != 0) {
            int matIdx = source.readIntLE();
            Material mat = materials[matIdx];
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

    private void exportWavefront(File f, GrimModel model) throws FileNotFoundException {
        PrintStream ps = new PrintStream(f);
        _vertexOff = 1;
        printNode(model, model.hierarchy.get(0), new Vector3(0, 0, 0), ps);
        // Todo: export MTL
        ps.close();
    }

    private void printNode(GrimModel model, ModelNode node, Vector3 offset, PrintStream ps) {
        Vector3 nodeOffset = offset.add(node.pos);
        // The mesh is offset by the pivot
        if(node.mesh != null)
            printMesh(model, node.mesh, nodeOffset.add(node.pivot), ps);
        if(node.child != null)
            printNode(model, node.child, nodeOffset, ps);
        if(node.sibling != null)
            printNode(model, node.sibling, offset, ps);
    }

    private int _vertexOff;
    private void printMesh(GrimModel model, Mesh mesh, Vector3 offset, PrintStream ps) {
        ps.println("g " + mesh.name);

        for(MeshFace face: mesh.faces) {
            Texture tex = null;
            if(face.texture != null)
                tex = face.texture;
            for (int i = 0; i < face.vertices.size(); i++) {
                Vector3 v = face.vertices.get(i);
                Vector3 n = face.normals.get(i);
                Vector2 t = face.uv.get(i);
                t = new Vector2(t.x, -t.y);
                if(tex != null)
                    t = t.div(new Vector2(tex.width, tex.height)); // map to 0-1 range...
                v = v.add(offset);
                ps.println("v " + v.x + " " + v.y + " " + v.z);
                ps.println("vn " + n.x + " " + n.y + " " + n.z);
                ps.println("vt " + t.x + " " + t.y);
            }
            ps.print("f ");
            for(int i = 0; i < face.vertices.size(); i++) {
                ps.print(" " + _vertexOff + "/" + _vertexOff + "/" + _vertexOff);
                _vertexOff++;
            }
            ps.println();
        }
    }

    public EntryDataProvider write(GrimModel source) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String[] getFileExtensions() {
        return new String[]{"3do"};
    }

    public byte[][] getFileHeaders() {
        return new byte[][]{{'L', 'D', 'O', 'M'}};
    }

    public Class<GrimModel> getEntryClass() {
        return GrimModel.class;
    }
}