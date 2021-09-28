package top.vrcode.app.errView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import top.vrcode.app.R

// https://github.com/NeoTerm/NeoTerm/blob/236072395ce056d2d2cccf950d3f243f099a178f/app/src/main/java/io/neoterm/frontend/floating/dialog.kt
class AddGraphicalSupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_graphical_support)

        val installBtn = findViewById<Button>(R.id.install_x_wayland)
        installBtn.setOnClickListener {  }
    }

    fun installXWayland() {

    }
}