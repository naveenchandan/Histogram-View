package android.view.histogram

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileInputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkbox_luma.setOnCheckedChangeListener { _, isChecked ->
            histogram_view.intensityPixelHistogram = isChecked
        }
        checkbox_red.setOnCheckedChangeListener { _, isChecked ->
            histogram_view.redPixelHistogram = isChecked
        }
        checkbox_green.setOnCheckedChangeListener { _, isChecked ->
            histogram_view.greenPixelHistogram = isChecked
        }
        checkbox_blue.setOnCheckedChangeListener { _, isChecked ->
            histogram_view.bluePixelHistogram = isChecked
        }
        checkbox_fill.setOnCheckedChangeListener { _, isChecked ->
            histogram_view.fillHistogram = isChecked
        }

        button_open_image.setOnClickListener {
            Log.d("MainActivity", "onCreate: ")
            if (isPermissionsGranted(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
                pickImageFromGallery()
            } else {
                requestForPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    RC_READ_PERMISSION
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                try {
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                    val fileInputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
                    val bitmap = BitmapFactory.decodeStream(fileInputStream)
                    image_preview.setImageBitmap(bitmap)
                    histogram_view.setBitmap(bitmap)
                    fileInputStream.close()
                    parcelFileDescriptor.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_READ_PERMISSION
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pickImageFromGallery()
        }
    }

    private fun isPermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    @Suppress("SameParameterValue")
    private fun requestForPermissions(
        permissions: Array<String>,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    private fun pickImageFromGallery() {
        startActivityForResult(
            Intent(
                Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).apply {
                type = "image/*"
            }, RC_IMAGE_PICK
        )
    }

    companion object {
        private const val RC_READ_PERMISSION = 1
        private const val RC_IMAGE_PICK = 2
    }

}