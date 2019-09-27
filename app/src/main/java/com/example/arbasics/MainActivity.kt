package com.example.arbasics

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {
    override fun onUpdate(p0: FrameTime?) {
        val frame = arScene.arFrame
        val updateAugmentedImage = frame?.getUpdatedTrackables<AugmentedImage>(AugmentedImage::class.java)


        for(augmentedImg in updateAugmentedImage!!){
            if(augmentedImg.name.equals("rat")){
                val node = MyArNode(this,R.raw.rat)
                node.image = augmentedImg
                arScene.scene.addChild(node)
            }
        }
    }

    private var session: Session? = null
    private var shouldConfigreSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity, "Permission camera need", Toast.LENGTH_LONG)
                        .show()
                }

            })
            .check()

        arScene.scene.addOnUpdateListener(this)

    }

    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity, "Permission camera need", Toast.LENGTH_LONG)
                        .show()
                }

            })
            .check()
    }

    override fun onPause() {
        super.onPause()
        if(session!=null)
            session!!.pause()
        arScene.pause()

    }

    private fun setupSession() {
        if (session == null) {
            try {
                session = Session(this)
            } catch (e: UnavailableArcoreNotInstalledException) {
                e.printStackTrace()
            } catch (e: UnavailableApkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableSdkTooOldException) {
                e.printStackTrace()
            }

            shouldConfigreSession = true
        }
        if (shouldConfigreSession) {
            configSession()
            shouldConfigreSession = false
            arScene.setupSession(session)
        }
        try {
            session!!.resume()
            arScene.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            session = null
            return
        }
    }


    private fun configSession(){
        val config = Config(session)
        if(!buildDatabase(config)){
            Toast.makeText(this@MainActivity, "Error Build", Toast.LENGTH_LONG)
                .show()
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session!!.configure(config)
        }
    }

    private fun buildDatabase(config: Config): Boolean{
        val augmentedImageDatabase: AugmentedImageDatabase
        val bitmap = loadBitmapFromAsset()

        if(bitmap == null){
            return false
        }


        augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("rat",bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadBitmapFromAsset(): Bitmap? {
        val inputStream = assets.open("rat.jpeg")
        return BitmapFactory.decodeStream(inputStream)
    }
}
