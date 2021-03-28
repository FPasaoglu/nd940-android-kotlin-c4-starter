package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseViewModel : ViewModel() {

    enum class AuthenticationState { AUTHENTICADED, UNAUTHENTICADED}

    val authenticationState = FirebaseLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICADED
        } else {
            AuthenticationState.UNAUTHENTICADED
        }

    }

}

class FirebaseLiveData : LiveData<FirebaseUser?>() {
    val firebaseAuth = FirebaseAuth.getInstance()

    val authStateListener = FirebaseAuth.AuthStateListener {
        value = it.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}