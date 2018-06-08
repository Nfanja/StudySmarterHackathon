package com.google.ar.sceneform.samples.hellosceneform.LineDrawing;

import android.util.Log;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;

public class Line {
    private ArrayList<Vector3> points = new ArrayList<>();
    private Material material;

    public Line(Node parentNode, Material material) {
        this.material = material;

    }

    public void add(Vector3 point, NodeParent parent) {
        points.add(point);

        if (points.size() > 1) {
            Vector3 start = points.get(points.size() - 2);
            Vector3 end = points.get(points.size() - 1);
            Vector3 vec = Vector3.subtract(end, start);

            Vector3 mid = new Vector3((end.x - start.x) / 2, (end.y - start.y) / 2, (end.z - start.z) / 2);

            Node node = new Node();
            node.setParent(parent);
            node.setRenderable(ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, 0.01f), end, material));
             // node.setWorldPosition(mid);
        }
    }
}
