package com.example.platecalculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var isKg = true
    private var barWeightKg = 20.0
    private var barWeightLbs = 45.0
    private var useCollar = false
    private val collarWeightKg = 2.5
    private val collarWeightLbs = 5.0
    private val allPlatesKg  = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    private val allPlatesLbs = listOf(55.0, 45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
    private val enabledKg  = mutableSetOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    private val enabledLbs = mutableSetOf(55.0, 45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
    private lateinit var etWeight: EditText
    private lateinit var tvUnit: TextView
    private lateinit var tvBarWeight: TextView
    private lateinit var tvCollarWeight: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvPlatesPerSide: TextView
    private lateinit var tvRemainder: TextView
    private lateinit var btnBar20: Button
    private lateinit var btnBar25: Button
    private lateinit var btnKg: Button
    private lateinit var btnLbs: Button
    private lateinit var btnCollar: Button
    private lateinit var tvCollarLabel: TextView
    private lateinit var plateCheckBoxes: Map<Int, CheckBox>
    private lateinit var plateLabels: Map<Int, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.background)

        if (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            window.decorView.systemUiVisibility = 0
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        bindViews()
        setupListeners()
        refreshAll()
    }

    private fun bindViews() {
        etWeight        = findViewById(R.id.etWeight)
        tvUnit          = findViewById(R.id.tvUnit)
        tvBarWeight     = findViewById(R.id.tvBarWeight)
        tvCollarWeight  = findViewById(R.id.tvCollarWeight)
        tvResult        = findViewById(R.id.tvResult)
        tvPlatesPerSide = findViewById(R.id.tvPlatesPerSide)
        tvRemainder     = findViewById(R.id.tvRemainder)
        btnBar20        = findViewById(R.id.btnBar20)
        btnBar25        = findViewById(R.id.btnBar25)
        btnKg           = findViewById(R.id.btnKg)
        btnLbs          = findViewById(R.id.btnLbs)
        btnCollar       = findViewById(R.id.btnCollar)
        tvCollarLabel   = findViewById(R.id.tvCollarLabel)

        plateCheckBoxes = mapOf(
            0 to findViewById(R.id.cb25),
            1 to findViewById(R.id.cb20),
            2 to findViewById(R.id.cb15),
            3 to findViewById(R.id.cb10),
            4 to findViewById(R.id.cb5),
            5 to findViewById(R.id.cb2_5),
            6 to findViewById(R.id.cb1_25)
        )
        plateLabels = mapOf(
            0 to findViewById(R.id.tvLabel25),
            1 to findViewById(R.id.tvLabel20),
            2 to findViewById(R.id.tvLabel15),
            3 to findViewById(R.id.tvLabel10),
            4 to findViewById(R.id.tvLabel5),
            5 to findViewById(R.id.tvLabel2_5),
            6 to findViewById(R.id.tvLabel1_25)
        )
    }

    private fun setupListeners() {
        btnKg.setOnClickListener  { setUnit(true) }
        btnLbs.setOnClickListener { setUnit(false) }
        btnBar20.setOnClickListener { setBar(20.0, 45.0) }
        btnBar25.setOnClickListener { setBar(25.0, 55.0) }

        btnCollar.setOnClickListener {
            useCollar = !useCollar
            updateCollarButton()
            calculate()
        }

        etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculate() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun refreshAll() {
        val unitStr = if (isKg) "kg" else "lbs"
        tvUnit.text = unitStr
        etWeight.hint = "Enter weight ($unitStr)"
        updateUnitButtons()
        updateBarButtons()
        updateCollarButton()
        refreshPlateCheckboxes()
        calculate()
    }

    private fun setUnit(kg: Boolean) {
        if (isKg == kg) return
        isKg = kg
        etWeight.setText("")
        refreshAll()
    }

    private fun setBar(kg: Double, lbs: Double) {
        barWeightKg  = kg
        barWeightLbs = lbs
        updateBarButtons()
        updateBarLabel()
        calculate()
    }

    private fun refreshPlateCheckboxes() {
        val plates  = if (isKg) allPlatesKg  else allPlatesLbs
        val enabled = if (isKg) enabledKg    else enabledLbs
        val unitStr = if (isKg) "kg" else "lbs"

        plateCheckBoxes.forEach { (i, cb) ->
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = plates[i] in enabled
            cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) enabled.add(plates[i]) else enabled.remove(plates[i])
                calculate()
            }
        }
        plateLabels.forEach { (i, tv) ->
            tv.text = "${fmt(plates[i])} $unitStr"
        }

        updateCollarLabel()
        updateBarLabel()
    }

    private fun updateBarLabel() {
        val unitStr = if (isKg) "kg" else "lbs"
        val barVal  = if (isKg) barWeightKg else barWeightLbs
        tvBarWeight.text = "${fmt(barVal)} $unitStr bar"
    }

    private fun updateCollarLabel() {
        val unitStr   = if (isKg) "kg" else "lbs"
        val collarVal = if (isKg) collarWeightKg else collarWeightLbs
        tvCollarLabel.text = "2 × ${fmt(collarVal)} $unitStr each"
    }

    private fun updateUnitButtons() {
        if (isKg) {
            btnKg.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            btnKg.setTextColor(ContextCompat.getColor(this, R.color.btn_active_text))
            btnLbs.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive))
            btnLbs.setTextColor(ContextCompat.getColor(this, R.color.btn_inactive_text))
        } else {
            btnLbs.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            btnLbs.setTextColor(ContextCompat.getColor(this, R.color.btn_active_text))
            btnKg.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive))
            btnKg.setTextColor(ContextCompat.getColor(this, R.color.btn_inactive_text))
        }
    }

    private fun updateBarButtons() {
        val unitStr = if (isKg) "kg" else "lbs"
        btnBar20.text = if (isKg) "20 $unitStr" else "45 $unitStr"
        btnBar25.text = if (isKg) "25 $unitStr" else "55 $unitStr"

        val bar20Active = if (isKg) barWeightKg == 20.0 else barWeightLbs == 45.0
        if (bar20Active) {
            btnBar20.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            btnBar20.setTextColor(ContextCompat.getColor(this, R.color.btn_active_text))
            btnBar25.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive))
            btnBar25.setTextColor(ContextCompat.getColor(this, R.color.btn_inactive_text))
        } else {
            btnBar25.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            btnBar25.setTextColor(ContextCompat.getColor(this, R.color.btn_active_text))
            btnBar20.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive))
            btnBar20.setTextColor(ContextCompat.getColor(this, R.color.btn_inactive_text))
        }
    }

    private fun updateCollarButton() {
        if (useCollar) {
            btnCollar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            btnCollar.setTextColor(ContextCompat.getColor(this, R.color.btn_active_text))
            btnCollar.text = "Remove"
        } else {
            btnCollar.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive))
            btnCollar.setTextColor(ContextCompat.getColor(this, R.color.btn_inactive_text))
            btnCollar.text = "Add"
        }
    }

    private fun calculate() {
        val inputValue = etWeight.text.toString().toDoubleOrNull()
        if (inputValue == null || inputValue <= 0) { showEmptyState(); return }

        val bar     = if (isKg) barWeightKg    else barWeightLbs
        val collar  = if (isKg) collarWeightKg else collarWeightLbs
        val enabled = if (isKg) enabledKg      else enabledLbs
        val unitStr = if (isKg) "kg" else "lbs"

        var remaining = inputValue - bar
        if (useCollar) remaining -= collar * 2

        if (remaining < 0) {
            showError("Weight is less than bar${if (useCollar) " + collars" else ""}!")
            return
        }

        fun Double.round3() = Math.round(this * 1000) / 1000.0

        val perSide = (remaining / 2.0).round3()
        val sortedPlates = enabled.sortedDescending()
        val plateResult = mutableListOf<Pair<Double, Int>>()
        var leftover = perSide

        for (plate in sortedPlates) {
            val count = (leftover / plate).toInt()
            if (count > 0) {
                plateResult.add(Pair(plate, count))
                leftover = (leftover - count * plate).round3()
            }
        }

        tvResult.text = "Total: ${fmt(inputValue)} $unitStr"
        tvResult.visibility = View.VISIBLE

        if (plateResult.isEmpty()) {
            tvPlatesPerSide.text = "No plates needed"
        } else {
            val sb = StringBuilder("Per side:\n")
            plateResult.forEach { (plate, count) ->
                sb.append("  ${fmt(plate)} $unitStr  × $count ${if (count == 1) "plate" else "plates"}\n")
            }
            if (useCollar) sb.append("  ${fmt(collar)} $unitStr  × 2 ${if (useCollar) "collars" else ""}\n")
            tvPlatesPerSide.text = sb.toString().trimEnd()
        }

        if (leftover > 0.001) {
            tvRemainder.text = "Unloaded: ${fmt(leftover)} $unitStr per side"
            tvRemainder.visibility = View.VISIBLE
        } else {
            tvRemainder.visibility = View.GONE
        }
    }

    private fun fmt(value: Double): String {
        return if (value == Math.floor(value)) value.toInt().toString()
        else String.format("%.2f", value).trimEnd('0').trimEnd('.')
    }

    private fun showEmptyState() {
        tvResult.visibility = View.GONE
        tvPlatesPerSide.text = ""
        tvRemainder.visibility = View.GONE
    }

    private fun showError(msg: String) {
        tvResult.text = msg
        tvResult.visibility = View.VISIBLE
        tvPlatesPerSide.text = ""
        tvRemainder.visibility = View.GONE
    }
}