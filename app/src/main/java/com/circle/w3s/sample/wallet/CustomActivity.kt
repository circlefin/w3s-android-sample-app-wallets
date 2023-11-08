package com.circle.w3s.sample.wallet

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import circle.programmablewallet.sdk.WalletSdk

class CustomActivity : AppCompatActivity() {
    companion object {
        const val ARG_MSG = "msg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        val msgTv = findViewById<TextView>(R.id.msg)
        val btMain = findViewById<TextView>(R.id.btMain)
        btMain.setOnClickListener { v: View? ->
            goBackToSdkUi()
        }
        val b = intent.extras ?: return
        val msg = b.getString(ARG_MSG)
        if (msg != null) {
            msgTv.text = msg
        }
    }

    override fun onBackPressed() {
        goBackToSdkUi();
        super.onBackPressed()
    }

    /**
     * Bring SDK UI to the front and finish the Activity.
     */
    private fun goBackToSdkUi() {
        WalletSdk.moveTaskToFront()
        finish()

    }
}