package browser.projektb.com.rikoshae.util;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import browser.projektb.com.rikoshae.R;

public class BookmarkPopUp extends Dialog {
    DialogButtonListener listener;
    String title;
    EditText bookmarkNameTextView, bookmarkUrlTextView;
    Button[] buttons = new Button[3];
    String msg;
    Context ctx;

    public BookmarkPopUp(Context context, String title, String message, DialogButtonListener dialogButtonListener) {

        super(context, R.style.DialogStyle);
        this.listener = dialogButtonListener;
        this.title = title;
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
        setContentView(R.layout.bookmark_pop_up);

        buttons[0] = (Button) findViewById(R.id.firstButton);
        buttons[1] = (Button) findViewById(R.id.secondButton);
        bookmarkNameTextView = (EditText) findViewById(R.id.bookmarkNameTextView);
        bookmarkUrlTextView = (EditText) findViewById(R.id.bookmarkUrlTextView);
        bookmarkNameTextView.setText(title);
        bookmarkUrlTextView.setText(msg);
        buttons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    dismiss();
            }
        });
        buttons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    dismiss();
                    listener.onButtonClicked(buttons[1].getText().toString());

                } else {
                    dismiss();
                }
            }
        });

    }

}
