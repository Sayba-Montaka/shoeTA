package com.example.shoeta;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * BengaliFont — loads SolaimanLipi.ttf from assets/
 * Download SolaimanLipi.ttf and place at: app/src/main/assets/SolaimanLipi.ttf
 */
public class BengaliFont {
    private static Typeface cache;

    public static Typeface get(Context ctx) {
        if (cache == null) {
            try { cache = Typeface.createFromAsset(ctx.getAssets(), "SolaimanLipi.ttf"); }
            catch (Exception e) { cache = Typeface.DEFAULT; }
        }
        return cache;
    }

    public static void apply(Context ctx, TextView... views) {
        Typeface tf = get(ctx);
        for (TextView tv : views) if (tv != null) tv.setTypeface(tf);
    }
}