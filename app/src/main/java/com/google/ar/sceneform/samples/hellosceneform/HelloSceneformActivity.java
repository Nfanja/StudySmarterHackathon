/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Plane.Type;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
    private static final String TAG = HelloSceneformActivity.class.getSimpleName();

    private MyArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ViewRenderable noteRenderable;
    public TransformableNode andy;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ux);
        arFragment = (MyArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        Button button = findViewById(R.id.button);
        button.setVisibility(View.GONE);

        EditText editText = findViewById(R.id.editText2);
        editText.setVisibility(View.GONE);

        CompletableFuture<ViewRenderable> noteStage = ViewRenderable.builder().
                setView(this, R.layout.note_view).build();

        CompletableFuture.allOf(noteStage).handle(
                (notUsed, throwable) -> {
                    if (throwable != null) {
                        DemoUtils.displayError(this, "Unable to load note", throwable);
                        return null;
                    }
                    try {
                        noteRenderable = noteStage.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        DemoUtils.displayError(this, "Unable to load note", ex);
                    }
                    return null;
                });


        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.jetengine)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });


        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    if (plane.getType() != Type.HORIZONTAL_UPWARD_FACING) {
                        return;
                    }

                    // Create the Anchor.
                    arFragment.anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(arFragment.anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Session session = arFragment.getArSceneView().getSession();
                    arFragment.anchor = session.hostCloudAnchor(arFragment.anchor);

                    // Create the transformable andy and add it to the anchor.
                    andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                    andy.setOnTapListener(
                            (HitTestResult hr, MotionEvent me) -> {
                                button.setVisibility(View.VISIBLE);
                                editText.setVisibility(View.VISIBLE);
                                andy.addChild(addNote());
                            }
                    );
                }
        );
    }

    public static final String EXTRA_MESSAGE = "com.google.ar.MESSAGE";

    public void sendMessage(View view) {
        EditText editText = findViewById(R.id.editText2);
        String message = editText.getText().toString();
        TextView textView = noteRenderable.getView().findViewById(R.id.noteText);
        textView.setText(message);
    }

    protected Node addNote() {
        Node base = new Node();
        Node noteToAdd = new Node();
        noteToAdd.setParent(base);
        noteToAdd.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));
        noteToAdd.setRenderable(noteRenderable);
        return base;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
