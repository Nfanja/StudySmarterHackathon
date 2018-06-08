package com.google.ar.sceneform.samples.hellosceneform;

import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

public class MyArFragment extends ArFragment {
    public Anchor anchor;
    public static String TAG = MyArFragment.class.getSimpleName();
    public boolean hosting = true;

    @Override
    public void onResume() {
        super.onResume();
        Session session = getArSceneView().getSession();
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
                }

            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }
}
