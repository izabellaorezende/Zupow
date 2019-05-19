package com.zupow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    final Activity activity = this;
    public Uri imageUri;

    private static final int FILECHOOSER_RESULTCODE = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.main_webview);

        // Define url that will open in webview
        String webViewUrl = "https://zupow.com/";

        // Javascript inabled on webview
        webView.getSettings().setJavaScriptEnabled(true);

        // Other webview options
        webView.getSettings().setLoadWithOverviewMode(true);

        //Other webview settings
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);

        //Load url in webview
        webView.loadUrl(webViewUrl);

        // Define Webview manage classes
        startWebView();
    }

    private void startWebView(){
        // Create new webview Client to show progress dialog
        // Called When opening a url or click on link
        // You can create external class extends with WebViewClient
        // Taking WebViewClient as inner class

        webView.setWebViewClient(new WebViewClient(){
            ProgressDialog progressDialog;

            public boolean shouldOverrideUrlLoading(WebView view, String url){
                if (Uri.parse(url).getHost().endsWith("zupow.com")){
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            }

            public void onLoadResource (WebView view, String url) {
                if (progressDialog == null && url.contains("zupow")){
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }
            }

            public void onPageFinished(WebView view, String url) {
                try {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){
                mUploadMessage = uploadMsg;
                try{
                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES),
                            "AndroidExampleFolder");
                    if(!imageStorageDir.exists()){
                        imageStorageDir.mkdirs();
                    }
                    File file = new File(imageStorageDir + File.separator +
                            "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    mCapturedImageURI = Uri.fromFile(file);

                    final Intent captureIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                    );

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                            new Parcelable[]{ captureIntent });

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                }
                catch (Exception ex){
                    Toast.makeText(getBaseContext(), "Erro: "+ex, Toast.LENGTH_LONG).show();
                }
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            public boolean onConsoleMessage(ConsoleMessage cm){
                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }

            public void onConsoleMessage(String message, int lineNumber, String sourceID){
                //Log.d("androidruntime", "Show console messages, Used for debugging: " + message);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (requestCode == FILECHOOSER_RESULTCODE){
            if (null == this.mUploadMessage)
                return;
            Uri result = null;

            try{
                if(resultCode != RESULT_OK){
                    result = null;
                } else {
                    result = intent == null ? mCapturedImageURI : intent.getData();
                }
            }
            catch (Exception ex){
                Toast.makeText(getApplicationContext(), "activity :"+ex,
                        Toast.LENGTH_LONG).show();
            }
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public  void onBackPressed(){
        if(webView.canGoBack()){
            webView.goBack();
        }else{
            super.onBackPressed();
        }
    }
}
