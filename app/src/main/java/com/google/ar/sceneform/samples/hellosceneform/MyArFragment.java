package com.google.ar.sceneform.samples.hellosceneform;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.animation.LinearInterpolator;
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
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
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
        Bitmap hydrogen = loadImage("H.jpg");
        Bitmap oxygen = loadImage("O.jpg");
        //Bitmap cl = loadImage("Cl.jpg");
        //Bitmap na = loadImage("Na.jpg");

        //imageDatabase.addImage("earth", earth);
        //imageDatabase.addImage("oxygen", oxygen);
        //imageDatabase.addImage("hydrogen", hydrogen);
        imageDatabase.addImage("H", hydrogen);
        imageDatabase.addImage("O", oxygen);

        config.setAugmentedImageDatabase(imageDatabase);

        session.configure(config);
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private AugmentedImageNode hydrogen;
    private AugmentedImageNode oxygen;

    // https://stackoverflow.com/a/52396327
    private static ObjectAnimator createAnimator() {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        Quaternion orientation1 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0);
        Quaternion orientation2 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 120);
        Quaternion orientation3 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 240);
        Quaternion orientation4 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 360);

        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4);

        // Next, give it the localRotation property.
        orbitAnimation.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        //  Allow orbitAnimation to repeat forever
//        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
//        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }

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
                    AugmentedImageNode node = null;

                    if(img.getName().equals("H") && hydrogen == null) {
                        node = new AugmentedImageNode(this.getContext(), R.raw.hydrogen);
                        hydrogen = node;
                    } else if (img.getName().equals("O") && oxygen == null) {
                        node = new AugmentedImageNode(this.getContext(), R.raw.oxygen);
                        oxygen = node;
                    }

                    if (node != null) {
                        node.setImage(img);
                        getArSceneView().getScene().addChild(node);
                    }
                }
            }

            if(anchor != null && hosting) {
                Anchor.CloudAnchorState state = anchor.getCloudAnchorState();
                hosting = !(state.isError() || state == Anchor.CloudAnchorState.SUCCESS);
            }

            if(!overlapped) {
                if (hydrogen != null && oxygen != null) {
                    Node probablyOxygen = getArSceneView().getScene().overlapTest(hydrogen.node);
                    if (probablyOxygen == oxygen.node) {
                        Vector3 center = Vector3.add(oxygen.getWorldPosition(), hydrogen.getWorldPosition());
                        center.set(center.x / 2, center.y / 2, center.z / 2);

                        ModelRenderable.builder().
                                setSource(getContext(), R.raw.water).
                                build().thenAccept(renderable -> {
                            Node water = new Node();
                            water.setParent(hydrogen);
                            water.setWorldPosition(center);

                            // ANIMATION
                            Vector3 poseHydrogen = hydrogen.getWorldPosition();
                            Vector3 poseOxygen = oxygen.getWorldPosition();

                            oxygen.node.setParent(water);
                            hydrogen.node.setParent(water);

                            oxygen.node.setWorldPosition(poseOxygen);
                            hydrogen.node.setWorldPosition(poseHydrogen);

                            ObjectAnimator rot = createAnimator();
                            rot.setTarget(water);
                            rot.setDuration(5000); // ms
                            rot.start();
                            // ANIMATION //

                            water.setRenderable(renderable);
                            hydrogen.node.setRenderable(null);
                            oxygen.node.setRenderable(null);
                        });
                        overlapped = true;
                    }
                }
            }

        }
    }

}
