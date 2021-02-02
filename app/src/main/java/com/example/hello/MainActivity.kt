package com.example.hello

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.File

class MainActivity : AppCompatActivity() {
    private val TAKE_PICTURE = 1
    private lateinit var imageUri : Uri //uri lokasi dari foto
    private lateinit var takePhotoButton : Button
    private lateinit var output : File //file foto
    val fileName = "CameraDemo.jpg" //nama file
    private val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider" //provider (nda terlalu paham juga)
    private lateinit var image: ImageView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        find view by id seperti biasa
        takePhotoButton = findViewById(R.id.takePhotoButton)
        image = findViewById(R.id.imageView)
        recyclerView = findViewById(R.id.recyclerView)

//        ambil foto
        takePhotoButton.setOnClickListener {
            output = File(File(filesDir, "photos"), fileName)
            if (output.exists()) {
                output.delete()
            }
            else {
                output.parentFile.mkdirs()
            }

            //intent kamera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            imageUri = FileProvider.getUriForFile(this, AUTHORITY, output)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, TAKE_PICTURE)
        }
    }

//    ketika sudah selesai foto
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TAKE_PICTURE -> if (resultCode == Activity.RESULT_OK) {
//                pilih uri
                val selectedImage = imageUri
                contentResolver.notifyChange(selectedImage, null)
                val cr = contentResolver

//                jadiin bitmap biar bisa di proses
                var bitmap: Bitmap
                try {
                    //ambil foto
                    bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage)
                    processImage(bitmap)

                } catch (e: Exception) {
                    Toast.makeText(this, "Failed : " + e.message, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    //proses foto
    private fun processImage(bitmap: Bitmap) {
        //jadiin bitmap ke tensorimage
        val tensorImage = TensorImage.fromBitmap(bitmap)

        //buat object detector dan detect dari tensorimage
        val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(4)
                .setScoreThreshold(0.5f)
                .build()
        val detector = ObjectDetector.createFromFileAndOptions(this, "detect.tflite", options)
        val results = detector.detect(tensorImage)

//        buat canvas dan paint biar bitmap bisa dicoret2 pake kotak2 sama bikin
        val bitmapNew = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmapNew)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 10.0f
        }

        //taruh gambar asal di canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        //adapter buat list deteksi
        val adapter = DetectionAdapter(results)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

//        gambar kotak2 deteksi
        for (detection : Detection in results) {
            canvas.drawRect(detection.boundingBox, paint)
        }

//        set imageview dengan foto kanvas
        image.setImageDrawable(BitmapDrawable(resources, bitmapNew))
    }
}