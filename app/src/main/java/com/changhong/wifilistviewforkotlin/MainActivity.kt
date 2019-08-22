package com.changhong.wifilistviewforkotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Switch
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import android.location.LocationManager
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.support.annotation.RequiresApi
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {
    lateinit var wifiBaseAdapter: WifiBaseAdapter
    lateinit var wifiManager: WifiManager
    var dataList = arrayListOf<Map<String, String>>()
    private var wifiBroadcastReceiver = WifiBroadcastReceiver()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        this.requestPermissions(arrayOf("android.permission.ACCESS_COARSE_LOCATION"), 1)
        this.requestPermissions(arrayOf("android.permission.ACCESS_FINE_LOCATION"), 1)

        wifiBaseAdapter = WifiBaseAdapter(dataList)
        listview_wifi.adapter = wifiBaseAdapter

        switch_wifi.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if (v is Switch) {
                    wifiManager.isWifiEnabled = v.isChecked
                }
            }
        })

        wifiRegisterReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiBroadcastReceiver)
    }

    fun wifiRegisterReceiver() {
        var intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
        registerReceiver(wifiBroadcastReceiver, intentFilter)
    }

    private fun isExistedConfiguration(scanResult: ScanResult): Boolean {
        val configurationList = wifiManager.configuredNetworks
        for (wifiConfiguration in configurationList) {
            if (wifiConfiguration.SSID.equals("\"" + scanResult.SSID + "\"")) {
                return true
            }
        }
        return false
    }


    private fun packageWifiItem(scanResult: ScanResult, info: String = ""): Map<String, String> {
        return mapOf(
                "info" to info,
                "frequency" to "[${scanResult.frequency.toString()}]",
                "ssid" to scanResult.SSID,
                "bssid" to scanResult.BSSID,
                "rssi" to WifiManager.calculateSignalLevel(scanResult.level, 10).toString())
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun updateDataAndView() {
        var isFlag = false
        dataList.clear()
        println("wifiManager.scanResults.size: " + wifiManager.scanResults.size)
        for (scanResult in wifiManager.scanResults) {
            if (isExistedConfiguration(scanResult)) {

                if ((wifiManager.connectionInfo.ssid == "\"" + scanResult.SSID + "\"")
                        and (wifiManager.connectionInfo.bssid == scanResult.BSSID)) {
                    dataList.add(0, packageWifiItem(scanResult, "已连接"))
                    isFlag = true

                } else {
                    dataList.add(if(isFlag) 1 else 0, packageWifiItem(scanResult, "已保存"))

                }
            } else{
                dataList.add(packageWifiItem(scanResult))
            }
        }
        wifiBaseAdapter.notifyDataSetChanged()

    }


    inner class WifiBaseAdapter(dataList: List<Map<String, String>>): BaseAdapter() {
        private var dataList = dataList
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var layoutInflater = LayoutInflater.from(parent?.context)
            var view = layoutInflater.inflate(R.layout.layout_listview_wifi_item, parent, false)

            view.findViewById<TextView>(R.id.textview_frequency).text = dataList.get(position).get("frequency")
            view.findViewById<TextView>(R.id.textview_ssid).text = dataList.get(position).get("ssid")
            view.findViewById<TextView>(R.id.textview_bssid).text = dataList.get(position).get("bssid")
            view.findViewById<TextView>(R.id.rssi).text = dataList.get(position).get("rssi")
            view.findViewById<TextView>(R.id.textview_info).text = dataList.get(position).get("info")

            return view
        }

        override fun getItem(position: Int): Any {
            return dataList.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return dataList.size
        }
    }

    inner class WifiBroadcastReceiver: BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceive(context: Context?, intent: Intent?) {

            println("ACTION: "+ intent?.action)
            when(intent?.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    var state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
                    when(state) {
                        1 ->{
                            switch_wifi.isChecked = false
                            updateDataAndView()
                        }

                        3 -> {
                            switch_wifi.isChecked = true
                            wifiManager.startScan()
                            updateDataAndView()
                        }
                    }

                }
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    updateDataAndView()
                }
                WifiManager.RSSI_CHANGED_ACTION -> {
                    updateDataAndView()
                }
            }
        }
    }
}
