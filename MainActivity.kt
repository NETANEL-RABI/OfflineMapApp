package com.example.offlinemap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.mapsforge.mapsforge.MapsforgeTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var txtInstruction: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // הגדרת תצורת Osmdroid לעבודה מקומית
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx))

        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        txtInstruction = findViewById(R.id.txtInstruction)

        map.setMultiTouchControls(true)

        // שלב 1: העתקת קובץ המפה מ-assets לזיכרון המכשיר (אם הוא עדיין לא הועתק)
        val mapFile = copyMapFileToInternalStorage()

        if (mapFile != null && mapFile.exists()) {
            // שלב 2: הגדרת מקור המפה לקובץ ה-Mapsforge המקומי (.map)
            val tileSource = MapsforgeTileSource.createFromFiles(arrayOf(mapFile))
            if (tileSource != null) {
                map.setTileSource(tileSource)
            }
        }

        // הגדרת מיקום התחלתי ורמת זום
        val mapController = map.controller
        mapController.setZoom(11.0)
        val startPoint = GeoPoint(32.0853, 34.7818) // מרכז הארץ / תל אביב
        mapController.setCenter(startPoint)

        checkPermissions()
    }

    /**
     * פונקציה המעתיקה את הקובץ מתיקיית assets/maps/israel.map לתיקיית הקבצים הפנימית של האפליקציה
     */
    private fun copyMapFileToInternalStorage(): File? {
        val file = File(filesDir, "israel.map")
        try {
            if (!file.exists()) {
                assets.open("maps/israel.map").use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
