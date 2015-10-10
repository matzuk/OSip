package com.tg.osip.utils.ui.auto_loaded_views;

/**
 * Offset and limit for {@link AutoLoadedRecyclerView AutoLoadedRecyclerView channel}
 *
 * @author e.matsyuk
 */
public class OffsetAndLimit {

    private int offset;
    private int limit;

    public OffsetAndLimit(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "OffsetAndLimit{" +
                "offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
