package com.tg.osip.utils.dagger2_static;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * @author e.matsyuk
 */
public class BytesFormatterTest extends BytesFormatter {

    @Override
    public String formatFileSize(@Nullable Context context, long sizeBytes) {
        return String.valueOf(sizeBytes);
    }

}