package edu.cram.mentoriapp.Psicologia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cram.mentoriapp.R

class PsicoActivity : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_psico)

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewPsico)

        val navFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_psico) as NavHostFragment

        NavigationUI.setupWithNavController(bottomNav, navFragment.navController)
    }
}