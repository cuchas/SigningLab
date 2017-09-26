package br.com.cucha.signinglab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;

public class AuthWithFingerprintActivity extends AppCompatActivity {

    public static final String KEY_ALIAS_NEW = "MY_NEW_NEW_ULTRA_SECRETE_KEY";
    public static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final int REQUEST_CODE_FINGERPRINT_PERMISSION = 1001;
    private static final String TAG = AuthWithFingerprintActivity.class.getName();

    public static Intent newIntent(Context context) {
        return new Intent(context, AuthWithFingerprintActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_with_fingerprint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT);

            if(permission == PackageManager.PERMISSION_DENIED) {

                String[] permissions = new String[] { Manifest.permission.USE_FINGERPRINT };

                requestPermissions(permissions, REQUEST_CODE_FINGERPRINT_PERMISSION);

                return;
            }

            onFingerPrintAccessAllowed();

        } else {

            Toast.makeText(this, getString(R.string.no_fingerprint_support), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode ==  REQUEST_CODE_FINGERPRINT_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if(permissions[i].equals(Manifest.permission.USE_FINGERPRINT) &&
                        grantResults[i] == PackageManager.PERMISSION_DENIED)

                    return;
            }

            onFingerPrintAccessAllowed();
        }
    }

    private void onNoFingerPrintsEnrolled() {
        Toast.makeText(this, getString(R.string.no_fingerprint_enrolled), Toast.LENGTH_SHORT).show();
    }

    private void onNoFingerPrintHardwareDetected() {
        Toast.makeText(this, getString(R.string.no_fingerprint_hardware), Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onFingerPrintAccessAllowed() {

        FingerprintManager fingerprintManager = (FingerprintManager)
                getSystemService(Context.FINGERPRINT_SERVICE);

        if(!fingerprintManager.isHardwareDetected()) {
            onNoFingerPrintHardwareDetected();
            return;
        }

        if(!fingerprintManager.hasEnrolledFingerprints()) {
            onNoFingerPrintsEnrolled();
            return;
        }

        try {
            KeyStore keyStore = getKeyStore();
            Key key = keyStore.getKey(KEY_ALIAS_NEW, null);

            if(key == null) {
                Toast.makeText(this, "Generating key", Toast.LENGTH_SHORT).show();
                key = generateKey();
            }

            Cipher cipher = getCipher(key);

            authenticate(fingerprintManager, cipher);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            onInvalidKey(fingerprintManager);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onInvalidKey(FingerprintManager fingerprintManager) {
        Key key = generateKey();

        Toast.makeText(this, "Invalid key, recreating...", Toast.LENGTH_SHORT).show();

        try {
            Cipher cipher = getCipher(key);

            authenticate(fingerprintManager, cipher);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @SuppressLint("InlinedApi")
    private Cipher getCipher(Key key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {

        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }

    @SuppressWarnings("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void authenticate(FingerprintManager fingerprintManager, Cipher cipher) {

        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

        fingerprintManager.authenticate(cryptoObject, null, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "onAuthenticationError: " + errString);
                Toast.makeText(AuthWithFingerprintActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                Log.e(TAG, "onAuthenticationHelp: " +  helpString);
                Toast.makeText(AuthWithFingerprintActivity.this, "Auth help", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(AuthWithFingerprintActivity.this, "Auth Success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AuthWithFingerprintActivity.this, "Auth fail", Toast.LENGTH_SHORT).show();
            }
        }, null);

//            generateKey();
//
//            byte[] data = "Eduardo".getBytes();
//
//            byte[] signatureBytes = signData(data);
//
//            boolean verify = verify(data, signatureBytes);
//
//            if(verify)
//                Toast.makeText(this, "verified data", Toast.LENGTH_SHORT).show();
//            else
//                Toast.makeText(this, "data verification fail", Toast.LENGTH_SHORT).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    Key generateKey() {
        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                //Importante escolha do algoritmo da chave
                //Existência do algoritmo na versão do Android
                KeyGenerator kpg = KeyGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER);


                //Purpose da key (crypto, temp ou auth), não pode ser alterado pós geração da chave
                //O que são, uso de bitwise
                //Digest - função geradora de hashes usado na criptografia das chaves
                KeyGenParameterSpec keyGenParameterSpec =
                        new KeyGenParameterSpec
                                .Builder(KEY_ALIAS_NEW, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .setUserAuthenticationRequired(true)
                                .setInvalidatedByBiometricEnrollment(true)
                                .build();

                //Pode estourar exceção por spec inválida
                kpg.init(keyGenParameterSpec);

                Key key = kpg.generateKey();

                return key;
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    private KeyStore getKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
        //Pode estourar exceções de algorithmo inválido
        ks.load(null);
        return ks;
    }
}
