package com.hfad.weatherapp

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

const val countTabs: Int = 5

class MainActivity : AppCompatActivity() {
    private val dataList: MutableList<DaysWeatherList> = mutableListOf()
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
        builder.setPositiveButton(R.string.ok_btn) { dialog, which ->
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel_btn) { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun viewListTown() {
        val catNames = arrayOf("Васька", "Рыжик", "Мурзик")
        var name: String = ""
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.town)
        builder.setTitle(R.string.list_towns)
        builder.setMultiChoiceItems(catNames, null) { _, which, _ ->
       //     checkedItems[which] = isChecked
            name = catNames[which] // Get the clicked item
        }
        builder.setPositiveButton(R.string.ok_btn) { dialog, which ->
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(R.string.delete_btn) { dialog, which ->
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