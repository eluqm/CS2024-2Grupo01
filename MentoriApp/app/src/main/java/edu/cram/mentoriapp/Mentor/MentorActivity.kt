package edu.cram.mentoriapp.Mentor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cram.mentoriapp.R

class MentorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewMentor)

        val navFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_mentor) as NavHostFragment

        NavigationUI.setupWithNavController(bottomNav, navFragment.navController)

    }
}