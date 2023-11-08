package com.hfad.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone


const val countTabs: Int = 5

class MainActivity : AppCompatActivity() {
    private val dataList: MutableList<WeatherData> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.adapter = SectionPagerAdapter(this)

        val tabLayout: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "Town ${(position + 1)}"
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
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
                                    runOnUiThread {
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
                                    //    Toast.makeText(applicationContext, tz.rawOffset.toString(), Toast.LENGTH_SHORT).show()
                                        sunriseText.text = getDateString(response.body()!!.sys.sunrise.toLong() +
                                            response.body()!!.timezone - tz.rawOffset / 1000)
                                        sunsetText.text = getDateString(response.body()!!.sys.sunset.toLong() +
                                                response.body()!!.timezone - tz.rawOffset / 1000)
                                        timeTownText.text = getDateString(response.body()!!.dt.toLong() +
                                                response.body()!!.timezone - tz.rawOffset / 1000)
//                                        sunriseText.text = getDateString((response.body()!!.sys.sunrise
//                                                - tz.rawOffset + response.body()!!.timezone * 1000).toLong())
//                                        sunsetText.text = getDateString((response.body()!!.sys.sunset
//                                                - tz.rawOffset + response.body()!!.timezone * 1000).toLong())
//                                        timeTownText.text = getDateString((response.body()!!.dt
//                                                - tz.rawOffset + response.body()!!.timezone * 1000).toLong())
                                    }
                                }
                            } catch (e: HttpException) {
                                Toast.makeText(applicationContext, "Exception ${e.message}", Toast.LENGTH_SHORT).show()
                            } catch (e: Throwable) {
                                Toast.makeText(applicationContext, "Ooops: Something else went wrong", Toast.LENGTH_SHORT).show()
                            }
                            alertDialog.dismiss();
                        }
                    }
                }
            }
        })
    }


    private fun getDateString(sunDate: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(sunDate * 1000L)
    }

    private fun viewListTown() {
        val catNames = arrayOf("Васька", "Рыжик", "Мурзик")
        var name = ""
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.town)
        builder.setTitle(R.string.list_towns)
        builder.setMultiChoiceItems(catNames, null) { _, which, _ ->
       //     checkedItems[which] = isChecked
            name = catNames[which] // Get the clicked item
        }
        builder.setPositiveButton(R.string.ok_btn) { _, _ ->
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(R.string.delete_btn) { dialog, _ ->
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