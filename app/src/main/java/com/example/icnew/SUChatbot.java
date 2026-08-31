package com.example.icnew;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.view.View;
import android.widget.ProgressBar;


public class SUChatbot extends AppCompatActivity {

    WebView myWeb;
    ProgressBar loading;
    private static final String CHAT_URL = "https://newchat-ybtwjhoqlxrtyvp3g5p9tk.streamlit.app/#b41d89ec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suchatbot);

        myWeb =findViewById(R.id.myWeb);
        loading = findViewById(R.id.chat_loading);
        myWeb.getSettings().setJavaScriptEnabled(true);
        myWeb.getSettings().setDomStorageEnabled(true);
        myWeb.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                loading.setVisibility(View.GONE);
            }
        });
        myWeb.setWebChromeClient(new WebChromeClient());
        findViewById(R.id.chat_back).setOnClickListener(view -> finish());
        findViewById(R.id.chat_refresh).setOnClickListener(view -> {
            loading.setVisibility(View.VISIBLE);
            myWeb.reload();
        });
        myWeb.loadUrl(CHAT_URL);







}


}
