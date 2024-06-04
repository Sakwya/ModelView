package top.sakwya.modelview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import top.sakwya.modelview.databinding.ActivityMainBinding
import top.sakwya.modelview.viewmodel.SharedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val sharedPref = getSharedPreferences("viewModel", Context.MODE_PRIVATE)
        viewModel.setUseCamera(sharedPref.getBoolean("useCamera",false))
        sharedPref.getString("modelName","city.glb")?.let { viewModel.setModelName(it) }
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        val sharedPref = getSharedPreferences("viewModel", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("useCamera", viewModel.useCamera.value==true)
        editor.apply()
        editor.putString("modelName",viewModel.modelName.value)
        editor.apply()
    }
}