package com.example.igec_admin.Dialogs;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LocationDialog extends DialogFragment {
    private final int LOCATION_REQUEST_CODE = 155;
    private FloatingActionButton fbSubmit;
    private WebView webView;
    private String currentUrl;
    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            currentUrl = url;
            if(currentUrl.contains("!3d"))
                fbSubmit.setEnabled(true);
        }
    };

    public static LocationDialog newInstance(String lat, String lang) {

        Bundle args = new Bundle();
        args.putString("lat", lat);
        args.putString("lng", lang);
        LocationDialog fragment = new LocationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static LocationDialog newInstance() {

        Bundle args = new Bundle();
        LocationDialog fragment = new LocationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_location, container, false);
    }
    @AfterPermissionGranted(LOCATION_REQUEST_CODE)
    private boolean getLocationPermissions() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "We need location permissions in order to the app to function correctly",
                    LOCATION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLocationPermissions();
        fbSubmit = view.findViewById(R.id.button_submit);
        webView = view.findViewById(R.id.web_view);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

        });
        // first time
        if (getArguments().size() == 0) {
            webView.loadUrl("https://www.google.com/maps");
        } else {
            fbSubmit.setEnabled(true);
            webView.loadUrl(String.format("https://www.google.com/maps/search/%s,%s", getArguments().get("lat"), getArguments().get("lng")));
        }
        fbSubmit.setOnClickListener(v -> {
            if (!currentUrl.contains("!3d"))
                return;
            String sub = currentUrl.substring(currentUrl.indexOf("!3d"));
            String[] cords = sub.split("![3-4]d");
            double lat = Double.parseDouble(cords[cords.length - 2]);
            double lang = Double.parseDouble(cords[cords.length - 1]);
            Bundle result = new Bundle();
            result.putString("lat", String.format("%.6f", lat));
            result.putString("lng", String.format("%.6f", lang));
            getParentFragmentManager().setFragmentResult("location", result);
            dismiss();
        });
    }
}
