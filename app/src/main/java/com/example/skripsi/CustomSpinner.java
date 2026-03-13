package com.example.skripsi;

import android.content.Context;
import android.util.AttributeSet;

public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner {

    private OnSpinnerEventsListener mListener;
    private boolean mOpenInitiated = false;

    public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context) {
        super(context);
    }

    public interface OnSpinnerEventsListener {
        void onSpinnerOpened(androidx.appcompat.widget.AppCompatSpinner spinner);
        void onSpinnerClosed(androidx.appcompat.widget.AppCompatSpinner spinner);
    }

    @Override
    public boolean performClick() {
        // Mendeteksi saat spinner dibuka
        mOpenInitiated = true;
        if (mListener != null) {
            mListener.onSpinnerOpened(this);
        }
        return super.performClick();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mOpenInitiated && hasWindowFocus) {
            performClosedEvent();
        }
    }

    private void performClosedEvent() {
        mOpenInitiated = false;
        if (mListener != null) {
            mListener.onSpinnerClosed(this);
        }
    }

    public void setSpinnerEventsListener(OnSpinnerEventsListener onSpinnerEventsListener) {
        mListener = onSpinnerEventsListener;
    }
}