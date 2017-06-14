package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.view.Window;
import android.widget.ImageButton;

import io.gloop.drawed.BoardDetailFragment;
import io.gloop.drawed.R;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class ColorChooserDialog extends Dialog{

    public ColorChooserDialog( BoardDetailFragment fragment) {
        super(fragment.getContext(), R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_color_chooser);

        //color buttons
        ImageButton color1Btn = (ImageButton) findViewById(R.id.color1);
        color1Btn.setOnClickListener(fragment.new ColorChangeListener(color1Btn, this));
        ImageButton color2Btn = (ImageButton) findViewById(R.id.color2);
        color2Btn.setOnClickListener(fragment.new ColorChangeListener(color2Btn, this));
        ImageButton color3Btn = (ImageButton) findViewById(R.id.color3);
        color3Btn.setOnClickListener(fragment.new ColorChangeListener(color3Btn, this));
        ImageButton color4Btn = (ImageButton) findViewById(R.id.color4);
        color4Btn.setOnClickListener(fragment.new ColorChangeListener(color4Btn, this));
        ImageButton color5Btn = (ImageButton) findViewById(R.id.color5);
        color5Btn.setOnClickListener(fragment.new ColorChangeListener(color5Btn, this));
        ImageButton color6Btn = (ImageButton) findViewById(R.id.color6);
        color6Btn.setOnClickListener(fragment.new ColorChangeListener(color6Btn, this));
        ImageButton color7Btn = (ImageButton) findViewById(R.id.color7);
        color7Btn.setOnClickListener(fragment.new ColorChangeListener(color7Btn, this));
        ImageButton color8Btn = (ImageButton) findViewById(R.id.color8);
        color8Btn.setOnClickListener(fragment.new ColorChangeListener(color8Btn, this));
        ImageButton color9Btn = (ImageButton) findViewById(R.id.color9);
        color9Btn.setOnClickListener(fragment.new ColorChangeListener(color9Btn, this));
        ImageButton color10Btn = (ImageButton) findViewById(R.id.color10);
        color10Btn.setOnClickListener(fragment.new ColorChangeListener(color10Btn, this));
        ImageButton color11Btn = (ImageButton) findViewById(R.id.color11);
        color11Btn.setOnClickListener(fragment.new ColorChangeListener(color11Btn, this));
        ImageButton color12Btn = (ImageButton) findViewById(R.id.color12);
        color12Btn.setOnClickListener(fragment.new ColorChangeListener(color12Btn, this));
    }
}

