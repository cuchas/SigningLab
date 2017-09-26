package br.com.cucha.signinglab;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;

public class SignVerifyActivity extends AppCompatActivity {

    private static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "MY_ULTRA_SECRETE_KEY";
    private static final String HASH_SHA256withECDSA_ALGORITHM = "SHA256withECDSA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_verify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @NonNull
    private KeyStore getKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
        //Pode estourar exceções de algorithmo inválido
        ks.load(null);
        return ks;
    }

    KeyPair generateKeyPair() {
        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                //Importante escolha do algoritmo da chave
                //Existência do algoritmo na versão do Android
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER);


                //Purpose da key (crypto, temp ou auth), não pode ser alterado pós geração da chave
                //O que são, uso de bitwise
                //Digest - função geradora de hashes usado na criptografia das chaves
                KeyGenParameterSpec keyGenParameterSpec =
                        new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .build();

                //Pode estourar exceção por spec inválida
                kpg.initialize(keyGenParameterSpec);

                KeyPair keyPair = kpg.generateKeyPair();

                return keyPair;
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

    byte[] signData(byte[] data) {
        try {
            KeyStore keyStore = getKeyStore();

            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);

            if(!(entry instanceof KeyStore.PrivateKeyEntry))
                return null;

            Signature signature = Signature.getInstance(HASH_SHA256withECDSA_ALGORITHM);
            signature.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            signature.update(data);

            byte[] signatureBytes = signature.sign();

            return signatureBytes;

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    boolean verify(byte[] data, byte[] signatureBytes) {

        try {
            KeyStore keyStore = getKeyStore();

            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);

            if(!(entry instanceof KeyStore.PrivateKeyEntry))
                return false;

            Signature signature = Signature.getInstance(HASH_SHA256withECDSA_ALGORITHM);
            signature.initVerify(((KeyStore.PrivateKeyEntry) entry).getCertificate());
            signature.update(data);

            return signature.verify(signatureBytes);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }

}
