package com.example.poly.firsttest

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri

import android.os.Bundle
import android.provider.Settings
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
import android.widget.Toast

import java.util.*
import kotlin.concurrent.schedule



import kotlinx.android.synthetic.main.content_main.text02


import kotlinx.android.synthetic.main.content_main.satellites_view
import kotlinx.android.synthetic.main.content_main.accuracy_view

import kotlinx.android.synthetic.main.content_main.stop_button
import kotlinx.android.synthetic.main.content_main.flash_button
import kotlinx.android.synthetic.main.content_main.GPS_button


import kotlinx.android.synthetic.main.content_main.map_link
import kotlinx.android.synthetic.main.content_main.joystic

import kotlinx.android.synthetic.main.app_bar_main.inc_id
import kotlinx.android.synthetic.main.app_bar_main.progressBar1


import com.erz.joysticklibrary.JoyStick
import com.example.poly.firsttest.bt.BtModule
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val mContext = this@MainActivity
    var btGattConnection : BluetoothGatt? = null
    var btGattCharacteristic : BluetoothGattCharacteristic? = null
    private var mLocationManager : LocationManager? = null

    var lastData : String = "1,0000000.00000,0000000.00000,1,1,0,0\n"


    var locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val lon = location.longitude
            val alti = location.altitude

            val equatorialRadius = 6378137.0
            val polarRadius = 6356752.314245

            val latRad = lat * Math.PI / 180
            val lonRad = lon * Math.PI / 180

            val eToSquare = 1 - Math.pow(polarRadius, 2.0) / Math.pow(equatorialRadius, 2.0)
            val Nlati = equatorialRadius / Math.sqrt(1 - eToSquare * Math.pow(Math.sin(latRad), 2.0))

//            val x = (Nlati + alti) * Math.cos(latRad) * Math.cos(lonRad)
//            val y = (Nlati + alti) * Math.cos(latRad) * Math.sin(lonRad)


//            satellites_view.text = location.getExtras().getInt("satellites").toString()
//            accuracy_view.text = location.accuracy.toString()

//            text02.text = test
//            map_link.text = "https://www.google.pl/maps/place/" + test

            val mode = 0
            val x = "0000000.00000"
            val y = "0000000.00000"
            val lDir = "1"
            val rDir = "1"
            val powerL = "000"
            val powerR = "000"


//            if( isArduinoBtConnected() ) {
            lastData = "${mode},${x},${y},${lDir},${rDir},${powerL},${powerR}\n"
