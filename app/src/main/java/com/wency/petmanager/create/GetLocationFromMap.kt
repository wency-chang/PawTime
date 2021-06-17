package com.wency.petmanager.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class GetLocationFromMap {
    fun createIntent(context: Context): Intent {
        val fieldList: MutableList<Place.Field> =
            mutableListOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)

        return Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
            .build(context)
    }

    fun onActivityResult(result: ActivityResult, requestCode: Int): Place? {
        var place: Place? = null

        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            intent?.let {
                when (requestCode) {
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

