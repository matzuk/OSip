package com.tg.osip.utils.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.tg.osip.utils.AndroidUtils;

import java.util.Locale;

/**
 * @author e.matsyuk
 */
public class LetterDrawable extends Drawable {

    private static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static TextPaint namePaint;
    private static TextPaint namePaintSmall;
    private static int[] arrColors = {0xffe56555, 0xfff28c48, 0xffeec764, 0xff76c84d, 0xff5fbed5, 0xff549cdd, 0xff8e85ee, 0xfff2749a};
    private static int[] arrColorsProfiles = {0xffd86f65, 0xfff69d61, 0xfffabb3c, 0xff67b35d, 0xff56a2bb, 0xff5c98cd, 0xff8c79d2, 0xfff37fa6};

    private int color;
    private StaticLayout textLayout;
    private float textWidth;
    private float textHeight;
    private float textLeft;

    private Rect bounds;
    private int id;
    private String firstName;
    private String lastName;

    public LetterDrawable(Rect rect, int id, String firstName, String lastName) {
        bounds = rect;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        if (namePaint == null) {
            namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(0xffffffff);
            namePaint.setTextSize(AndroidUtils.dp(20));

            namePaintSmall = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaintSmall.setColor(0xffffffff);
            namePaintSmall.setTextSize(AndroidUtils.dp(14));
        }

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.bounds.set(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        setInfo(id, firstName, lastName);
        int size = bounds.width();
        paint.setColor(color);
        canvas.save();
        canvas.translate(bounds.left, bounds.top);
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        if (textLayout != null) {
            canvas.translate((size - textWidth) / 2 - textLeft, (size - textHeight) / 2);
            textLayout.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        if (paint.getAlpha() != alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setInfo(int id, String firstName, String lastName) {
        color = arrColors[getColorIndex(id)];

        if (firstName == null || firstName.length() == 0) {
            firstName = lastName;
            lastName = null;
        }

        String text = "";
        if (firstName != null && firstName.length() > 0) {
            text += firstName.substring(0, 1);
        }
        if (lastName != null && lastName.length() > 0) {
            String lastch = null;
            for (int a = lastName.length() - 1; a >= 0; a--) {
                if (lastch != null && lastName.charAt(a) == ' ') {
                    break;
                }
                lastch = lastName.substring(a, a + 1);
            }
            if (Build.VERSION.SDK_INT >= 16) {
                text += "\u200C" + lastch;
            } else {
                text += lastch;
            }
        } else if (firstName != null && firstName.length() > 0) {
            for (int a = firstName.length() - 1; a >= 0; a--) {
                if (firstName.charAt(a) == ' ') {
                    if (a != firstName.length() - 1 && firstName.charAt(a + 1) != ' ') {
                        if (Build.VERSION.SDK_INT >= 16) {
                            text += "\u200C" + firstName.substring(a + 1, a + 2);
                        } else {
                            text += firstName.substring(a + 1, a + 2);
                        }
                        break;
                    }
                }
            }
        }

        if (text.length() > 0) {
            text = text.toUpperCase();
            try {
                textLayout = new StaticLayout(text, (namePaint), AndroidUtils.dp(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (textLayout.getLineCount() > 0) {
                    textLeft = textLayout.getLineLeft(0);
                    textWidth = textLayout.getLineWidth(0);
                    textHeight = textLayout.getLineBottom(0);
                }
            } catch (Exception e) {
//                FileLog.e("tmessages", e);
            }
        } else {
            textLayout = null;
        }
    }

    public static int getColorIndex(int id) {
        if (id >= 0 && id < 8) {
            return id;
        }
        try {
            String str;
            if (id >= 0) {
//                str = String.format(Locale.US, "%d%d", id, UserConfig.getClientUserId());
                str = String.format(Locale.US, "%d%d", id, 200); // FIXME
            } else {
                str = String.format(Locale.US, "%d", id);
            }
            if (str.length() > 15) {
                str = str.substring(0, 15);
            }
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes());
            int b = digest[Math.abs(id % 16)];
            if (b < 0) {
                b += 256;
            }
            return Math.abs(b) % arrColors.length;
        } catch (Exception e) {
//            FileLog.e("tmessages", e);
        }
        return id % arrColors.length;
    }

}
