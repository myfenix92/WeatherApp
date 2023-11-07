package com.hfad.weatherapp

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
import java.util.Calendar
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

    private fun addNewTown() {
        val layoutInflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val promtView: View = layoutInflater.inflate(R.layout.add_new_town_alert, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.town)
        builder.setTitle(R.string.add_new_town)
        builder.setView(promtView)
        builder.setPositiveButton(R.string.ok_btn) { dialog, _ ->
            val townName: String
            val inputTown: EditText = promtView.findViewById(R.id.input_new_town)
            townName = inputTown.text.toString()

            val weatherApi = RetrofitClient.getInstance().create(RetrofitServices::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                val response = weatherApi.getWeather(townName, Locale.getDefault().country)
                withContext(Dispatchers.Main) {
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
                                sunriseText.text = getDateString(response.body()!!.sys.sunrise)
                                sunsetText.text = getDateString(response.body()!!.sys.sunset)
                            }
                        }
                        else if(response.code() == 400) {
                            Toast.makeText(applicationContext, R.string.error_town_empty, Toast.LENGTH_SHORT).show()
                        }
                        else if(response.code() == 404) {
                            Toast.makeText(applicationContext, R.string.error_town, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: HttpException) {
                        Toast.makeText(applicationContext, "Exception ${e.message}", Toast.LENGTH_SHORT).show()
                    } catch (e: Throwable) {
                        Toast.makeText(applicationContext, "Ooops: Something else went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.cancel_btn) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun getDateString(sunDate: Int): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getDefault()
        return simpleDateFormat.format(sunDate * 1000L)

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