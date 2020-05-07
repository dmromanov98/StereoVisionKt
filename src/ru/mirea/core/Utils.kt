package ru.mirea.core

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.net.URISyntaxException
import java.nio.file.Paths

class Utils {
    companion object {
        /**
         * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
         *
         * @param frame the [Mat] representing the current frame
         * @return the [Image] to show
         */

        fun mat2Image(frame: Mat): Image? {
            return try {
                SwingFXUtils.toFXImage(matToBufferedImage(frame), null)
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println("Cannot convert the Mat obejct: $e")
                null
            }
        }

        /**
         * Generic method for putting element running on a non-JavaFX thread on the
         * JavaFX thread, to properly update the UI
         *
         * @param property a [ObjectProperty]
         * @param value    the value to set for the given [ObjectProperty]
         */
        fun <T> onFXThread(property: ObjectProperty<T>, value: T) {
            Platform.runLater { property.set(value) }
        }

        /**
         * Support for the [] method
         *
         * @param original the [Mat] object in BGR or grayscale
         * @return the corresponding [BufferedImage]
         */
        private fun matToBufferedImage(original: Mat): BufferedImage? {
            val width = original.width()
            val height = original.height()
            val channels = original.channels()
            val sourcePixels = ByteArray(width * height * channels)
            original[0, 0, sourcePixels]
            val image = if (original.channels() > 1) {
                BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
            } else {
                BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
            }
            val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
            System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.size)
            return image
        }

        fun getScaledImage(mat: Mat, w: Int, h: Int): Mat? {
            val newMat = Mat()
            val iNewMat = 0
            val jNewMat = 0
            newMat.create(w, h, CvType.CV_8UC3)
            val hStep = mat.height() / h
            val wStep = mat.width() / w
            var i = 0
            while (i < mat.height()) {
                var j = 0
                while (j < mat.width()) {
                    val newPixel = DoubleArray(3)
                    newPixel[0] =
                        ((mat[i, j][0] + mat[i + 1, j][0] + mat[i, j + 1][0] + mat[i + 1, j + 1][0]) / (hStep + wStep)).toInt()
                            .toDouble()
                    newPixel[1] =
                        ((mat[i, j][1] + mat[i + 1, j][1] + mat[i, j + 1][1] + mat[i + 1, j + 1][1]) / (hStep + wStep)).toInt()
                            .toDouble()
                    newPixel[2] =
                        ((mat[i, j][2] + mat[i + 1, j][2] + mat[i, j + 1][2] + mat[i + 1, j + 1][2]) / (hStep + wStep)).toInt()
                            .toDouble()
                    newMat.put(iNewMat, jNewMat, *newPixel)
                    println(newPixel[0].toString() + " " + newPixel[1] + " " + newPixel[2])
                    j += wStep
                }
                i += hStep
            }
            return newMat
        }


        fun getScaledImage(img: Image?, width: Int, height: Int): Image? {
            val bufImage: BufferedImage = SwingFXUtils.fromFXImage(img, null)
            val swImage = bufImage.getScaledInstance(width, height, 1)
            val tmp = swImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)
            val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2d = resized.createGraphics()
            g2d.drawImage(tmp, 0, 0, null)
            g2d.dispose()
            return SwingFXUtils.toFXImage(resized, null)
        }

        fun bufferedImage2Mat(`in`: BufferedImage): Mat? {
            val out: Mat
            val data: ByteArray
            var r: Int
            var g: Int
            var b: Int
            val height = `in`.height
            val width = `in`.width
            if (`in`.type == BufferedImage.TYPE_INT_RGB || `in`.type == BufferedImage.TYPE_INT_ARGB) {
                out = Mat(height, width, CvType.CV_8UC3)
                data = ByteArray(height * width * out.elemSize().toInt())
                val dataBuff = `in`.getRGB(0, 0, width, height, null, 0, width)
                for (i in dataBuff.indices) {
                    data[i * 3 + 2] = (dataBuff[i] shr 16 and 0xFF).toByte()
                    data[i * 3 + 1] = (dataBuff[i] shr 8 and 0xFF).toByte()
                    data[i * 3] = (dataBuff[i] shr 0 and 0xFF).toByte()
                }
            } else {
                out = Mat(height, width, CvType.CV_8UC1)
                data = ByteArray(height * width * out.elemSize().toInt())
                val dataBuff = `in`.getRGB(0, 0, width, height, null, 0, width)
                for (i in dataBuff.indices) {
                    r = (dataBuff[i] shr 16 and 0xFF)
                    g = (dataBuff[i] shr 8 and 0xFF)
                    b = (dataBuff[i] shr 0 and 0xFF)
                    data[i] = (0.21 * r + 0.71 * g + 0.07 * b).toByte() //luminosity
                }
            }
            out.put(0, 0, data)
            return out
        }

        fun getOpenCvResource(clazz: Class<*>, path: String?): String? {
            return try {
                Paths.get(clazz.getResource(path).toURI()).toString()
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }

        // Convert image to Mat
        // alternate version http://stackoverflow.com/questions/21740729/converting-bufferedimage-to-mat-opencv-in-java
        fun bufferedImage2MatV2(im: BufferedImage): Mat? {
            var im = im
            im = toBufferedImageOfType(im, BufferedImage.TYPE_3BYTE_BGR)

            // Convert INT to BYTE
            //im = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            // Convert bufferedimage to byte array
            val pixels = (im.raster.dataBuffer as DataBufferByte).data

            // Create a Matrix the same size of image
            val image = Mat(im.height, im.width, CvType.CV_8UC3)
            // Fill Matrix with image values
            image.put(0, 0, pixels)
            return image
        }

        private fun toBufferedImageOfType(original: BufferedImage?, type: Int): BufferedImage {
            requireNotNull(original) { "original == null" }

            // Don't convert if it already has correct type
            if (original.type == type) {
                return original
            }

            // Create a buffered image
            val image = BufferedImage(original.width, original.height, type)

            // Draw the image onto the new buffer
            val g = image.createGraphics()
            try {
                g.composite = AlphaComposite.Src
                g.drawImage(original, 0, 0, null)
            } finally {
                g.dispose()
            }
            return image
        }
    }
}