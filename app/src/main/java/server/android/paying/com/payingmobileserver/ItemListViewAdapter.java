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

public class ItemListViewAdapter extends ArrayAdapter<Item> {
    private Context context;
    private List<Item> values;
    private int resourceId;

    public ItemListViewAdapter(Context context, int resourceId, List<Item> objects) {
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

            final TextView itemName = (TextView) convertView
                    .findViewById(R.id.item_name);

            final TextView itemQuantity = (TextView) convertView
                    .findViewById(R.id.item_quantity);

            final TextView itemPrice = (TextView) convertView
                    .findViewById(R.id.item_price);

            holder = new ViewHolder(itemName, itemQuantity, itemPrice);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.itemName.setText(values.get(position).getName());
        holder.itemQuantity.setText("X " + values.get(position).getQuantity() + "");
        holder.itemPrice.setText(values.get(position).getPrice() + " TL");

        return convertView;
    }
    static class ViewHolder {

        public final TextView itemName;
        public final TextView itemQuantity;
        public final TextView itemPrice;

        public ViewHolder(TextView itemName, TextView itemQuantity, TextView itemPrice) {
            this.itemName = itemName;
            this.itemQuantity = itemQuantity;
            this.itemPrice = itemPrice;
        }
    }
}

