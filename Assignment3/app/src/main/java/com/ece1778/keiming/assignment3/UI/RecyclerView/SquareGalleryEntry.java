package com.ece1778.keiming.assignment3.UI.RecyclerView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Kei-Ming on 2015-02-09.
 */
public class SquareGalleryEntry extends RelativeLayout {
    public SquareGalleryEntry(Context context) {
        super(context);
    }

    public SquareGalleryEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGalleryEntry(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure (int widthSpec, int heightSpec) {
        // Override the onMeasure so the widthspec is passed as the height as well.
        // using setMeasured will only change parent container and not the children views as well.
        super.onMeasure(widthSpec, widthSpec);
    }
}
