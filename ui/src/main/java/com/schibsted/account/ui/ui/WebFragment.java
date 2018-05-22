/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui;

import android.app.Activity;
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
     * create a new instance of the fragment with a website's link as arguments
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
        private static final String SPID_PAGE = "spid_page";
        private static final String NEW_PASSWORD = "request+new+password";

        DeepLinkOverrideClient(URI appScheme) {
            this.appScheme = appScheme;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return url.startsWith(appScheme.getScheme() + "://") && handleUrlAction(Uri.parse(url), getActivity());
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return uri.getScheme().equals(appScheme.getScheme()) && handleUrlAction(uri, getActivity());
        }

        private boolean handleUrlAction(final Uri uri, final Activity activity) {
            if (activity != null) {
                final String stringUri = uri.toString();
                if (stringUri.contains(SPID_PAGE + "=" + NEW_PASSWORD)) {
                    activity.onBackPressed();
                    return true;
                }
            }
            return false;
        }
    }
}
