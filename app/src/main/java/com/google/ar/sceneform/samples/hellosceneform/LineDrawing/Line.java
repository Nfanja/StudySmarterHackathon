package com.google.ar.sceneform.samples.hellosceneform.LineDrawing;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;

import java.util.ArrayList;

public class Line {
    private ArrayList<Vector3> points = new ArrayList<>();
    private Material material;
    private Node parentNode;

    public Line(Node parentNode, Material material) {
        this.material = material;
        this.parentNode = parentNode;
    }

    public void add(Vector3 point) {
        points.add(point);

        if (points.size() > 1) {
            Vector3 start = points.get(points.size() - 2);
            Vector3 end = points.get(points.size() - 1);

            Cylinder stroke = new Cylinder(start, end, material);
            stroke.node.setParent(this.parentNode);
        }
    }
}
