package com.example.offlinemap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // טעינת הגדרות תצורת הבסיס של המערכת
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx))

        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // העתקת קובץ המפה מתיקיית ה-assets לזיכרון הפנימי בהפעלה הראשונה
        val mapFile = copyMapFileToInternalStorage()

        if (mapFile != null && mapFile.exists()) {
            // טעינת קובץ המפה הלא-מקוון (Mapsforge)
            val tileSource = MapsforgeTileSource.createFromFiles(arrayOf(mapFile))
            if (tileSource != null) {
                map.setTileSource(tileSource)
            }
        }

        // הגדרת מיקום התחלתי במרכז הארץ ורמת תצוגה
        val mapController = map.controller
        mapController.setZoom(11.0)
        val startPoint = GeoPoint(32.0853, 34.7818)
        mapController.setCenter(startPoint)

        checkPermissions()
    }

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
