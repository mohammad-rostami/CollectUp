package com.collect_up.c_up.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by collect-up3 on 5/10/2016.
 */
public class CustomEditText extends EditText {
    public void setHandleDismissingKeyboard(
            handleDismissingKeyboard handleDismissingKeyboard) {
        this.handleDismissingKeyboard = handleDismissingKeyboard;
    }

    private handleDismissingKeyboard handleDismissingKeyboard;

    public interface handleDismissingKeyboard {
        public void dismissKeyboard();
    }

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            handleDismissingKeyboard.dismissKeyboard();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
