package br.com.cucha.signinglab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class FingerprintAuthActivity extends AppCompatActivity {

    public static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    public static final String KEY_ALIAS = "MY_ULTRA_SECRETE_KEY";
    private static final String SIGN_ALGORITHM = "SHA256withECDSA";

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

        byte[] data = "Eduardo".getBytes();

        byte[] bytes = signData(data);

        boolean verify = verify(data);

        if(verify) {
            Toast.makeText(this, "verified data", Toast.LENGTH_SHORT).show();
        }
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
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE_PROVIDER);


                //Purpose da key (crypto, temp ou auth), não pode ser alterado pós geração da chave
                //O que são, uso de bitwise
                //Digest - função geradora de hashes usado na criptografia das chaves
                KeyGenParameterSpec keyGenParameterSpec =
                        new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
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


            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            signature.update(data);

            byte[] signed = signature.sign();

            return signed;

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

    boolean verify(byte[] data) {

        try {
            KeyStore keyStore = getKeyStore();

            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);

            if(!(entry instanceof KeyStore.PrivateKeyEntry))
                return false;

            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(((KeyStore.PrivateKeyEntry) entry).getCertificate());
            signature.update(data);

            return signature.verify(data);

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
