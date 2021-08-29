package com.example.iot;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

public class Loading  extends Dialog {
    private final Context context;
    public Loading(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        progressBar.setProgressDrawable(context.getDrawable(R.drawable.circular_progress_bar));
        setContentView(progressBar);
    }
}
