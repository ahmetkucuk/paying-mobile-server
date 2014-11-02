package server.android.paying.com.payingmobileserver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Eren on 2.11.2014.
 */
public class TableListViewAdapter extends ArrayAdapter<Table> {
    private Context context;
    private List<Table> values;
    private int resourceId;

    public TableListViewAdapter(Context context, int resourceId, List<Table> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
        // TODO Auto-generated constructor stub
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resourceId, parent, false);

            final TextView tableID = (TextView) convertView
                    .findViewById(R.id.table_name);

            final TextView totalAmount = (TextView) convertView
                    .findViewById(R.id.total_amount);

            holder = new ViewHolder(tableID, totalAmount);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tableID.setText("Masa - " + values.get(position).getId());
        holder.totalAmount.setText( values.get(position).getTotalAmount() + " TL");

        return convertView;
    }
    static class ViewHolder {

        public final TextView tableID;
        public final TextView totalAmount;

        public ViewHolder(TextView tableID, TextView totalAmount) {
            this.tableID = tableID;
            this.totalAmount = totalAmount;
        }
    }
}


