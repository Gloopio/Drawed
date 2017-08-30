package io.gloop.drawed.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.glxn.qrgen.android.QRCode;

import io.gloop.drawed.R;
import io.gloop.drawed.deeplink.DeepLinkActivity;
import io.gloop.drawed.model.Board;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class QRCodeDialog extends Dialog {

    public QRCodeDialog(final @NonNull Context context, Board board) {
        super(context, R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_qr_code);

        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_qr_background);
        layout.setBackgroundColor(board.getColor());

        Bitmap myBitmap = QRCode.from(DeepLinkActivity.BASE_DEEP_LINK + board.getName()).withSize(500,500).withColor(Color.WHITE, board.getColor()).bitmap();
        ImageView myImage = (ImageView) findViewById(R.id.dialog_qr_image_view);
        myImage.setImageBitmap(myBitmap);

        Button closeButton = (Button) findViewById(R.id.dialog_qr_btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
