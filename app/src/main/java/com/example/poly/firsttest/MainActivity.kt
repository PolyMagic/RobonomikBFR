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


    var locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val lon = location.longitude

            satellites_view.text = location.getExtras().getInt("satellites").toString()
            accuracy_view.text = location.accuracy.toString()
            val test = convert(lat,lon)

            text02.text = test
            map_link.text = "https://www.google.pl/maps/place/" + test
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun btConnected(){
        runOnUiThread {
            inc_id.visibility = View.VISIBLE
            progressBar1.visibility = View.GONE

        }

    }

    fun turnGPSOn(){
        val provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            val poke = Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
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

        val permList = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(mContext, permList , 112)

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // GPS Request Permissions
        GPS_button.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)

                text02.text = "GPS Listening..."

            }

            setBtnColor(GPS_button.isChecked,defautlButtonBG,coloredButtonBG,GPS_button)

        }

        flash_button.setOnClickListener{
            setBtnColor(flash_button.isChecked,defautlButtonBG,coloredButtonBG,flash_button)
        }

        // Stops Robot
        stop_button.setOnClickListener {
            //            val i = Intent(Intent.ACTION_VIEW)
            //i.setData(Uri.parse(map_link.text.toString()))
            //startActivity(i)


            if (mLocationManager!=null){
                mLocationManager?.removeUpdates(locationListener)
                text02.text = "Robot Stoped"
                map_link.text = ""
            }

        }


        var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var bluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner

        println("=P= Init")

        if(mBluetoothAdapter.isEnabled){

            if(mLocationManager!=null) {
                if (mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    bluetoothLeScanner.startScan(BtModule.BtScanerCallback(this@MainActivity, bluetoothLeScanner))
                }else{
                    val toast = Toast.makeText(applicationContext, "Gps disabled!!!", Toast.LENGTH_LONG)
                    toast.show()
                }


            }


        }else{
            mBluetoothAdapter.enable()
        }


        joystic.setListener(object : JoyStick.JoyStickListener {
//            var lastPower : Double = 0;
            override fun onMove(joyStick: JoyStick, angle: Double, power: Double, direction: Int){
                text02.text = (angle*60).toString()

//                if(  Math.abs(lastPower-power) > 0.01 ){
//
//                }
//
//                lastPower = power

                if(btGattConnection!=null) {
                    btGattCharacteristic?.setValue("P ${power.roundToInt()} \n" + "D ${direction} \n")
                    btGattConnection?.writeCharacteristic(btGattCharacteristic)
                }

            }
            override fun onTap(){

            }
            override fun onDoubleTap(){}
        })




    }



    fun setBtnColor(state:Boolean,defautlButtonBG:Drawable,coloredButtonBG:Drawable,btn:View){

            if(state){
                btn.background = coloredButtonBG
            }
            else {
                btn.background = defautlButtonBG
            }


    }


    override fun onRequestPermissionsResult(requestCode: Int, permisions: Array<String>, grantResults: IntArray) {
        turnGPSOn()
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



    // Utils

    // Lat Long to cordinates converter for Google Maps
    private fun convert(latitude: Double, longitude: Double): String {
        val builder = StringBuilder()


        val latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS)
        val latitudeSplit = latitudeDegrees.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        builder.append(latitudeSplit[0])
        builder.append("°")
        builder.append(latitudeSplit[1])
        builder.append("'")
        builder.append(latitudeSplit[2])
        builder.append("\"")
        if (latitude < 0) {
            builder.append("S ")
        } else {
            builder.append("N ")
        }


        builder.append(" ")



        val longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS)
        val longitudeSplit = longitudeDegrees.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        builder.append(longitudeSplit[0])
        builder.append("°")
        builder.append(longitudeSplit[1])
        builder.append("'")
        builder.append(longitudeSplit[2])
        builder.append("\"")
        if (longitude < 0) {
            builder.append("W ")
        } else {
            builder.append("E ")
        }

        return builder.toString()
    }
}
