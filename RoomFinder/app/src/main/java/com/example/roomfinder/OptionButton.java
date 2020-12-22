package com.example.roomfinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by USER on 12/9/2018.
 */
public class OptionButton extends AppCompatButton {
    private int buttonIndex = 0;
    private static int buttonCount = 0;

    protected void initFeature(Context context){
        buttonCount++;
        this.buttonIndex = buttonCount;
        setTypeface(Typeface.createFromAsset(context.getAssets(), "icomoon.ttf"));
        setTextSize(25);
        setPadding(0,0,0,0);
        if ((this.buttonIndex % 2) == 0){
            setBackgroundColor(Color.parseColor("#D3D2D2"));
        }else{
            setBackgroundColor(Color.parseColor("#E8E8E8"));
        }
        setBackgroundResource(R.drawable.round_button_small);
    }

    public OptionButton(Context context) {
        super(context);
        initFeature(context);
    }

    public OptionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFeature(context);
    }

    public OptionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFeature(context);
    }



    public void setButtonText(String text){
        this.setText(text);
    }

}
