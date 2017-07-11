package com.greenlogger.tennis;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

public class CustomFont {
	public static Typeface REGULAR_FONT = null;

	public static void overrideFonts(final Context context, final View v) {
		if (REGULAR_FONT == null) {
			REGULAR_FONT = Typeface.createFromAsset(context.getAssets(),
					"dotrice.ttf");
		}
		if ((v instanceof CheckBox) || (v instanceof RadioButton)) { // hack http://stackoverflow.com/questions/4037795/android-spacing-between-checkbox-and-text
			final float scale = context.getResources().getDisplayMetrics().density;
			v.setPadding(v.getPaddingLeft() + (int) (10.0f * scale + 0.5f),
					v.getPaddingTop(), v.getPaddingRight(),
					v.getPaddingBottom());
		}
		try {
			if (v instanceof ViewGroup) {
				final ViewGroup vg = (ViewGroup) v;
				for (int i = 0; i < vg.getChildCount(); i++) {
					final View child = vg.getChildAt(i);
					overrideFonts(context, child);
				}
			} else if (v instanceof TextView) {
				((TextView) v).setTypeface(REGULAR_FONT);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
