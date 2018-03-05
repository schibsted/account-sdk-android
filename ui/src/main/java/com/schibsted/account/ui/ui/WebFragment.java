/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.schibsted.account.ui.R;

import java.net.URI;


/**
 * a {@link android.support.v4.app.Fragment} used to display a website into the application
 */
public class WebFragment extends BaseFragment {

    private static final String KEY_URL = "URL";
    private static final String KEY_APP_SCHEME = "APP_SCHEME";
    private WebView webview;

    /**
     * create a new instance of the fragment with a website's link as arguements
     *
     * @param link the website link
     * @return an instance of {@link WebFragment}
     */
    public static WebFragment newInstance(String link, URI appScheme) {
        final Bundle args = new Bundle();
        args.putString(KEY_URL, link);
        args.putSerializable(KEY_APP_SCHEME, appScheme);
        final WebFragment fragment = new WebFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.schacc_web_fragment_layout, container, false);
        webview = view.findViewById(R.id.webview);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final URI appScheme = (URI) getArguments().getSerializable(KEY_APP_SCHEME);
        final DeepLinkOverrideClient client = new DeepLinkOverrideClient(appScheme);

        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(client);
        webview.loadUrl(getArguments().getString(KEY_URL));
    }

    class DeepLinkOverrideClient extends WebViewClient {
        private final URI appScheme;

        public DeepLinkOverrideClient(URI appScheme) {
            this.appScheme = appScheme;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent;

            if (url.startsWith(appScheme.getScheme() + "://")) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

                return true;
            }

            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Intent intent;
            final Uri uri = request.getUrl();

            if (uri.getScheme().equals(appScheme.getScheme())) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);

                return true;
            }

            return false;
        }
    }
}
