package com.example.vinay.epubscreenreader;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jubabu on 8/21/2016.
 */
public class Pagination {
        private final boolean mIncludePad;
        private final int mWidth;
        private final int mHeight;
        private final float mSpacingMult;
        private final float mSpacingAdd;
        private final CharSequence mText;
        private final TextPaint mPaint;
        private final List<CharSequence> mPages;

        public Pagination(CharSequence text, int pageW, int pageH, TextPaint paint, float spacingMult, float spacingAdd, boolean inclidePad) {
            this.mText = text;
            this.mWidth = pageW;
            this.mHeight = pageH;
            this.mPaint = paint;
            this.mSpacingMult = spacingMult;
            this.mSpacingAdd = spacingAdd;
            this.mIncludePad = inclidePad;
            this.mPages = new ArrayList<CharSequence>();

            layout();
        }

        private void layout() {
            final StaticLayout layout = new StaticLayout(mText, mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, mIncludePad);

            final int lines = layout.getLineCount();
            final CharSequence text = layout.getText();
            int startOffset = 0;
            int height = mHeight;

            for (int i = 0; i < lines; i++) {
                if (height < layout.getLineBottom(i)) {
                    // When the layout height has been exceeded
                    addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                    startOffset = layout.getLineStart(i);
                    height = layout.getLineTop(i) + mHeight;
                }

                if (i == lines - 1) {
                    // Put the rest of the text into the last page
                    addPage(text.subSequence(startOffset, layout.getLineEnd(i)));
                    return;
                }
            }
        }

        private void addPage(CharSequence text) {
            mPages.add(text);
        }

        public int size() {
            return mPages.size();
        }

        public CharSequence get(int index) {
            return (index >= 0 && index < mPages.size()) ? mPages.get(index) : null;
        }
}
