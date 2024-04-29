package com.app.imageprocessing

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.app.imageprocessing.R


class MainActivity : AppCompatActivity() {

    private lateinit var captureIv: ImageView
    private lateinit var negativeFilterBtn: Button
    private lateinit var sepiaFilterBtn: Button
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var saveImageBtn: Button
    private lateinit var convertToGrayBtn: Button
    private lateinit var seekBarContrast: SeekBar
    private lateinit var edgeDetectionBtn: Button
    private lateinit var removeFiltersBtn: Button
    private var contrastValue = 0
    private lateinit var imageUrl: Uri
    private var brightnessValue = 0
    private val cameraClass = Camera(this)
    private var isGrayScale = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edgeDetectionBtn = findViewById(R.id.edgeDetectionBtn)
        removeFiltersBtn = findViewById(R.id.removeFiltersBtn)
        seekBarContrast = findViewById(R.id.seekBarContrast)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        sepiaFilterBtn = findViewById(R.id.sepiaFilterBtn)
        negativeFilterBtn = findViewById(R.id.negativeFilterBtn)
        convertToGrayBtn = findViewById(R.id.convertToGrayBtn)
        saveImageBtn = findViewById(R.id.saveImageBtn)

        saveImageBtn.visibility = View.INVISIBLE
        convertToGrayBtn.visibility = View.INVISIBLE
        negativeFilterBtn.visibility = View.INVISIBLE
        sepiaFilterBtn.visibility = View.INVISIBLE
        seekBarBrightness.visibility = View.INVISIBLE
        seekBarContrast.visibility = View.INVISIBLE
        edgeDetectionBtn.visibility = View.INVISIBLE
        removeFiltersBtn.visibility = View.INVISIBLE

        captureIv = findViewById(R.id.captureImageView)

        val captureImgBtn = findViewById<Button>(R.id.captureImgBtn)
        captureImgBtn.setOnClickListener {
            isGrayScale = false
            imageUrl = cameraClass.createImageUri()
            contract.launch(imageUrl)

            saveImageBtn.visibility = View.INVISIBLE
            convertToGrayBtn.visibility = View.INVISIBLE
            negativeFilterBtn.visibility = View.INVISIBLE
            sepiaFilterBtn.visibility = View.INVISIBLE
            seekBarBrightness.visibility = View.INVISIBLE
            seekBarContrast.visibility = View.INVISIBLE
            edgeDetectionBtn.visibility = View.INVISIBLE
            removeFiltersBtn.visibility = View.INVISIBLE
        }

        saveImageBtn.setOnClickListener {
            cameraClass.saveImageToGallery(captureIv, imageUrl, isGrayScale)
        }

        convertToGrayBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            if (isGrayScale) {
                captureIv.setImageBitmap(bitmap)
                isGrayScale = false
            } else {
                val grayBitmap = cameraClass.toGrayscale(bitmap)
                captureIv.setImageBitmap(grayBitmap)
                isGrayScale = true
            }
        }

        negativeFilterBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val negativeBitmap = cameraClass.applyNegativeFilter(bitmap)
            captureIv.setImageBitmap(negativeBitmap)
        }

        sepiaFilterBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val sepiaBitmap = cameraClass.applySepiaFilter(bitmap)
            captureIv.setImageBitmap(sepiaBitmap)
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightnessValue = progress - 100
                applyBrightnessAndContrast(brightnessValue, contrastValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrastValue = progress - 100
                applyBrightnessAndContrast(brightnessValue, contrastValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        edgeDetectionBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val edgeBitmap = cameraClass.applyEdgeDetection(bitmap)
            captureIv.setImageBitmap(edgeBitmap)
        }

        removeFiltersBtn.setOnClickListener {
            captureIv.setImageURI(imageUrl)
            isGrayScale = false
            seekBarBrightness.progress = 100
            seekBarContrast.progress = 100
        }
    }

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            captureIv.setImageURI(null)
            captureIv.setImageURI(imageUrl)
            saveImageBtn.visibility = View.VISIBLE
            convertToGrayBtn.visibility = View.VISIBLE
            negativeFilterBtn.visibility = View.VISIBLE
            sepiaFilterBtn.visibility = View.VISIBLE
            seekBarBrightness.visibility = View.VISIBLE
            seekBarContrast.visibility = View.VISIBLE
            edgeDetectionBtn.visibility = View.VISIBLE
            removeFiltersBtn.visibility = View.VISIBLE
        }
    }
    private fun applyBrightnessAndContrast(brightnessValue: Int, contrastValue: Int) {
        val bitmap = captureIv.drawable.toBitmap()
        val adjustedBitmap = cameraClass.applyBrightnessAndContrast(bitmap, brightnessValue, contrastValue)
        captureIv.setImageBitmap(adjustedBitmap)
    }
}