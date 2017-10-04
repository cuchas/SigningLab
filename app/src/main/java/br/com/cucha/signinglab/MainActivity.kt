package br.com.cucha.signinglab

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_fingerprint_main).setOnClickListener {
            val intent = AuthWithFingerprintActivity.newIntent(this)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_confirmcredential_main).setOnClickListener {
            val keyguard = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val title = getString(R.string.are_you_yourself)
                val description = getString(R.string.pls_show_me_you_)

                val intent = keyguard.createConfirmDeviceCredentialIntent(title, description)
                startActivityForResult(intent, REQUEST_CONFIRM_CREDENTIAL)
            }
        }

        findViewById<Button>(R.id.button_listkeys_main).setOnClickListener {
            val intent = KeyListActivity.newIntent(applicationContext)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CONFIRM_CREDENTIAL) {
            if(resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.credential_confirmed), Toast.LENGTH_SHORT)
                        .show()
                return
            }

            Toast.makeText(this, getString(R.string.credential_confirm_failed), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    companion object {
        val REQUEST_CONFIRM_CREDENTIAL = 1001
    }
}
