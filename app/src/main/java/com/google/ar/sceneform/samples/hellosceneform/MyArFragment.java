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
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

public class MyArFragment extends ArFragment {
    public Anchor anchor;
    public static String TAG = MyArFragment.class.getSimpleName();
    public boolean hosting = false;

    private Bitmap loadImage(String name) {
        Bitmap bitmap = null;
        try (InputStream inputStream = this.getContext().getAssets().open("augmented-images-earth.jpg")) {
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
        Bitmap earth = loadImage("augmented-images-earth.jpg");
        Bitmap hydrogen = loadImage("H.jpg");
        Bitmap oxygen = loadImage("O.jpg");
        Bitmap cl = loadImage("Cl.jpg");
        Bitmap na = loadImage("na.jpg");

        imageDatabase.addImage("earth", earth);
        imageDatabase.addImage("oxygen", oxygen);
        imageDatabase.addImage("hydrogen", hydrogen);
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
                    if (!trackedAugImgs.contains(img)) {
                        AugmentedImageNode node = new AugmentedImageNode(this.getContext(), R.raw.hydrogen);
                        node.setImage(img);
                        getArSceneView().getScene().addChild(node);
                    }
                }
            }
        }
    }

}
