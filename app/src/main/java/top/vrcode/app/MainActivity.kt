package top.vrcode.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import top.vrcode.app.databinding.ActivityMainBinding
import com.termux.shared.termux.TermuxUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}