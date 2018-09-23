package com.google.ar.sceneform.samples.hellosceneform;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.MathHelper;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MyArFragment extends ArFragment {
    public Anchor anchor;
    public static String TAG = MyArFragment.class.getSimpleName();
    public boolean hosting = false;
    public boolean overlapped = false;

    private Bitmap loadImage(String name) {
        Bitmap bitmap = null;
        try (InputStream inputStream = this.getContext().getAssets().open(name)) {
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "I/O exception loading augmented image bitmap.", e);
        }

        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        AugmentedImageDatabase imageDatabase;

        Session session = getArSceneView().getSession();

        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);

        imageDatabase = new AugmentedImageDatabase(session);
        //Bitmap earth = loadImage("augmented-images-earth.jpg");
        //Bitmap hydrogen = loadImage("H.jpg");
        //Bitmap oxygen = loadImage("O.jpg");
        Bitmap cl = loadImage("Cl.jpg");
        Bitmap na = loadImage("Na.jpg");

        //imageDatabase.addImage("earth", earth);
        //imageDatabase.addImage("oxygen", oxygen);
        //imageDatabase.addImage("hydrogen", hydrogen);
        imageDatabase.addImage("Cl", cl);
        imageDatabase.addImage("Na", na);

        config.setAugmentedImageDatabase(imageDatabase);

        session.configure(config);
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private HashSet<AugmentedImage> trackedAugImgs = new HashSet<>();

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        Session session = getArSceneView().getSession();
        if (session != null) {
            Frame frame = getArSceneView().getArFrame();
            Collection<AugmentedImage> updatedAugmentedImages =
                    frame.getUpdatedTrackables(AugmentedImage.class);

            for (AugmentedImage img : updatedAugmentedImages) {
                if (img.getTrackingState() == TrackingState.TRACKING) {
                    Toast.makeText(getContext(), img.getName(), Toast.LENGTH_SHORT).show();
                    if (!trackedAugImgs.contains(img)) {
                        int resource = -1;
                        if(img.getName().equals("Cl")) {
                            resource = R.raw.hydrogen;
                        } else if (img.getName().equals("Na")) {
                            resource = R.raw.oxygen;
                        }

                        AugmentedImageNode node = new AugmentedImageNode(this.getContext(), resource);
                        node.setImage(img);
                        getArSceneView().getScene().addChild(node);
                        trackedAugImgs.add(img);
                    }
                }
            }

            if(anchor != null && hosting) {
                Anchor.CloudAnchorState state = anchor.getCloudAnchorState();
                hosting = !(state.isError() || state == Anchor.CloudAnchorState.SUCCESS);
            }

            if(!overlapped) {
                List<Node> children = getArSceneView().getScene().getChildren();
                //First two anchors are camera and sun
                if (children.size() > 3) {
                    Node node = children.get(2);
                        for (Node child : node.getChildren()) {
                            ArrayList<Node> overlappedNodes = getArSceneView().getScene().overlapTestAll(child);
                            if (!overlappedNodes.isEmpty()) {
                                node.removeChild(child);
                                Toast.makeText(getContext(), "Overlapped", Toast.LENGTH_SHORT).show();
                                Vector3 center = Vector3.add(overlappedNodes.get(0).getWorldPosition(), child.getWorldPosition());
                                center.set(center.x / 2, center.y / 2, center.z / 2);

                                ModelRenderable.builder().
                                        setSource(getContext(), R.raw.water).
                                        build().thenAccept(renderable -> {
                                    Node newNode = new Node();
                                    newNode.setParent(node);
                                    newNode.setLocalPosition(node.worldToLocalDirection(center));
                                    newNode.setRenderable(renderable);
                                });
                                overlapped = true;
                                break;
                            }
                        }
                        if(overlapped) {
                            getArSceneView().getScene().removeChild(children.get(3));
                        }

                }
            }

        }
    }

}
