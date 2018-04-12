package com.andycar.binarytest.webapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.pm.PackageManager.GET_META_DATA;

@SuppressLint({"SetJavaScriptEnabled"})
public class WebViewActivity extends Activity {
    private static final String TAG = "WebViewActivity";
    private String appId;
    private String assetsFolder;
    private Console console;
    private String content;
    private boolean enableZoom;
    private boolean release;
    private WebView webView;

    //private static Context context;

//    public static Context getAppContext() {
//        return WebViewActivity.context;
//    }

    private static void copyFile(String assetPath, String localPath, Context context) {
        try {
            InputStream in = context.getAssets().open(assetPath);
            FileOutputStream out = new FileOutputStream(localPath);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
            File mBin = new File(localPath);
            mBin.setExecutable(true, false);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class C00001 implements OnClickListener {
        C00001() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    private class Console {
        AlertDialog dialog;

        class C00022 implements OnClickListener {
            C00022() {
            }

            public void onClick(DialogInterface arg0, int arg1) {
            }
        }

        private Console() {
        }

        void newMessage(final ConsoleMessage consoleMessage) {
            Intent intent = new Intent("com.andycar.binarytest.webapp.console");
            intent.putExtra("appId", WebViewActivity.this.appId);
            intent.putExtra("sourceId", consoleMessage.sourceId());
            intent.putExtra("lineNumber", consoleMessage.lineNumber());
            intent.putExtra("messageLevel", consoleMessage.messageLevel().toString());
            intent.putExtra("message", consoleMessage.message());
            WebViewActivity.this.sendBroadcast(intent);
            Log.i(WebViewActivity.TAG, "Console Message: sourceId=" + consoleMessage.sourceId() + ", lineNumber=" + consoleMessage.lineNumber() + ", messageLevel=" + consoleMessage.messageLevel() + ", message=" + consoleMessage.message());
            WebViewActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Console.this.showDialog(consoleMessage);
                }
            });
        }

        void showDialog(ConsoleMessage consoleMessage) {
            if (this.dialog == null) {
                Builder dgb = new Builder(WebViewActivity.this);
                dgb.setTitle("Console");
                String msg = consoleMessage.message();
                String src = consoleMessage.sourceId();
                int lineNo = consoleMessage.lineNumber();
                String show = msg;
                if (src != null) {
                    show = new StringBuilder(String.valueOf(show)).append("\n\nSource: ").append(src).append("\nLine: ").append(lineNo).toString();
                }
                dgb.setMessage(show);
                dgb.setPositiveButton("Close", new C00022());
                this.dialog = dgb.create();
                this.dialog.show();
                return;
            }
            this.dialog.setMessage(consoleMessage.message());
            if (!this.dialog.isShowing()) {
                this.dialog.show();
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        private final Console console;
        private final WebView webView;

        MyWebChromeClient(WebView webView, Console console) {
            this.webView = webView;
            this.console = console;
        }

        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            Builder dgb = new Builder(view.getContext());
            dgb.setTitle("Alert");
            dgb.setMessage(message);
            dgb.setPositiveButton("Close", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    result.confirm();
                }
            });
            dgb.create().show();
            return true;
        }

        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            Builder dgb = new Builder(view.getContext());
            dgb.setMessage(message);
            dgb.setPositiveButton("OK", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    result.confirm();
                }
            });
            dgb.setNegativeButton("Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    result.cancel();
                }
            });
            dgb.create().show();
            return true;
        }

        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            Builder dgb = new Builder(view.getContext());
            dgb.setMessage(message);
            final EditText ed = new EditText(view.getContext());
            ed.setText(defaultValue);
            dgb.setView(ed);
            dgb.setPositiveButton("OK", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm(ed.getText().toString());
                }
            });
            dgb.setNegativeButton("Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            });
            dgb.create().show();
            return true;
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            this.console.newMessage(consoleMessage);
            return true;
        }

        public void onReceivedTitle(WebView view, String title) {
            WebViewActivity.this.setTitle(title);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        private MyWebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("file:")) {
                return false;
            }
            WebViewActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
            return true;
        }
    }

    private void extractParameters() {
        this.content = "<html><body><h1>Empty content</h1></body></html>";
        this.enableZoom = false;
        this.assetsFolder = null;
        this.release = true;
        try {
            extractParametersFromBundle(getPackageManager().getApplicationInfo(getPackageName(), GET_META_DATA).metaData);
            if (!this.release) {
                Intent intent = getIntent();
                if (intent != null) {
                    Bundle b = intent.getExtras();
                    if (b != null) {
                        extractParametersFromBundle(b);
                    }
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e2) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e2.getMessage());
        }
    }

    private void extractParametersFromBundle(Bundle bundle) {
        this.content = bundle.getString("content", this.content);
        this.enableZoom = bundle.getBoolean("enableZoom", this.enableZoom);
        this.assetsFolder = bundle.getString("assetsFolder", this.assetsFolder);
        this.release = bundle.getBoolean("release", this.release);
    }

    private WebView createWebView() {
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.setAcceptFileSchemeCookies(true);
        WebView w = new WebView(this);
        w.setWebViewClient(new MyWebViewClient());
        w.setWebChromeClient(new MyWebChromeClient(w, this.console));
        WebSettings webSettings = w.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(this.enableZoom);
        return w;
    }

    @SuppressLint("SetWorldWritable")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context Appcontext = getApplicationContext();
        extractParameters();
        this.console = new Console();
        this.webView = createWebView();
        setContentView(this.webView);
        this.webView.loadDataWithBaseURL("file:///android_asset/", this.content, "text/html", "UTF-8", null);

        ///////START HERE
        String filesDirPath =  this.getFilesDir().getPath();
        File mBin = new File(filesDirPath);
        mBin.setWritable(true, false);
        mBin.setExecutable(true,false);

        copyFile("busybox",filesDirPath+"/busybox", Appcontext);
        copyFile("curl",filesDirPath+"/curl", Appcontext);
        copyFile("dextra",filesDirPath+"/dextra", Appcontext);
        //copyFile("openssl",filesDirPath+"/openssl", context);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 || !this.webView.canGoBack()) {
            return super.onKeyDown(keyCode, event);
        }
        this.webView.goBack();
        return true;
    }

    public void reportError(Throwable e) {
        Builder dgb = new Builder(this);
        dgb.setTitle("Error");
        dgb.setMessage(e.getMessage());
        dgb.setNegativeButton("Close", new C00001());
        dgb.create().show();
    }
}
