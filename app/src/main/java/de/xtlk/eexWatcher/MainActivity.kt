package de.xtlk.eexWatcher

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import de.xtlk.eexWatcher.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var dates: ArrayList<String> = arrayListOf()
        var colors: ArrayList<String> = arrayListOf()
        var values: HashMap<String, LineGraphSeries<DataPoint>> = hashMapOf()
        var changed: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("mainCREATE", "hier")
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /*val pref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        dates.clear()
        pref.all.forEach { any ->
            dates.add(any.value as String)
        }
        changed = true*/
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("mainDestroy", "called!")
        Log.d("dates on Destroy", "$dates")
        /*val pref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(pref.edit()){
            dates.forEachIndexed { index, date ->
                putString(index.toString(), date)
            }
            commit()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            /*R.id.action_settings -> {
                val settings = Intent(this, SettingsActivity::class.java)
                startActivity(settings)
                return true
            }*/
            R.id.addButton -> {
                val addMode = Intent(this, AddDataActivity::class.java)
                startActivity(addMode)
                return true
            }
            R.id.loadToday -> {
                val today = Calendar.getInstance()
                val todaystring = "${today.get(Calendar.YEAR)}-${(today.get(Calendar.MONTH)+1).toString().padStart(2, '0')}-${today.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
                dates.clear()
                dates.add(todaystring)
                colors.clear()
                colors.add("#ffc800")
                fetchAll()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("mainPAUSE", "pause")
        Log.d("pause-changed", "$changed")
    }

    override fun onResume() {
        super.onResume()
        Log.d("mainRESUME", "resume")
        Log.d("dates", "$dates")
        Log.d("resume-changed", "$changed")
        if(changed){
            fetchAll()
            changed = false
        }else{
            displayData()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("savedState", "save")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("restoredState", "restore")
    }

    private fun fetchAll(){
        Toast.makeText(this, "getting Data...", Toast.LENGTH_LONG).show()
        values.clear()
        val queue = Volley.newRequestQueue(this)
        var counter = 0

        Log.d("dates-size", "${dates.size}")
        dates.forEachIndexed { datesIndex, date ->
            val url = "http://eex.xtlk.de/$date"
            val request = StringRequest(url, /*ResponseListener*/{ responseText ->
                try {
                    val splitted = responseText.split("\n")
                    Log.d("onResponse dates-index", "$datesIndex")
                    values[date] = LineGraphSeries<DataPoint>()
                    splitted.forEachIndexed { splitterIndex, element ->
                        if (element != "") values[date]?.appendData(DataPoint(splitterIndex.toDouble(), element.toDouble()), false, 24)
                    }
                    if (dates.size - 1 == counter) {
                        displayData()
                    }
                    counter++
                } catch (e: Exception){
                    Toast.makeText(this@MainActivity, "internal error occurred", Toast.LENGTH_SHORT).show()
                }
            }, /*ErrorResponseListener*/ { error ->
                Log.d("error", error.toString())
                Toast.makeText(this@MainActivity, "Network Error. Please retry", Toast.LENGTH_SHORT).show()
                }
            )
            request.tag = "TAG$datesIndex"
            queue.add(request)
        }
    }

    private fun displayData(){
        binding.resultGraph.removeAllSeries()
        binding.resultGraph.legendRenderer.resetStyles()

        val now= Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE).toDouble() / 60
        Log.d("displayDATA", "time: ${currentHour.toDouble()+currentMinute}")

        val currentHourLine = LineGraphSeries<DataPoint>()
        with(currentHourLine) {
            title = "NOW"
            appendData(DataPoint(currentHour.toDouble()+currentMinute, -1000.0), false, 2)
            appendData(DataPoint(currentHour.toDouble()+currentMinute, 1000.0), false, 2)
        }
        binding.resultGraph.addSeries(currentHourLine)

        var lowY: Double = Double.NaN
        var highY: Double = Double.NaN

        var counter = 0
        values.forEach { (index, series) ->
            with(series) {
                title = index
                color = Color.parseColor(colors[counter])
                isDrawDataPoints = true
                setOnDataPointTapListener { _, dataPoint ->
                    Toast.makeText(this@MainActivity, dataPoint.y.toString(), Toast.LENGTH_SHORT).show()
                }
                if(lowY.isNaN() || lowestValueY < lowY){
                    lowY = lowestValueY
                }
                if(highY.isNaN() || highestValueY > highY){
                    highY = highestValueY
                }
            }
            binding.resultGraph.addSeries(series)
            counter++
        }
        with(binding.resultGraph.viewport){
            isXAxisBoundsManual = true
            isYAxisBoundsManual = true
            setMinY(lowY - 5)
            setMaxY(highY + 5)
            setMinX(0.0)
            setMaxX(24.0)
        }
        with(binding.resultGraph.gridLabelRenderer){
            horizontalAxisTitle = "Hours"
            numHorizontalLabels = 12
            numVerticalLabels = 10
            verticalAxisTitle = "EUR/MWh"
        }

        with(binding.resultGraph.legendRenderer) {
            Log.d("legend visible", "$isVisible")
            isVisible = true
            align = LegendRenderer.LegendAlign.BOTTOM
            margin = 50
        }
    }
}