package com.example.poly.firsttest

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.os.Vibrator

import android.widget.TextView
import java.io.InputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val mContext = this@MainActivity

    private var outputStream: OutputStream? = null
    private var inStream: InputStream? = null

    internal var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

            val lat = location.latitude
            val lon = location.longitude

            val test = findViewById<View>(R.id.text02) as TextView

            test.text = "" + lat + "\n" + lon + "\n" + "https://www.google.pl/maps/@" + lat + "," + lon + ",17.5z"
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {
            val test = findViewById<View>(R.id.text02) as TextView
            test.text = "Called onProviderEnabled"
        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        val fab = findViewById<View>(R.id.fab) as FloatingActionButton


        // GPS Start Listener
        fab.setOnClickListener { ActivityCompat.requestPermissions(mContext as Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 112) }


        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        val panic = findViewById<View>(R.id.panic_button) as FloatingActionButton
        val send = findViewById<View>(R.id.search_button) as FloatingActionButton

        startBt()

        panic.setOnClickListener {
//            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
//            }
//            else {
//                v.vibrate(500)
//            }
//
            run()
        }

        send.setOnClickListener {
            write("Hym... działa?")
        }


}

    fun startBt(){
        val ba = BluetoothAdapter.getDefaultAdapter() as BluetoothAdapter

        if (ba.isEnabled()) {
            val bondedDevices = ba.getBondedDevices()

            if(bondedDevices.size > 0) {
                val devices = bondedDevices.toTypedArray()
                val device = devices[0]
                val uuids = device.getUuids()
                val socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid())
                socket.connect();
                outputStream = socket.getOutputStream();
                inStream = socket.getInputStream();
            }


        }
    }

    fun write(s: String) {
        outputStream?.write(s.toByteArray())
    }

    fun run() {
        val BUFFER_SIZE = 1024
        val buffer = ByteArray(BUFFER_SIZE)
        var bytes = 0
        val b = BUFFER_SIZE

        while (true) {

            bytes = inStream!!.read(buffer, bytes, BUFFER_SIZE - bytes)

            println("=P= ${bytes.toString()}")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permisions: Array<String>, grantResults: IntArray) {

        val mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, locationListener)

            val test = findViewById<View>(R.id.text02) as TextView
            test.text = "Listening..."

        }

    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // adds items to the action bar
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        //        if (id == R.id.nav_camera) {
        //            // Handle the camera action
        //        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
