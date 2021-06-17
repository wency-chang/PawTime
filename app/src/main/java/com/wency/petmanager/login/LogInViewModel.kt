package com.wency.petmanager.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LogInViewModel(val repository: Repository): ViewModel() {


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val _logInSuccess = MutableLiveData<Boolean>(false)
    val logInSuccess : LiveData<Boolean>
        get() = _logInSuccess


    fun getGoogleSignInActivityResult(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let { firebaseAuthWithGoogle(it) }

        } catch (e: ApiException){
            Log.w(ManagerApplication.instance.getString(R.string.APP_NAME),
                "signInResult failed code: ${e.statusCode}")
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String){
        coroutineScope.launch {
            when (val result = repository.signInWithGoogle(idToken)){
                is Result.Success -> {
                    if (result.data.isNotEmpty()){
                        UserManager.userID = result.data
                        _logInSuccess.value = true
                    }
                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Toast.makeText(
                    ManagerApplication.instance,
                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


}