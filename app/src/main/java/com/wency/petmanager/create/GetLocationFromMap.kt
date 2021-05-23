package com.wency.petmanager.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.wency.petmanager.MainActivity
import java.lang.Exception

class GetLocationFromMap() {
    fun createIntent(context: Context): Intent{
        val fieldList: MutableList<Place.Field> = mutableListOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(context)

        return intent
    }
    fun onActivityResult(result: ActivityResult, requestCode: Int): Place?{
        var place: Place? = null
        Log.d("result code","${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK){
            Log.d("result code","OK")
            val intent = result.data
            intent?.let {
                when (requestCode){
                    CreateEventViewModel.CASE_PICK_LOCATION -> {
                        place = Autocomplete.getPlaceFromIntent(intent)
                    }
                    else -> throw Exception("unknown case")
                }


            }

        }
        return place


    }
}

