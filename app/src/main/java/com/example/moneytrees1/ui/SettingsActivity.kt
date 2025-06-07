package com.example.moneytrees1.ui

//✅ Android Framework – System Components
import android.os.Bundle            // Handles activity lifecycle methods like onCreate()
import android.content.SharedPreferences // Used to save/load user preferences locally

// ✅ UI Elements and Event Handling
import android.view.View           // Represents views in the layout, needed for event listeners
import android.widget.AdapterView  // Interface to handle Spinner item selection events
import android.widget.ArrayAdapter // Adapter to populate the Spinner with items
import android.widget.Spinner      // UI element: dropdown menu
import android.widget.Toast        // Popup messages for user feedback

// ✅ AndroidX / AppCompat Libraries
import androidx.appcompat.app.AppCompatActivity // Base class for modern Android activities

// ✅ My App Resource
import com.example.moneytrees1.R // Access to layout (XML) and resource IDs

class SettingsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Set the layout for the Settings screen
            setContentView(R.layout.activity_settings)

            // Reference the Spinner from the layout
            val currencySpinner: Spinner = findViewById(R.id.currency_spinner)

            // List of currency symbols to show in the dropdown
            val currencyOptions = arrayOf("R", "$", "€", "£", "¥")

            // Create an adapter to show the currency options in the Spinner
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                currencyOptions
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            currencySpinner.adapter = adapter

            // Access SharedPreferences to load/save selected currency
            val prefs: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

            // Get the previously selected currency symbol (default to "R" if none)
            val selectedCurrency = prefs.getString("currency_symbol", "R")
            val selectedIndex = currencyOptions.indexOf(selectedCurrency)

            // Pre-select the saved currency in the Spinner if available
            if (selectedIndex >= 0) currencySpinner.setSelection(selectedIndex)

            // Listener for when a currency is selected
            currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Get the selected currency symbol
                    val selected = currencyOptions[position]

                    // Save it to SharedPreferences
                    prefs.edit().putString("currency_symbol", selected).apply()

                    // Show a small confirmation message
                    Toast.makeText(
                        this@SettingsActivity,
                        "Currency set to $selected",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No action needed
                }
            }
        }
}