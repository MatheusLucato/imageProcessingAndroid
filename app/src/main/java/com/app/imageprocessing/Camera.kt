package com.app.imageprocessing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap

class Camera(private val context: Context) {

    private var imageCounter = 0

    fun createImageUri(): Uri {
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${imageCounter++}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        return context.contentResolver.insert(imageCollection, imageDetails)!!
    }

    fun saveImageToGallery(imageView: ImageView, uri: Uri, isGrayScale: Boolean) {
        val bitmap = imageView.drawable.toBitmap()
        val outputStream = context.contentResolver.openOutputStream(uri)!!
        if (isGrayScale) {
            val grayBitmap = toGrayscale(bitmap)
            grayBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        outputStream.close()

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)

        val message = "Imagem salva"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayBitmap
    }

    fun applyNegativeFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val negativeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(negativeBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return negativeBitmap
    }

    fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val sepiaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(sepiaBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return sepiaBitmap
    }

    fun applyBrightnessAndContrast(bitmap: Bitmap, brightnessValue: Int, contrastValue: Int): Bitmap {
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        val canvas = Canvas(adjustedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = (1f + contrastValue / 100f).coerceIn(0f, 2f)
        val brightness = brightnessValue.coerceIn(-255, 255)

        val contrastMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, 0f,
                0f, contrast, 0f, 0f, 0f,
                0f, 0f, contrast, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val brightnessMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness.toFloat(),
                0f, 1f, 0f, 0f, brightness.toFloat(),
                0f, 0f, 1f, 0f, brightness.toFloat(),
                0f, 0f, 0f, 1f, 0f
            ))
        }

        contrastMatrix.preConcat(brightnessMatrix)

        val filter = ColorMatrixColorFilter(contrastMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return adjustedBitmap
    }


    fun applyEdgeDetection(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val edgeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val sobelX = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )

        val sobelY = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )

        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                var pixelX = 0
                var pixelY = 0

                for (i in -1..1) {
                    for (j in -1..1) {
                        val neighborPixel = bitmap.getPixel(x + i, y + j)
                        val neighborGray = (Color.red(neighborPixel) + Color.green(neighborPixel) + Color.blue(neighborPixel)) / 3

                        pixelX += neighborGray * sobelX[i + 1][j + 1]
                        pixelY += neighborGray * sobelY[i + 1][j + 1]
                    }
                }

                var magnitude = Math.sqrt((pixelX * pixelX + pixelY * pixelY).toDouble()).toInt()
                magnitude = if (magnitude > 255) 255 else if (magnitude < 0) 0 else magnitude

                edgeBitmap.setPixel(x, y, Color.rgb(magnitude, magnitude, magnitude))
            }
        }

        return edgeBitmap
    }
}