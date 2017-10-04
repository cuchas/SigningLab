package br.com.cucha.signinglab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class KeyListActivity extends AppCompatActivity {

    private static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String TAG = KeyListActivity.class.getName();

    @NotNull
    public static Intent newIntent(@Nullable Context applicationContext) {
        return new Intent(applicationContext, KeyListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keylist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupRecycler();
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

    Enumeration<String> listKeys() {
        try {
            //Pode estourar exceções de algorithmo inválido
            KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
            ks.load(null);
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
}
