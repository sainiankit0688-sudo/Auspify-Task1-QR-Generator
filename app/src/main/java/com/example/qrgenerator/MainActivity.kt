package com.example.qrgenerator

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var qrBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etInput = findViewById<EditText>(R.id.etInput)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val ivQR = findViewById<ImageView>(R.id.ivQR)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnShare = findViewById<Button>(R.id.btnShare)

        btnGenerate.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Pehle kuch likho", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val encoder = BarcodeEncoder()
                qrBitmap = encoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 600, 600)
                ivQR.setImageBitmap(qrBitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener {
            val bitmap = qrBitmap
            if (bitmap == null) {
                Toast.makeText(this, "Pehle QR Generate karo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveToGallery(bitmap)
        }

        btnShare.setOnClickListener {
            val bitmap = qrBitmap
            if (bitmap == null) {
                Toast.makeText(this, "Pehle QR Generate karo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shareQR(bitmap)
        }
    }

    private fun saveToGallery(bitmap: Bitmap) {
        val fileName = "QR_${System.currentTimeMillis()}.png"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    Toast.makeText(this, "Gallery me save ho gaya", Toast.LENGTH_SHORT).show()
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(this, "Save ho gaya: ${file.path}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Save fail: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQR(bitmap: Bitmap) {
        try {
            val file = File(cacheDir, "shared_qr.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(this, "com.example.qrgenerator.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "QR Share karo"))
        } catch (e: Exception) {
            Toast.makeText(this, "Share fail: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}