package com.tg.osip.ui.messages;

import com.tg.osip.business.models.PhotoItem;

import java.util.List;

/**
 * ClickListener for message
 *
 * @author e.matsyuk
 */
public interface OnMessageClickListener {
    void onPhotoMessageClick(int clickedPosition, List<PhotoItem> photoMItemList);
}
