package com.example.poly.firsttest.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.poly.firsttest.MainActivity
import android.bluetooth.BluetoothGattService



class BtModule{
    class BtScanerCallback(var parent : MainActivity, var bluetoothLeScanner: BluetoothLeScanner) : ScanCallback(){
        var found = false
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            println("=P= Found: ${result?.device?.name} : ${result?.device?.address}")

            if(result?.device?.name=="BT05" && !found){
                found = true
                bluetoothLeScanner.stopScan(this)
                result?.device?.connectGatt( parent.mContext ,true,mGattCallback)
            }
            super.onScanResult(callbackType, result)
        }

        override fun onScanFailed(errorCode: Int) {
            println("=P= fail")
        }

        private val mGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        println("=P= Connected")
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {

                    }
                }

            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    println("=P= Discovered")

                    for (gattService in gatt.services) {

                        for (characteristic in gattService.characteristics) {

                            if (characteristic.uuid.toString() == "0000ffe9-0000-1000-8000-00805f9b34fb") {

                                Log.w("=P=", "onServicesDiscovered: found LED")

                                val originalString = "560D0F0600F0AA"



                                characteristic.setValue(originalString) // call this BEFORE(!) you 'write' any stuff to the server
                                gatt.writeCharacteristic(characteristic)

                                Log.i("=P=", "onServicesDiscovered: , write bytes?! ")
                            }
                        }
                    }

                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

            }
        }
    }


}
