package edu.cram.mentoriapp.Mentoriado

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cram.mentoriapp.R

class MentoriadoActivity : AppCompatActivity() {
    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentoriado)

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewMentoriado)

        val navFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_mentoriado) as NavHostFragment

        NavigationUI.setupWithNavController(bottomNav, navFragment.navController)
    }
}