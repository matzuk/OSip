/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.tg.osip.ui.launcher_and_registration.country.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tg.osip.utils.common.AndroidUtils;


public class LetterSectionCell extends FrameLayout {

    private TextView textView;

    public LetterSectionCell(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(AndroidUtils.dp(54), AndroidUtils.dp(64)));

        textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
//        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextColor(0xff808080);
        textView.setGravity(Gravity.CENTER);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams)textView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        textView.setLayoutParams(layoutParams);
    }

    public void setLetter(String letter) {
        textView.setText(letter.toUpperCase());
    }

    public void setCellHeight(int height) {
        setLayoutParams(new ViewGroup.LayoutParams(AndroidUtils.dp(54), height));
    }
}
