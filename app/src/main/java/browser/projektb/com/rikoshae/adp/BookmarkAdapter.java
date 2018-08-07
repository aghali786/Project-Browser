package browser.projektb.com.rikoshae.adp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import browser.projektb.com.rikoshae.BrowserApp;
import browser.projektb.com.rikoshae.R;
import browser.projektb.com.rikoshae.model.HistoryModel;

/**
 * Created by User on 9/28/2016.
 */
public class BookmarkAdapter extends ArrayAdapter<HistoryModel> {

    ArrayList<HistoryModel> list = new ArrayList<>();
    Context _ctx;

    public BookmarkAdapter(Context ctx, ArrayList<HistoryModel> list) {
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
            holder.deleteBookmarkCross = (ImageView) v.findViewById(R.id.deleteBookmarkCross);
            holder.title = (TextView) v.findViewById(R.id.historyTitleTextView);
            holder.url = (TextView) v.findViewById(R.id.historyUrlTextView);
            holder.deleteBookmarkCross.setVisibility(View.VISIBLE);
            v.setTag(holder);


        } else {

            holder = (HolderView) v.getTag();
        }

        HistoryModel model = list.get(position);
        holder.title.setText(model.title);
        holder.url.setText(model.url);

        holder.deleteBookmarkCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BrowserApp)_ctx).deleteBookmarkData(list.get(position));
            }
        });
        return v;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    class HolderView {
        ImageView deleteBookmarkCross;
        TextView title, url;
    }
}
