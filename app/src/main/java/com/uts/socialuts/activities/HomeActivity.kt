package com.uts.socialuts.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uts.socialuts.R
import com.uts.socialuts.fragments.ChatsFragment
import com.uts.socialuts.fragments.FiltersFragment
import com.uts.socialuts.fragments.HomeFragment
import com.uts.socialuts.fragments.ProfileFragment
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.TokenProvider
import com.uts.socialuts.providers.UsersProvider
import com.uts.socialuts.utils.ViewedMessageHelper

class HomeActivity : AppCompatActivity() {
    var bottomNavigation: BottomNavigationView? = null
    var mTokenProvider: TokenProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mUsersProvider: UsersProvider? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
        mTokenProvider = TokenProvider()
        mAuthProvider = AuthProvider()
        mUsersProvider = UsersProvider()
        openFragment(HomeFragment())
        createToken()
    }

    protected override fun onStart() {
        super.onStart()
        ViewedMessageHelper.updateOnline(true, this@HomeActivity)
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@HomeActivity)
    }

    private fun openFragment(fragment: Fragment?) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment!!)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private var navigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.itemHome) {
                // FRAGMENT HOME
                openFragment(HomeFragment())
            } else if (item.itemId == R.id.itemChats) {
                // FRAGMENT CHATS
                openFragment(ChatsFragment())
            } else if (item.itemId == R.id.itemFilters) {
                // FRAGMENT FILTROS
                openFragment(FiltersFragment())
            } else if (item.itemId == R.id.itemProfile) {
                // FRAGMENT PROFILE
                openFragment(ProfileFragment())
            }
            true
        }

    private fun createToken() {
        mTokenProvider?.create(mAuthProvider?.getUid())
    }
}