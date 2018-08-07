package browser.projektb.com.rikoshae.util;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import browser.projektb.com.rikoshae.R;

public class CustomDialog extends Dialog {
    DialogButtonListener listener;
    String title;
    String[] buttonNames;
    TextView titleTextview, messageTextview;
    Button[] buttons = new Button[3];
    String msg;
    Context ctx;

    public CustomDialog(Context context, String title, String message, String[] buttonNames, DialogButtonListener dialogButtonListener) {

        super(context, R.style.DialogStyle);
        this.listener = dialogButtonListener;
        this.title = title;
        this.buttonNames = buttonNames;
        this.msg = message;
        this.ctx = context;
        this.setCancelable(false);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);

        buttons[0] = (Button) findViewById(R.id.firstButton);
        buttons[1] = (Button) findViewById(R.id.secondButton);
        titleTextview = (TextView) findViewById(R.id.customDialogTitleTextview);
        messageTextview = (TextView) findViewById(R.id.customDialogMessageTextview);
        if (title.equalsIgnoreCase(""))
            titleTextview.setVisibility(View.GONE);
        titleTextview.setText(title);
        messageTextview.setVisibility(View.VISIBLE);
        messageTextview.setText(msg);
        for (int i = 0; i < buttonNames.length; i++) {
            final int j = i;
            buttons[i].setVisibility(View.VISIBLE);
            buttons[i].setText(buttonNames[i]);
            buttons[i].setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        dismiss();
                        listener.onButtonClicked(buttonNames[j]);

                    }
                    else{
                        dismiss();
                    }
                }
            });

        }

    }

}
