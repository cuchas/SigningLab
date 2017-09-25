package br.com.cucha.signinglab

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_fingerprint_main).setOnClickListener {
//            val intent = KeyListActivity.newIntent(applicationContext)
//            startActivity(intent)

            val intent = AuthWithFingerprintActivity.newIntent(this)
            startActivity(intent)
        }
    }
}
