package com.google.ar.sceneform.samples.hellosceneform;

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

public class MyArFragment extends ArFragment {
    public Anchor anchor;
    public static String TAG = MyArFragment.class.getSimpleName();
    public boolean hosting = false;

    @Override
    public void onResume() {
        super.onResume();
        InputStream inputStream = null;

        Session session = getArSceneView().getSession();

        try {
            inputStream = this.getContext().getAssets().open("example.imgdb");
            AugmentedImageDatabase imageDatabase = AugmentedImageDatabase.deserialize(session, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Config config = new Config(session);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        session.configure(config);
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        Session session = getArSceneView().getSession();
        if (session != null && anchor != null && hosting) {
            try {
                Frame frame = session.update();
                frame.getCamera().getTrackingState();
                Anchor.CloudAnchorState state = anchor.getCloudAnchorState();
                if(state.isError()){
                    Log.e(TAG, "Error: " + state);
                    Toast.makeText(this.getContext(), "Error", Toast.LENGTH_LONG).show();
                    hosting = false;

                }else if(state == Anchor.CloudAnchorState.SUCCESS){
                    Log.e(TAG, "ID: " + anchor.getCloudAnchorId());
                    Toast.makeText(this.getContext(), "Host successfull", Toast.LENGTH_LONG).show();
                    hosting = false;

                    // Augmented images
                    Collection<AugmentedImage> updatedAugmentedImages =
                            frame.getUpdatedTrackables(AugmentedImage.class);

                    for (AugmentedImage img : updatedAugmentedImages) {
                        // Developers can:
                        // 1. Check tracking state.
                        // 2. Render something based on the pose, or attach an anchor.
                        if (img.getTrackingState() == TrackingState.TRACKING) {
                            // You can also check which image this is based on getName().
//                            if (img.getIndex() == dogIndex) {
//                                // TODO: Render a 3D version of a dog in front of img.getCenterPose().
//                            } else if (img.getIndex() == catIndex) {
//                                // TODO: Render a 3D version of a cat in front of img.getCenterPose().
//                            }
                            AugmentedImageNode node = new AugmentedImageNode(this.getContext(), "jetengine.sfa");
                            node.setImage(img);
                            getArSceneView().getScene().addChild(node);
                        }
                    }
                }

            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

}
