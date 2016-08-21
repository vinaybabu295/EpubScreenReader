package com.example.vinay.epubscreenreader;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

public class TextRendererActivity extends AppCompatActivity {
    Button prev_btn,next_btn;
    TextView mTextView;
    CharSequence mText;
    Pagination mPagination;
    int mCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_renderer);
        prev_btn = (Button)findViewById(R.id.prev_page);
        next_btn = (Button)findViewById(R.id.next_page);
        mTextView = (TextView)findViewById(R.id.ebook_tv);
        String displayString = getIntent().getExtras().getString("display");
        mText = Html.fromHtml(displayString);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Removing layout listener to avoid multiple calls
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mPagination = new Pagination(mText,
                        mTextView.getWidth(),
                        mTextView.getHeight(),
                        mTextView.getPaint(),
                        mTextView.getLineSpacingMultiplier(),
                        mTextView.getLineSpacingExtra(),
                        mTextView.getIncludeFontPadding());
                update();
            }
        });


    }
    private void update() {
        final CharSequence text = mPagination.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }

    public void loadPrevPage(View view){
        mCurrentIndex = (mCurrentIndex > 0) ? mCurrentIndex - 1 : 0;
        update();
    }
    public void loadNextPage(View view){
        mCurrentIndex = (mCurrentIndex < mPagination.size() - 1) ? mCurrentIndex + 1 : mPagination.size() - 1;
        update();
    }
}
