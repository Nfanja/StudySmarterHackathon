package com.google.ar.sceneform.samples.hellosceneform.LineDrawing;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.Vertex;

import java.util.ArrayList;

public class Cylinder {
    private final float radius = 0.01f;
    private final int nVertices = 5;
    private final double da = Math.PI / (nVertices - 1);
    private ModelRenderable model;
    public Node node = new Node();

    public Cylinder (Vector3 start, Vector3 end, Material material) {
        Vector3 heightVec = Vector3.subtract(end, start);

        float hh = (float) (heightVec.length() * 0.5);

        ArrayList<Vertex> vertices = new ArrayList<>();

        double a = 0;
        for (int i = 0; i < nVertices; i++, a += da) {
            float c = (float) (radius * Math.cos(a));
            float s = (float) (radius * Math.sin(a));

            Vector3 v1 = new Vector3(c, s, hh);
            Vector3 v2 = new Vector3(c, s, -hh);

            vertices.add(
                    Vertex.builder()
                        .setPosition(v1)
                        .build()
            );

            vertices.add(
                    Vertex.builder()
                            .setPosition(v2)
                            .build()
            );
        }

        ArrayList<RenderableDefinition.Submesh> submeshes = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            ArrayList<Integer> triangleIndices = new ArrayList<>();

            triangleIndices.add(i);
            triangleIndices.add((i + 1) / vertices.size());
            triangleIndices.add((i + 2) / vertices.size());

            RenderableDefinition.Submesh submesh =  RenderableDefinition.Submesh.builder()
                    .setName("Submesh" + i)
                    .setTriangleIndices(triangleIndices)
                    .setMaterial(material)
                    .build();

            submeshes.add(submesh);
        }

        RenderableDefinition definition = RenderableDefinition.builder()
                .setVertices(vertices)
                .setSubmeshes(submeshes)
                .build();

        ModelRenderable.builder()
                .setSource(definition)
                .build()
                .thenAccept(renderable -> {
                    model = renderable;
                    node.setRenderable(renderable);
                });

    }
}
