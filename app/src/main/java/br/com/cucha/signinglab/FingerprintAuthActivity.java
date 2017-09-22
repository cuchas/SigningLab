package br.com.cucha.signinglab;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;

public class FingerprintAuthActivity extends AppCompatActivity {

    public static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    public static final String KEY_ALIAS = "MY_ULTRA_SECRETE_KEY";
    public static final String KEY_ALIAS_NEW = "MY_NEW_ULTRA_SECRETE_KEY";
    private static final String HASH_SHA256withECDSA_ALGORITHM = "SHA256withECDSA";
    private static final int REQUEST_CODE_FINGERPRINT_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_auth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupRecycler();
        setupFab();
    }

    private void setupFab() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSingClick();
            }
        });
    }

    private void onSingClick() {

        generateKey();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT);

            if(permission == PackageManager.PERMISSION_DENIED) {

                String[] permissions = new String[] { Manifest.permission.USE_FINGERPRINT };

                requestPermissions(permissions, REQUEST_CODE_FINGERPRINT_PERMISSION);

                return;
            }

            onFingerPrintPermission();

        } else {

            Toast.makeText(this, getString(R.string.no_fingerprint_support), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onFingerPrintPermission() {

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

        authenticate(fingerprintManager);
    }

    @SuppressWarnings("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void authenticate(FingerprintManager fingerprintManager) {

        try {

            Key key = getKeyStore().getKey(KEY_ALIAS_NEW, null);

            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

            fingerprintManager.authenticate(cryptoObject, null, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(FingerprintAuthActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    super.onAuthenticationHelp(helpCode, helpString);
                    Toast.makeText(FingerprintAuthActivity.this, "Auth help", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(FingerprintAuthActivity.this, "Auth Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(FingerprintAuthActivity.this, "Auth fail", Toast.LENGTH_SHORT).show();
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

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void onNoFingerPrintsEnrolled() {
        Toast.makeText(this, getString(R.string.no_fingerprint_enrolled), Toast.LENGTH_SHORT).show();
    }

    private void onNoFingerPrintHardwareDetected() {
        Toast.makeText(this, getString(R.string.no_fingerprint_hardware), Toast.LENGTH_SHORT).show();
    }

    private void setupRecycler() {

        KeysAdapter adapter = new KeysAdapter();

        RecyclerView recyclerView = findViewById(R.id.recycler_fingerprint_auth);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Enumeration<String> keyList = listKeys();

        if(keyList != null) {
            ArrayList<String> list = Collections.list(keyList);
            adapter.setKeyList(list);
        }
    }

    KeyPair generateKey() {
        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                //Importante escolha do algoritmo da chave
                //Existência do algoritmo na versão do Android
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER);


                //Purpose da key (crypto, temp ou auth), não pode ser alterado pós geração da chave
                //O que são, uso de bitwise
                //Digest - função geradora de hashes usado na criptografia das chaves
                KeyGenParameterSpec keyGenParameterSpec =
                        new KeyGenParameterSpec.Builder(KEY_ALIAS_NEW, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
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

            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS_NEW, null);

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

            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS_NEW, null);

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

    Enumeration<String> listKeys() {
        try {
            KeyStore ks = getKeyStore();
            Enumeration<String> aliases = ks.aliases();

            return aliases;

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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

            onFingerPrintPermission();
        }
    }

    @NonNull
    private KeyStore getKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
        //Pode estourar exceções de algorithmo inválido
        ks.load(null);
        return ks;
    }

    @NotNull
    public static Intent newIntent(@Nullable Context applicationContext) {
        return new Intent(applicationContext, FingerprintAuthActivity.class);
    }

    static class KeyView extends RecyclerView.ViewHolder {

        private final TextView textKey;

        public KeyView(View itemView) {
            super(itemView);

            textKey = itemView.findViewById(R.id.text_key_fingerprint_auth);
        }

        public void bind(String key) {
            textKey.setText(key);
        }
    }

    static class KeysAdapter extends RecyclerView.Adapter<KeyView> {

        private List<String> keyList;

        public void setKeyList(List<String> keyList) {
            this.keyList = keyList;
            notifyDataSetChanged();
        }

        @Override
        public KeyView onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View view = inflater.inflate(R.layout.view_fingerprint_auth, parent, false);

            KeyView kv = new KeyView(view);

            return kv;
        }

        @Override
        public void onBindViewHolder(KeyView holder, int position) {
            holder.bind(keyList.get(position));
        }

        @Override
        public int getItemCount() {
            return keyList.size();
        }
    }
}
