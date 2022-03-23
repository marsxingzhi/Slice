package com.mars.infra.mixin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var mBtnStartLogin: Button
    lateinit var mBtnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBtnStartLogin = findViewById(R.id.btn_start_login)
        mBtnLogout = findViewById(R.id.btn_logout)

        mBtnStartLogin.setOnClickListener {
            LoginService.login()
        }

        mBtnLogout.setOnClickListener {
            LoginService.logout()
        }

        Log.e("MainActivity", "onCreate")

        // 测试hook code是否正确，即Log.e有多个重载方法，@Proxy标识的方法描述符与想要hook的方法描述符一致
        Log.e("MainActivity", "Throwable", object : Throwable() {

        })
    }
}