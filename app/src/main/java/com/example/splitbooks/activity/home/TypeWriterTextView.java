package com.example.splitbooks.activity.home;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class TypeWriterTextView extends AppCompatTextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 70;

    private final Handler mHandler = new Handler();

    public TypeWriterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if (mIndex <= mText.length()) {
                mHandler.postDelayed(this, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;
        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}
