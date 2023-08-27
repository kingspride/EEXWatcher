package de.xtlk.eexWatcher

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import de.xtlk.eexWatcher.databinding.ActivityAddDataBinding
import de.xtlk.eexWatcher.databinding.PickerBinding

class AddDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDataBinding
    private lateinit var pickerBinding: PickerBinding

    companion object {
        var currentField = 1337
        var openField = 1337
        var counter = 0
        val colorSet = arrayListOf<String>("#FFC800", "#F6511D", "#7FB800", "#76BED0", "#2D7DD2", "#41D3BD", "#363537")
    }
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_data)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        pickerBinding = PickerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        title = "Add Dates"

        dialog = Dialog(this)
        dialog.setContentView(pickerBinding.root)
        pickerBinding.submitCal.setOnClickListener {
            val datum = "${pickerBinding.pickCal.year}-${(pickerBinding.pickCal.month + 1).toString().padStart(2, '0')}-${pickerBinding.pickCal.dayOfMonth.toString().padStart(2, '0')}"
            findViewById<EditText>(openField).setText(datum)
            MainActivity.dates.add(datum)
            MainActivity.colors.add(colorSet[counter])
            if (counter >= colorSet.size-1){
                counter = 0
            }else {
                counter++
            }
            MainActivity.changed = true
            dialog.dismiss()
        }
        pickerBinding.cancelCal.setOnClickListener {
            dialog.dismiss()
        }

        binding.floatingAddFieldButton.setOnClickListener {
            createTextField(currentField)
            currentField++
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_dates, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.closeAddDates -> {
                this.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("pauseADDSTATE", "paused")
    }

    override fun onResume() {
        super.onResume()
        Log.d("resumeADDSTATE", "resumed")
        MainActivity.dates.forEachIndexed{index, date ->
            createTextField(index, date)
        }
    }

    private fun createTextField(index: Int, date: String = ""){
        val datecontainer = LinearLayout(this)
        datecontainer.removeAllViews()
        datecontainer.orientation = LinearLayout.HORIZONTAL
        datecontainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val textfeld = EditText(this)
        with(textfeld){
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 10f)
            id = index
            isFocusable = false
            setHint(R.string.addDateTemplate)
            setText(date)
            setOnClickListener {
                openField = it.id
                dialog.show()
                if (text.isNotBlank()) {
                    Log.d("date", "${text.toString().substring(0,4)} ${text.toString().substring(5,7)} ${text.toString().substring(8,10)}")
                    pickerBinding.pickCal.updateDate(text.toString().substring(0,4).toInt(), text.toString().substring(5,7).toInt()-1, text.toString().substring(8,10).toInt())
                }
            }
        }

        val deleteButton = ImageButton(this)
        with(deleteButton){
            setImageResource(R.drawable.ic_delete_white_24dp)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val colorIndex = MainActivity.dates.indexOf(date)
                MainActivity.dates.remove(date)
                if(colorIndex != -1) MainActivity.colors.removeAt(colorIndex)
                MainActivity.changed = true
                binding.dateList.removeView(it.parent as View)
            }
        }
        Log.d("layout added", "$index")
        datecontainer.addView(textfeld)
        datecontainer.addView(deleteButton)
        binding.dateList.addView(datecontainer)
    }

    /*private fun randomColor(): String{
        val possible = arrayListOf<Char>('E','C','A','8','5','0')
        var newColor = "#"
        while (newColor.length <= 6){
            val selection = Random().nextInt(possible.size)
            Log.d("selection", "$selection")
            newColor += possible[selection]
        }
        Log.d("color", newColor)
        return newColor
    }*/
}