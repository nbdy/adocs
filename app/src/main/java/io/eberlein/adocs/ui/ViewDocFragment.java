package io.eberlein.adocs.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.adocs.R;
import io.eberlein.adocs.objects.Documentation;

public class ViewDocFragment extends Fragment {
    private static final String LOG_TAG = "ViewDocFragment";
    private Documentation documentation;
    private String sub;

    @BindView(R.id.wv) WebView webView;

    public ViewDocFragment(Documentation documentation, String sub){
        this.documentation = documentation;
        this.sub = sub;
    }

    class IWebViewClient extends WebViewClient {
        @Override
        public void onLoadResource(WebView view, String url) {
            if(!url.startsWith("file:///storage")){
                url = url.replace("file:///", "file://" + documentation.getDirectory() + "/documentation/");
            }
            Log.d(LOG_TAG, url);
            super.onLoadResource(view, url);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_doc, container, false);
        ButterKnife.bind(this, v);
        Log.d(LOG_TAG, documentation.getIndex());
        webView.setWebViewClient(new IWebViewClient());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(documentation.getSub(sub));
        return v;
    }

}
