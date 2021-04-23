package no.realitylab.arinterface

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName
    lateinit var arFragment: ArFragment
    var anchorList = arrayListOf<AnchorModel>()
    var checkIndex = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = sceneform_fragment as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            anchorList.add(AnchorModel(checkIndex, hitResult.createAnchor()))
            placeObject(arFragment, anchorList.lastOrNull()!!)
            checkIndex++
        }
    }

    private fun placeObject(fragment: ArFragment, anchor: AnchorModel) {
        ViewRenderable.builder()
                .setView(fragment.context, R.layout.controls)
                .build()
                .thenAccept {
                    it.isShadowCaster = true
                    it.isShadowReceiver = true
                    it.view.findViewById<Button>(R.id.btnDelete).setOnClickListener { _ ->
                        anchor.anchor.detach()
                        anchorList.remove(anchor)
                        Log.e(TAG, "remove anchor: $anchor list size ${anchorList.size}")
                    }
                    addControlsToScene(fragment, anchor.anchor, it)
                }
                .exceptionally {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(it.message).setTitle("Error")
                    val dialog = builder.create()
                    dialog.show()
                    return@exceptionally null
                }
    }

    private fun addControlsToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
    }
}
