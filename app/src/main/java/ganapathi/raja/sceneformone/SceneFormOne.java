package ganapathi.raja.sceneformone;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;

public class SceneFormOne extends AppCompatActivity {

    ArFragment arFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> addObject(Uri.parse("model.sfb")));

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneForm);
    }

    private void addObject(Uri parse) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        Point point = getScreenCenter();
        if (frame != null) {
            List<HitResult> hits = frame.hitTest((float) point.x, (float) point.y);

            for (int i = 0; i < hits.size(); i++) {
                Trackable trackable = hits.get(i).getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hits.get(i).getHitPose()))
                    placeObject(arFragment, hits.get(i).createAnchor(), parse);
            }
        }
    }

    private Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }

    private void placeObject(final ArFragment fragment, final Anchor createAnchor, Uri model) {

        ModelRenderable.builder().setSource(fragment.getContext(), model).build().thenAccept(it -> {
            if (it != null)
                SceneFormOne.this.addNode(arFragment, createAnchor, it);
        }).exceptionally(it -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SceneFormOne.this);
            builder.setMessage(it.getMessage()).setTitle("error!");
            AlertDialog dialog = builder.create();
            dialog.show();
            return null;
        });
    }

    private void addNode(ArFragment fragment, Anchor createAnchor, ModelRenderable renderable) {
        AnchorNode anchorNode = new AnchorNode(createAnchor);
        TransformableNode transformableNode = new TransformableNode(fragment.getTransformationSystem());
        transformableNode.setRenderable(renderable);
        transformableNode.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }
}
