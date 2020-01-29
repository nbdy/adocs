package io.eberlein.adocs.ui;

import android.annotation.SuppressLint;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.adocs.R;
import io.eberlein.adocs.objects.Documentation;
import io.eberlein.adocs.objects.events.EventNextUrl;
import io.eberlein.adocs.objects.events.EventViewUrl;

public class ViewDocFragment extends Fragment {
    private static final String TAG = "ViewDocFragment";
    private Documentation documentation;
    private String currentUrl;

    @BindView(R.id.wv) WebView webView;

    ViewDocFragment(Documentation documentation, String sub){
        this.documentation = documentation;
        currentUrl = documentation.getSub(sub);
    }

    ViewDocFragment(String url){
        currentUrl = url;
    }

    class IWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.endsWith(".html") && !currentUrl.equals(url)) EventBus.getDefault().post(new EventNextUrl(url));
            if(!url.startsWith("file:///storage")){
                url = url.replace("file:///", "file://" + documentation.getDirectory() + "/documentation/");
            }
            Log.d(TAG, url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url){
        webView.setWebViewClient(new IWebViewClient());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(url);
        EventBus.getDefault().post(new EventNextUrl(url));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventViewUrl(EventViewUrl e){
        Log.d(TAG, e.getUrl());
        loadUrl(e.getUrl());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_doc, container, false);
        ButterKnife.bind(this, v);
        getActivity().setTitle(R.string.view);
        loadUrl(currentUrl);
        return v;
    }
}
