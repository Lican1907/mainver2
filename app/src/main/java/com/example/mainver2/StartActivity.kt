package com.example.mainver2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val buttonUser = findViewById<ImageButton>(R.id.imageButtonUser)
        val buttonCreateProject = findViewById<ImageButton>(R.id.imageButtonCreateProject)
        val buttonMyPorjects = findViewById<ImageButton>(R.id.imageButtonMyProjects)


        buttonUser.setOnClickListener {
            val intent = Intent(this, StartActivitySecondScreen::class.java)
            startActivity(intent)
        }
        buttonCreateProject.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        buttonMyPorjects.setOnClickListener {
            val intent = Intent(this, MyProjects::class.java)
            startActivity(intent)
        }
    }
}
