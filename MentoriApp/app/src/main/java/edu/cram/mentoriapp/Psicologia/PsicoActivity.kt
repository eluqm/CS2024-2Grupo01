package edu.cram.mentoriapp.Psicologia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cram.mentoriapp.R

class PsicoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psico)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewPsico)

        val navFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_psico) as NavHostFragment

        NavigationUI.setupWithNavController(bottomNav, navFragment.navController)
    }
}