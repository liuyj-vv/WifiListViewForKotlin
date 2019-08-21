package com.changhong.wifilistviewforkotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

class MainActivity : AppCompatActivity() {
    lateinit var wifiBaseAdapter: WifiBaseAdapter
    lateinit var wifiManager: WifiManager
    var dataList = arrayListOf<Map<String, String>>()

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return super.registerReceiver(receiver, filter)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

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

    fun wifiRegisterReceiver() {
        var intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
        registerReceiver(WifiBroadcastReceiver(), intentFilter)
    }
    fun updateDataAndView() {
        dataList.clear()
        for (scanResult in wifiManager.scanResults) {
            dataList.add(
                mapOf(
                    "ssid" to scanResult.SSID,
                    "bssid" to scanResult.BSSID))
        }
        wifiBaseAdapter.notifyDataSetChanged()
    }

    inner class WifiBaseAdapter(dataList: List<Map<String, String>>): BaseAdapter() {
        private var dataList = dataList
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var layoutInflater = LayoutInflater.from(parent?.context)
            var view = layoutInflater.inflate(R.layout.layout_listview_wifi_item, parent, false)

            view.findViewById<TextView>(R.id.textview_ssid).text = dataList.get(position).get("ssid")
            view.findViewById<TextView>(R.id.textview_bssid).text = dataList.get(position).get("bssid")

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
        override fun onReceive(context: Context?, intent: Intent?) {
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
