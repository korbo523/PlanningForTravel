package com.example.planningfortravel

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(){
    private val listFragment = ListFragment()
    private val mapFragment = MapFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, listFragment, "list")
                .commit()
        }

        findViewById<BottomNavigationView>(R.id.bottom_nav).setOnItemSelectedListener {item : MenuItem ->
            when (item.itemId) {
                R.id.nav_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, listFragment, "list")
                        .commit()
                    true
                }
                R.id.nav_map -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, mapFragment, "map")
                        .commit()
                    true
                }
                else -> false
            }

        }
    }
}