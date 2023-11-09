package com.hfad.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.weatherapp.Interface.RetrofitServices
import com.hfad.weatherapp.Model.WeatherData
import com.hfad.weatherapp.retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


//var savedTown: MutableList<String> = mutableListOf()
var countTabs: Int = 0
class MainActivity : AppCompatActivity() {
    private val dataList: MutableList<WeatherData> = mutableListOf()
    private var savedTown: MutableList<String> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("onCreate", "create")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.adapter = SectionPagerAdapter(this)
        savedTown = loadTown()?.toMutableList() ?: mutableListOf()
        countTabs = savedTown.size

        val tabLayout: TabLayout = findViewById(R.id.tabs)
        if (countTabs == 0) {
            addNewTown()
        } else {
            //viewPager.currentItem = 0
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = savedTown[position]
            }.attach()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabTown: String = tab?.text.toString()
                val weatherApi = RetrofitClient.getInstance().create(RetrofitServices::class.java)

                CoroutineScope(Dispatchers.IO).launch {
                    val response = weatherApi.getWeather(tabTown, Locale.getDefault().country)
                    withContext(Dispatchers.Main) {
                        try {
                            if (response.isSuccessful) {
                                runOnUiThread {
                                    val tempText: TextView = findViewById(R.id.current_temp)
                                    val tempFeelsText: TextView = findViewById(R.id.feels_temp)
                                    val weatherDescriptionText: TextView =
                                        findViewById(R.id.weather_description)
                                    val pressureText: TextView = findViewById(R.id.pressure)
                                    val humidityText: TextView = findViewById(R.id.humidity)
                                    val popText: TextView = findViewById(R.id.pop)
                                    val windText: TextView = findViewById(R.id.wind)
                                    val visibilityText: TextView = findViewById(R.id.visibility)
                                    val cloudsText: TextView = findViewById(R.id.clouds)
                                    val sunriseText: TextView = findViewById(R.id.sunrise)
                                    val sunsetText: TextView = findViewById(R.id.sunset)
                                    val timeTownText: TextView = findViewById(R.id.timeTown)
                                    Log.d("textV", tempText.text.toString())
                                    tempText.text =
                                        getString(R.string.temp_value, response.body()?.main?.temp)
                                    tempFeelsText.text = getString(
                                        R.string.temp_feels_value,
                                        response.body()?.main?.feels_like
                                    )
                                    weatherDescriptionText.text =
                                        response.body()?.weather?.get(0)?.description
                                    pressureText.text = getString(
                                        R.string.pressure_value,
                                        response.body()?.main?.pressure
                                    )
                                    humidityText.text = getString(
                                        R.string.percent_value,
                                        response.body()?.main?.humidity.toString()
                                    )
                                    popText.text = getString(
                                        R.string.percent_value,
                                        response.body()?.pop ?: "0"
                                    )
                                    windText.text = getString(
                                        R.string.wind_value,
                                        response.body()?.wind?.speed, response.body()?.wind?.deg
                                    )
                                    visibilityText.text = getString(
                                        R.string.visibility_value,
                                        response.body()?.visibility
                                    )
                                    cloudsText.text = getString(
                                        R.string.percent_value,
                                        response.body()?.clouds?.all.toString()
                                    )
                                    val tz = TimeZone.getDefault()
                                    sunriseText.text = getDateString(
                                        response.body()!!.sys.sunrise +
                                                response.body()!!.timezone - tz.rawOffset / 1000
                                    )
                                    sunsetText.text = getDateString(
                                        response.body()!!.sys.sunset +
                                                response.body()!!.timezone - tz.rawOffset / 1000
                                    )
                                    timeTownText.text = getDateString(
                                        response.body()!!.dt +
                                                response.body()!!.timezone - tz.rawOffset / 1000
                                    )
                                }

                            }
                        } catch (e: Throwable) {
                            Toast.makeText(applicationContext, "Oops", Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun saveTown(nameTown: MutableList<String>) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("WeatherTown", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putStringSet("save_town", nameTown.toSet())
        editor.apply()
    }

    fun loadTown(): MutableSet<String>? {
        val sharedPreferences: SharedPreferences = getSharedPreferences("WeatherTown", MODE_PRIVATE)
        //    Toast.makeText(this, shKey.toString(), Toast.LENGTH_SHORT).show()
        return sharedPreferences.getStringSet("save_town", null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.refresh_weather -> Toast.makeText(this, "refresh", Toast.LENGTH_SHORT).show()
            R.id.add_town -> addNewTown()
            R.id.list_towns -> viewListTown()
            R.id.units -> Toast.makeText(this, "units", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    fun checkEmpty(input: EditText, context: Context): Boolean {
        if (input.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.error_town_empty, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    fun checkTownName(input: EditText, context: Context, code: Int): Boolean {
        if (input.text.toString().isNotEmpty() && code == 404) {
            Toast.makeText(context, R.string.error_town, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun addNewTown() {
        val layoutInflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val promtView: View = layoutInflater.inflate(R.layout.add_new_town_alert, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.town)
        builder.setTitle(R.string.add_new_town)
        builder.setView(promtView)
        var townName: String

        val inputTown: EditText = promtView.findViewById(R.id.input_new_town)
        builder.setPositiveButton(R.string.ok_btn) { _, _ ->

        }
        builder.setNegativeButton(R.string.cancel_btn) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (!checkEmpty(inputTown, applicationContext)) {
                townName = inputTown.text.toString()

                val weatherApi = RetrofitClient.getInstance().create(RetrofitServices::class.java)

                CoroutineScope(Dispatchers.IO).launch {
                    val response = weatherApi.getWeather(townName, Locale.getDefault().country)
                    withContext(Dispatchers.Main) {
                        if (!checkTownName(inputTown, applicationContext, response.code())) {
                            try {
                                if (response.isSuccessful) {
                                    val tabLayout: TabLayout = findViewById(R.id.tabs)
                                    tabLayout.addTab(tabLayout.newTab().setText(townName), true)



                                    runOnUiThread {
                                        savedTown.add(townName)
                                        saveTown(savedTown)
                                        countTabs++

                                        val tempText: TextView = findViewById(R.id.current_temp)
                                        val tempFeelsText: TextView = findViewById(R.id.feels_temp)
                                        val weatherDescriptionText: TextView = findViewById(R.id.weather_description)
                                        val pressureText: TextView = findViewById(R.id.pressure)
                                        val humidityText: TextView = findViewById(R.id.humidity)
                                        val popText: TextView = findViewById(R.id.pop)
                                        val windText: TextView = findViewById(R.id.wind)
                                        val visibilityText: TextView = findViewById(R.id.visibility)
                                        val cloudsText: TextView = findViewById(R.id.clouds)
                                        val sunriseText: TextView = findViewById(R.id.sunrise)
                                        val sunsetText: TextView = findViewById(R.id.sunset)
                                        val timeTownText: TextView = findViewById(R.id.timeTown)
                                        Log.d("textV", tempText.text.toString())
                                            tempText.text = getString(R.string.temp_value, response.body()?.main?.temp)
                                            tempFeelsText.text = getString(R.string.temp_feels_value, response.body()?.main?.feels_like)
                                            weatherDescriptionText.text = response.body()?.weather?.get(0)?.description
                                            pressureText.text = getString(R.string.pressure_value,
                                                response.body()?.main?.pressure)
                                            humidityText.text = getString(R.string.percent_value,
                                                response.body()?.main?.humidity.toString())
                                            popText.text = getString(R.string.percent_value,
                                                response.body()?.pop ?: "0")
                                            windText.text = getString(R.string.wind_value,
                                                response.body()?.wind?.speed, response.body()?.wind?.deg)
                                            visibilityText.text = getString(R.string.visibility_value,
                                                response.body()?.visibility)
                                            cloudsText.text = getString(R.string.percent_value,
                                                response.body()?.clouds?.all.toString())
                                            val tz = TimeZone.getDefault()
                                            sunriseText.text = getDateString(response.body()!!.sys.sunrise +
                                                response.body()!!.timezone - tz.rawOffset / 1000)
                                            sunsetText.text = getDateString(response.body()!!.sys.sunset+
                                                    response.body()!!.timezone - tz.rawOffset / 1000)
                                            timeTownText.text = getDateString(response.body()!!.dt +
                                                    response.body()!!.timezone - tz.rawOffset / 1000)

                                    }

                                }
                            } catch (e: HttpException) {
                                Toast.makeText(applicationContext, "Exception ${e.message}", Toast.LENGTH_SHORT).show()
                            } catch (e: Throwable) {
                                Toast.makeText(applicationContext, "Ooops: Something else went wrong", Toast.LENGTH_SHORT).show()
                            }

//                            Log.d("temper", tempText.text.toString())
                            alertDialog.dismiss()


                        }

                    }

                }
                val tabLayout: TabLayout = findViewById(R.id.tabs)
                val viewPager: ViewPager2 = findViewById(R.id.pager)
                Log.d("current", viewPager.currentItem.toString())
                viewPager.currentItem = countTabs - 1
                tabLayout.selectTab(tabLayout.getTabAt(viewPager.currentItem))
                Log.d("current", viewPager.currentItem.toString())
                viewPager.setOnDragListener { v, event ->
                    Log.d("event", event.toString())
                    return@setOnDragListener true
                }
                viewPager.adapter = SectionPagerAdapter(this@MainActivity)
            }
        })
    }


    private fun getDateString(sunDate: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(sunDate * 1000L)
    }

    private fun viewListTown() {
        var name = ""
    //    Toast.makeText(this, loadTown().size.toString(), Toast.LENGTH_SHORT).show()
        val checkedItems: MutableList<Boolean> = BooleanArray(savedTown.size).toMutableList()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.town)
        builder.setTitle(R.string.list_towns)
        builder.setMultiChoiceItems(savedTown.toTypedArray(), checkedItems.toBooleanArray()) { _, which, isChecked ->
            checkedItems[which] = isChecked
            name = savedTown[which]
        }
        builder.setPositiveButton(R.string.ok_btn) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.delete_btn) { dialog, which ->
            val tabLayout: TabLayout = findViewById(R.id.tabs)
            val viewPager: ViewPager2 = findViewById(R.id.pager)
            for(i in savedTown.size - 1 downTo 0) {
                if (checkedItems[i]) {
                    savedTown.removeAt(i)
                    tabLayout.removeTab(tabLayout.getTabAt(i)!!)
                }
            }

            saveTown(savedTown)
            countTabs = savedTown.size
            viewPager.currentItem = 0
            viewPager.adapter = SectionPagerAdapter(this)
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }

}

private class SectionPagerAdapter(fragment: FragmentActivity?) :
    FragmentStateAdapter(fragment!!) {
    override fun getItemCount(): Int {
        return countTabs
    }

    override fun createFragment(position: Int): Fragment {
        return TownFragment()
    }
}