package com.hfad.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.weatherapp.Interface.RetrofitServices
import com.hfad.weatherapp.Model.ForecastData
import com.hfad.weatherapp.Model.WeatherData
import com.hfad.weatherapp.retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.CoroutineContext
import kotlin.math.floor


var countTabs: Int = 0
class MainActivity : AppCompatActivity() {
    private var savedTown: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.adapter = SectionPagerAdapter(this)
        savedTown = loadTown().first?.toMutableList() ?: mutableListOf()
        countTabs = savedTown.size
        val tabLayout: TabLayout = findViewById(R.id.tabs)


        if (savedTown.size != 0 && savedTown[0].isEmpty()) {
            savedTown.clear()
            viewPager.visibility = ViewPager2.INVISIBLE
            addNewTown()
        } else {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = savedTown[position]
            }.attach()
            tabLayout.getTabAt(loadTown().second)?.select()
            getWeatherApi(tabLayout.getTabAt(loadTown().second)?.text.toString(), false)
        }

   //     refreshWeather()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabTown: String = tab?.text.toString()
                getWeatherApi(tabTown, false)
                saveTown(savedTown, tabLayout.selectedTabPosition)
                refreshWeather()

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

    fun getWeatherApi(nameTown: String, isNew: Boolean) {
        val weatherApi = RetrofitClient.getInstance().create(RetrofitServices::class.java)
        AsyncTaskExecutorService().execute(nameTown, weatherApi, isNew)
    }

    private fun degToCompass(num: Int?): String {
        val degValue: Int = floor((num?.div(22.5))?.plus(0.5) ?: 0.0).toInt();
        val nameDirect: MutableList<String> =
            mutableListOf("С", "С-С-В", "С-В", "В-С-В",
                          "В", "В-Ю-В", "Ю-В", "Ю-Ю-В",
                          "Ю", "Ю-Ю-З", "Ю-З", "З-Ю-З",
                          "З", "З-С-З", "С-З", "С-С-З")
        return nameDirect[(degValue % 16)];
    }
    private fun setImage(imageView: ImageView, imageId: Int) {
        when(imageId) {
            in 200..202 -> imageView.setImageResource(R.drawable.weather_thunderstorm_200_202)
            in 210..211 -> imageView.setImageResource(R.drawable.weather_thunderstorm_210_211)
            in 212..221 -> imageView.setImageResource(R.drawable.weather_thunderstorm_212_221)
            in 230..232 -> imageView.setImageResource(R.drawable.weather_thunderstorm_230_232)
            in 300..302 -> imageView.setImageResource(R.drawable.weather_drizzle_300_302)
            in 310..321 -> imageView.setImageResource(R.drawable.weather_drizzle_310_321)
            in 500..502, in 520..521 -> imageView.setImageResource(R.drawable.weather_rain_500_502_520_521)
            in 503..504, in 522..531 -> imageView.setImageResource(R.drawable.weather_rain_503_504_522_531)
            in 600..602 -> imageView.setImageResource(R.drawable.weather_snow_600_602)
            in 611..613 -> imageView.setImageResource(R.drawable.weather_snow_611_613)
            in 615..620 -> imageView.setImageResource(R.drawable.weather_snow_615_620)
            in 621..622 -> imageView.setImageResource(R.drawable.weather_snow_621_622)
            701, 721 -> imageView.setImageResource(R.drawable.weather_mist_701_721)
            711 -> imageView.setImageResource(R.drawable.weather_smoke_711)
            731, 751 -> imageView.setImageResource(R.drawable.weather_sand_731_751)
            741 -> imageView.setImageResource(R.drawable.weather_fog_741)
            761 -> imageView.setImageResource(R.drawable.weather_dust_761)
            762 -> imageView.setImageResource(R.drawable.weather_volcanic_ash_762)
            in 771..781 -> imageView.setImageResource(R.drawable.weather_tornado_771_781)
            800 -> imageView.setImageResource(R.drawable.weather_clear_sky_day_800)
            in 801..802 -> imageView.setImageResource(R.drawable.weather_cloud_801_802)
            803 -> imageView.setImageResource(R.drawable.weather_cloud_803)
            804 -> imageView.setImageResource(R.drawable.weather_cloud_804)
        }
    }

    private fun getWeatherView(response: Response<WeatherData>) {
        val iconCurrent: ImageView = findViewById(R.id.icon_current_weather)
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

        val imageID: Int = response.body()?.weather?.get(0)?.id!!
        setImage(iconCurrent, imageID)
        tempText.text = getString(R.string.temp_value, response.body()?.main?.temp?.toInt())
        tempFeelsText.text = getString(R.string.temp_feels_value, response.body()?.main?.feels_like?.toInt())
        weatherDescriptionText.text = response.body()?.weather?.get(0)?.description
        pressureText.text = getString(
            R.string.pressure_value,
            (response.body()?.main?.pressure.toString().toInt() / 1.333).toInt()
        )
        humidityText.text = getString(
            R.string.percent_value,
            response.body()?.main?.humidity.toString()
        )
        popText.text = getString(
            R.string.pop_value,
            response.body()?.rain?.`1h` ?: 0.0
        )
        windText.text = getString(
            R.string.wind_value,
            response.body()?.wind?.speed, degToCompass(response.body()?.wind?.deg)
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

    fun getForecastView(response: Response<ForecastData>) {
        val daysForecast: LayoutInflater = LayoutInflater.from(applicationContext)
        val forecastLayout: LinearLayout = findViewById(R.id.forecast_days_layout)
        val tz = TimeZone.getDefault()
        val startTime = response.body()?.list?.get(0)?.dt?.minus(tz.rawOffset / 1000)
        val startValue: String = getDateString(startTime!!.toLong()).substring(0, 2)
        val getTimeInterval = 3
        val start = ((24 - startValue.toInt()) / getTimeInterval) //24h in day
        val step = 24 / getTimeInterval
        val countDay = 4
        forecastLayout.removeAllViews()
        for ((count, i) in (start until start + step * countDay step step).withIndex()) {
            daysForecast.inflate(R.layout.days_weather, forecastLayout)
            forecastLayout[count].findViewById<TextView>(R.id.date_day).text =
                response.body()?.list?.get(i)?.dt_txt
                    ?.substring(0, 10)
                    ?.split("-")
                    ?.reversed()
                    ?.joinToString(separator = "-")
            forecastLayout[count].findViewById<TextView>(R.id.day_temp).text =
                getString(R.string.temp_max_min, response.body()?.list?.get(i + 4)?.main?.temp_max?.toInt(),
                    response.body()?.list?.get(i)?.main?.temp_min?.toInt())
            val imageID: Int = response.body()?.list?.get(i)?.weather?.get(0)?.id!!
            setImage(forecastLayout[count].findViewById(R.id.icon_weather), imageID)
            forecastLayout[count].findViewById<TextView>(R.id.day_weather_desc).text =
                response.body()?.list?.get(i)?.weather?.get(0)?.description
        }
    }

    private fun saveTown(nameTown: MutableList<String>, tab: Int) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("WeatherTown", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val townString: String = nameTown.joinToString(separator = "*").replace("*", "|")
        editor.putString("save_town", townString)
        editor.putInt("save_tab", tab)
        editor.apply()
    }

    private fun loadTown(): Pair<MutableList<String>?, Int> {
        val sharedPreferences: SharedPreferences = getSharedPreferences("WeatherTown", MODE_PRIVATE)
        val res = sharedPreferences.getString("save_town", "")
        val tab = sharedPreferences.getInt("save_tab", 0)
        return Pair(res?.split("|")?.toMutableList(), tab)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh_weather -> refreshWeather()
            R.id.add_town -> addNewTown()
            R.id.list_towns -> viewListTown()
            R.id.units -> Toast.makeText(this, "units", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkEmpty(input: EditText, context: Context): Boolean {
        if (input.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.error_town_empty, Toast.LENGTH_SHORT).show()
            return true
        }
        if (input.text.isNotEmpty() && savedTown.contains(input.text.toString())) {
            Toast.makeText(context, R.string.error_town_duplicate, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    fun checkTownName(input: String, context: Context, code: Int): Boolean {
        if (input.isNotEmpty() && code == 404) {
            Toast.makeText(context, R.string.error_town, Toast.LENGTH_SHORT).show()
            addNewTown()
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
        builder.setNegativeButton(R.string.cancel_btn) { _, _ ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (!checkEmpty(inputTown, applicationContext)) {
                    townName = inputTown.text.toString()
                    getWeatherApi(townName, true)
                    alertDialog.dismiss()
            }
        })
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(View.OnClickListener {
            if (savedTown.size == 0) {
                Toast.makeText(this, "Нет городов, добавьте город!", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
            }
        })
    }

    private fun refreshWeather() {
        val tabLayout: TabLayout = findViewById(R.id.tabs)
        val nameTown = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
        getWeatherApi(nameTown, false)
    }


    private fun getDateString(sunDate: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(sunDate * 1000L)
    }

    private fun viewListTown() {
        val checkedItems: MutableList<Boolean> = BooleanArray(savedTown.size).toMutableList()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.town)
        builder.setTitle(R.string.list_towns)
        builder.setMultiChoiceItems(
            savedTown.toTypedArray(),
            checkedItems.toBooleanArray()
        ) { _, which, isChecked ->
            checkedItems[which] = isChecked
            savedTown[which]
        }
        builder.setPositiveButton(R.string.close_btn) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.delete_btn) { dialog, _ ->
            val tabLayout: TabLayout = findViewById(R.id.tabs)
            val viewPager: ViewPager2 = findViewById(R.id.pager)
            for (i in savedTown.size - 1 downTo 0) {
                if (checkedItems[i]) {
                    savedTown.removeAt(i)
                    tabLayout.removeTab(tabLayout.getTabAt(i)!!)
                }
            }
            tabLayout.getTabAt(0)?.select()
            saveTown(savedTown, 0)
            countTabs = savedTown.size
            viewPager.currentItem = 0
            if (savedTown.size == 0) {
                val linearLayout: LinearLayout = findViewById(R.id.last_refresh_layout)
                linearLayout.visibility = LinearLayout.INVISIBLE
                viewPager.visibility = ViewPager2.INVISIBLE
                Toast.makeText(this, "Нет городов, добавьте город!", Toast.LENGTH_SHORT).show()
                addNewTown()
            }
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }

    override fun onPause() {
        super.onPause()
        AsyncTaskExecutorService().cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        AsyncTaskExecutorService().cancel()
    }

    inner class AsyncTaskExecutorService: CoroutineScope {
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // to run code in Main(UI) Thread

        // call this method to cancel a coroutine when you don't need it anymore,
        // e.g. when user closes the screen
        fun cancel() {
            job.cancel()
        }

        fun execute(nameTown: String, weatherApi: RetrofitServices, isNew: Boolean) {
            launch {
                onPreExecute()
                val responseA = weatherApi.getWeather(nameTown, Locale.getDefault().country)
                val responseB = weatherApi.getForecast(nameTown, Locale.getDefault().country)
                doInBackground(nameTown, responseA, responseB, isNew)
                onPostExecute()
            }
        }

        private suspend fun doInBackground(nameTown: String, responseA: Response<WeatherData>, responseB: Response<ForecastData>, isNew: Boolean): String = withContext(Dispatchers.IO) { // to run code in Background Thread
                try {
                   // delay(50)
                    if (responseA.isSuccessful && responseB.isSuccessful) {
                        runOnUiThread {
                            if (isNew) {
                                val tabLayout: TabLayout = findViewById(R.id.tabs)
                                tabLayout.addTab(tabLayout.newTab().setText(nameTown), true)
                                savedTown.add(nameTown)
                                countTabs++
                                tabLayout.getTabAt(countTabs - 1)?.select()
                                saveTown(savedTown, tabLayout.selectedTabPosition)
                                getWeatherView(responseA)
                                getForecastView(responseB)
                            } else {
                                getWeatherView(responseA)
                                getForecastView(responseB)
                            }
                        }
                    } else if (!responseA.isSuccessful && !responseB.isSuccessful) {
                        runOnUiThread {
                            checkTownName(nameTown, applicationContext, responseA.code())
                        }
                    }
                } catch (e: HttpException) {
                    Toast.makeText(
                        applicationContext,
                        "Exception ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } catch (e: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Ooops: Something else went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            return@withContext responseA.body()?.name.toString()
        }

        // Runs on the Main(UI) Thread
        private fun onPreExecute() {
            // show progress
            val progressBar: ProgressBar = findViewById(R.id.progressBar)
            progressBar.visibility = ProgressBar.VISIBLE
            val linearLayout: LinearLayout = findViewById(R.id.last_refresh_layout)
            val viewPager: ViewPager2 = findViewById(R.id.pager)
            viewPager.visibility = ViewPager2.INVISIBLE
            linearLayout.visibility = LinearLayout.INVISIBLE
        }

        // Runs on the Main(UI) Thread
        private fun onPostExecute() {
            // hide progress
            val progressBar: ProgressBar = findViewById(R.id.progressBar)
            progressBar.visibility = ProgressBar.INVISIBLE
            val viewPager: ViewPager2 = findViewById(R.id.pager)
            val linearLayout: LinearLayout = findViewById(R.id.last_refresh_layout)
            viewPager.visibility = ViewPager2.VISIBLE
            linearLayout.visibility = LinearLayout.VISIBLE

        }
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