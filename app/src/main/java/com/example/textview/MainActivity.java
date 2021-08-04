package com.example.textview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public String TAG = "Main";
    private MarqueeTextView marqueeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        marqueeTextView = findViewById(R.id.scrollText);
        Editable editable = Editable.Factory.getInstance().newEditable("123456789");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editable.toString().substring(1,3));
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
        spannableStringBuilder.setSpan(foregroundColorSpan, 0,
                2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder(editable.toString().substring(3,6));
        ForegroundColorSpan foregroundColorSpan1 = new ForegroundColorSpan(Color.RED);
        spannableStringBuilder1.setSpan(foregroundColorSpan1, 0,
                2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        editable.replace(1,3,spannableStringBuilder);
        editable.replace(3,6,spannableStringBuilder1);
        marqueeTextView.setText(editable);
    }

    public void onclk(View view) {
        marqueeTextView.stopScrolling();
        Log.d(TAG, "onclk: ");
    }

    public void onclk2(View view) {
        marqueeTextView.startScrolling();
        Log.d(TAG, "onclk2: ");
    }
}