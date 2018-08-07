package browser.projektb.com.rikoshae.adp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import browser.projektb.com.rikoshae.BrowserApp;
import browser.projektb.com.rikoshae.R;
import browser.projektb.com.rikoshae.model.HistoryModel;
import browser.projektb.com.rikoshae.util.Util;

/**
 * Created by User on 9/28/2016.
 */
public class HistoryAdapter extends ArrayAdapter<HistoryModel> {

    ArrayList<HistoryModel> list = new ArrayList<>();
    Context _ctx;

    public HistoryAdapter(Context ctx, ArrayList<HistoryModel> list) {
        super(ctx, R.layout.history_list_row);
        this.list = list;
        this._ctx = ctx;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View v = convertView;
        HolderView holder;

        if (v == null) {

            holder = new HolderView();
            LayoutInflater inflater = ((Activity) _ctx).getLayoutInflater();
            v = inflater.inflate(R.layout.history_list_row, parent,
                    false);
            holder.historyCheckBox = (CheckBox) v.findViewById(R.id.historyCheckBox);
            holder.title = (TextView) v.findViewById(R.id.historyTitleTextView);
            holder.historyTimeTextView = (TextView) v.findViewById(R.id.historyTimeTextView);
            holder.url = (TextView) v.findViewById(R.id.historyUrlTextView);
            holder.historyDateTextView = (TextView) v.findViewById(R.id.historyDateTextView);
            holder.historyCheckBox.setVisibility(View.VISIBLE);
            v.setTag(holder);


        } else {

            holder = (HolderView) v.getTag();
        }

        HistoryModel model = list.get(position);
        holder.title.setText(model.title);
        holder.url.setText(model.url);
        if (!model.fullDate.equalsIgnoreCase("")) {
            holder.historyDateTextView.setVisibility(View.VISIBLE);
            holder.historyDateTextView.setText(model.fullDate);
        }else
        {
            holder.historyDateTextView.setVisibility(View.GONE);
        }
        holder.historyTimeTextView.setText(Util.getTime(model.timeStamp));

        if (model.isChecked)
            holder.historyCheckBox.setChecked(true);
        else
            holder.historyCheckBox.setChecked(false);
        holder.historyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.get(position).isChecked)
                    list.get(position).isChecked = false;
                else
                    list.get(position).isChecked = true;
                ((BrowserApp)_ctx).updateHistoryList(list.get(position), position);
            }
        });
        return v;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    class HolderView {
        CheckBox historyCheckBox;
        TextView title, historyTimeTextView, url, historyDateTextView;
    }
}
