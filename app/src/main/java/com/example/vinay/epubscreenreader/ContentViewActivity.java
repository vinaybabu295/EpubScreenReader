package com.example.vinay.epubscreenreader;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class ContentViewActivity extends Activity {

    WebView webView;
    Button prev;
    Button next;
    int curr_page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        webView = (WebView) findViewById(R.id.webview);
        prev = (Button) findViewById(R.id.prev_button);
        next = (Button) findViewById(R.id.next_button);

        webView.getSettings().setJavaScriptEnabled(true);   // enable javascript
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                injectJavascript(view);
            }

        });
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);  // disabling touch events
            }
        });
        webView.setVerticalScrollBarEnabled(false);

        String displayString = getIntent().getExtras().getString("display");
        if(displayString != null)
            webView.loadData(displayString,"text/html; charset=utf-8", "utf-8");
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String js = "javascript:scroll_left()";
//                webView.loadUrl(js);
                webView.scrollBy(-webView.getWidth(),0);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String js = "javascript:scroll_right()";
//                webView.loadUrl(js);
                webView.scrollBy(webView.getWidth(),0);
            }
        });
    }
    /*
        Inject javascript after the Html file is loaded into the WebView
     */
    public void injectJavascript(WebView view) {
        // javaScript function to achieve pagination like kindle ebook reader
        String js = "javascript:function initialize() { " +
                "var d = document.getElementsByTagName('body')[0];" +
                "var ourH = window.innerHeight; " +
                "var ourW = window.innerWidth; " +
                "var fullH = d.offsetHeight; " +
                "var pageCount = Math.floor(fullH/ourH)+1;" +
                "var currentPage = 0; " +
                "var newW = pageCount*ourW; " +
                "d.style.height = ourH+'px';" +
                "d.style.width = newW+'px';" +
                "d.style.webkitColumnGap = '3px'; " +
                "d.style.margin = 0; " +
                "d.style.webkitColumnCount = pageCount;" +
                "}";
        String js_new = "function initialize() { " +
            "var d = document.getElementsByTagName('body')[0];" +
                    "var ourH = window.innerHeight; " +
                    "var ourW = window.innerWidth; " +
                    "var fullH = d.offsetHeight; " +
                    "var pageCount = Math.floor(fullH/ourH)+1;" +
                    "var currentPage = 0; " +
                    "var newW = pageCount*ourW; " +
                    "d.style.height = ourH+'px';" +
                    "d.style.width = newW+'px';" +
                    "d.style.webkitColumnGap = '2px'; " +
                    "d.style.margin = 1; " +
                    "d.style.webkitColumnCount = pageCount;" +
                    "}";
        String js2 = "javascript:function initialize() { " +
                "var desiredHeight;\n" +
                "var desiredWidth;\n" +
                "var bodyID = document.getElementsByTagName('body')[0];\n" +
                "totalHeight = bodyID.offsetHeight;\n" +
                "pageCount = Math.floor(totalHeight/desiredHeight) + 1;\n" +
                "bodyID.style.padding = 10;" +
                "bodyID.style.width = desiredWidth * pageCount;\n" +
                "bodyID.style.height = desiredHeight;\n" +
                "bodyID.style.WebkitColumnCount = pageCount;\n"+
                 "}";
//        if((int) Build.VERSION.SDK_INT >=19){
//            view.evaluateJavascript(js_new,null);
//            view.evaluateJavascript("initialize()",null);
//        }else {
            view.loadUrl(js);
            view.loadUrl("javascript:initialize()");
//        }
    }
}