//            }
//            if(btGattConnection!=null) {
//
//            }
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderDisabled(provider: String) {}
    }


    // Callback for BtModule
    // Called when Gatt and GattCharacteristic are found by BtModule
    fun btConnected(gatt : BluetoothGatt,characteristic : BluetoothGattCharacteristic){

        btGattConnection = gatt
        btGattCharacteristic = characteristic

        runOnUiThread {
            inc_id.visibility = View.VISIBLE
            progressBar1.visibility = View.GONE

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inc_id.visibility = View.GONE
        progressBar1.visibility = View.VISIBLE

        // Create Toolbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Left Side Drawer
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)



        val defautlButtonBG = GPS_button.background
        var coloredButtonBG = getDrawable(R.drawable.abc_btn_colored_material)



        // Request GPS Perisions
        val permList = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(mContext, permList , 112)
        //

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var bluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner

        println("=P= Init")

        if(mBluetoothAdapter.isEnabled){
            if(mLocationManager!=null) {
                if (mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // Start BLE Scan
                    bluetoothLeScanner.startScan(BtModule.BtScanerCallback(this@MainActivity, bluetoothLeScanner))
                }else{
                    // Can't Scan - gps is disabled
                    val toast = Toast.makeText(applicationContext, "Gps is disabled!!!", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        }else{
            // Turn on BT
            mBluetoothAdapter.enable()
        }


        // Start GPS Listener
        GPS_button.setOnClickListener {
            // Check Gps Perms
            if (
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {

                mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)

                text02.text = "GPS Listening..."

            }

            setBtnColor(GPS_button.isChecked,defautlButtonBG,coloredButtonBG,GPS_button)
        }

        // Turn On Robot Lights
        flash_button.setOnClickListener{
            setBtnColor(flash_button.isChecked,defautlButtonBG,coloredButtonBG,flash_button)


            val mode = 1
            val x = "0000000.00000"
            val y = "0000000.00000"
            val lDir = "1"
            val rDir = "1"
            val powerL = "000"
            val powerR = "000"


//            if( isArduinoBtConnected() ) {
                lastData = "${mode},${x},${y},${lDir},${rDir},${powerL},${powerR}\n"
//            }
        }

        // Stop Robot
        stop_button.setOnClickListener {
            if (mLocationManager!=null){
                mLocationManager?.removeUpdates(locationListener)
                text02.text = "Robot Stoped"
                map_link.text = ""
            }

            lastData = "1,0000000.00000,0000000.00000,1,1,0,0\n"
        }



        joystic.setListener(object : JoyStick.JoyStickListener {
            override fun onMove(joyStick: JoyStick, angle: Double, power: Double, direction: Int){

                var dirStri = "Idle"

                when(direction){
                    JoyStick.DIRECTION_CENTER -> dirStri = "Ceneter"
                    JoyStick.DIRECTION_UP -> dirStri = "Up"
                    JoyStick.DIRECTION_DOWN -> dirStri = "Down"
                    JoyStick.DIRECTION_LEFT -> dirStri = "Left"
                    JoyStick.DIRECTION_RIGHT -> dirStri = "Right"
                    JoyStick.DIRECTION_LEFT_UP -> dirStri = "Left Up"
                    JoyStick.DIRECTION_UP_RIGHT -> dirStri = "Right Up"
                }


                text02.text = dirStri


                var lDir = 1
                var rDir = 1

                when(direction){
                    JoyStick.DIRECTION_CENTER -> {
                        lDir=1
                        rDir=1
                    }
                    JoyStick.DIRECTION_LEFT -> {
                        lDir=0
                        rDir=1
                    }
                    JoyStick.DIRECTION_RIGHT -> {
                        lDir=1
                        rDir=0
                    }
                    JoyStick.DIRECTION_UP_RIGHT -> {
                        lDir=1
                        rDir=1
                    }
                    JoyStick.DIRECTION_LEFT_UP->{
                        lDir=1
                        rDir=1
                    }
                    JoyStick.DIRECTION_UP -> {
                        lDir=1
                        rDir=1
                    }
                    JoyStick.DIRECTION_DOWN -> {
                        lDir=0
                        rDir=0
                    }
                    JoyStick.DIRECTION_DOWN_LEFT -> {
                        lDir=0
                        rDir=0
                    }
                    JoyStick.DIRECTION_RIGHT_DOWN -> {
                        lDir=0
                        rDir=0
                    }
                }

                if(lDir == 0) lDir = 1
                else if (lDir ==1 ) lDir = 0

                val mode = 1
                val x = "0000000.00000"
                val y = "0000000.00000"
//                val lDir = "1"
//                val rDir = "1"
                val powerL = power.roundToInt().map(0,100,0,255) //000
                val powerR = power.roundToInt().map(0,100,0,255) //000


                val fillPL =  powerL.toString().fillStart(3)
                val fillPR =  powerR.toString().fillStart(3)

                // Send Joystic Data To Arduino

                lastData = "${mode},${x},${y},${lDir},${rDir},${fillPL},${fillPR}\n"
            }
            override fun onTap(){}
            override fun onDoubleTap(){}
        })


        Timer().schedule(200){ sendJoyData() }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permisions: Array<String>, grantResults: IntArray) {
        turnGPSOn()
    }

    fun sendJoyData(){
        if( isArduinoBtConnected() ) {
            btGattCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            btGattCharacteristic?.setValue(lastData.toByteArray())
            btGattConnection?.writeCharacteristic(btGattCharacteristic)
        }
        Timer().schedule(200) { sendJoyData() }
    }

    // UI callbacks
    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
        if (id == R.id.home) { }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    //
    // Utils
    //

    fun isArduinoBtConnected() : Boolean{
        if (btGattConnection == null) return false
        if (btGattCharacteristic == null) return false
        return true
    }

    // Toggle btn color
    fun setBtnColor(state:Boolean,defautlButtonBG:Drawable,coloredButtonBG:Drawable,btn:View){
        if(state){
            btn.background = coloredButtonBG
        }
        else {
            btn.background = defautlButtonBG
        }
    }

    // try to turn on gps
    fun turnGPSOn(){
        val provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED)

        if(!provider.contains("gps")){ //if gps is disabled
            val poke = Intent()
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider")
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.setData(Uri.parse("3"))
            sendBroadcast(poke)
        }
    }

    fun Int.map( start1:Int, stop1:Int, start2:Int, stop2:Int):Int{
        val a : Double = (this - start1.toDouble()) / (stop1 - start1) * (stop2 - start2) + start2
        return a.roundToInt()
    }

    fun String.fillStart(size: Int): String {
        var out = this
        while (out.length < size){
            out = "0" + out
        }
        return out
    }
}
