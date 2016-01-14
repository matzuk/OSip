package com.tg.osip.utils.dagger2_static;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.format.Formatter;

/**
 * @author e.matsyuk
 */
public class BytesFormatter {

    public String formatFileSize(@Nullable Context context, long sizeBytes) {
        return Formatter.formatFileSize(context, sizeBytes);
    }

}
