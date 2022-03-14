package com.mars.infra.mixin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var mBtnStartLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBtnStartLogin = findViewById(R.id.btn_start_login)

        mBtnStartLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        LoginService.login()
    }
}