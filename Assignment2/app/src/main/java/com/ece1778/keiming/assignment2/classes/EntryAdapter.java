package com.ece1778.keiming.assignment2.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ece1778.keiming.assignment2.R;

import java.util.List;

public class EntryAdapter extends ArrayAdapter<DataEntry> {
    private final Context context;
    private final List<DataEntry> entries;

    static class ViewHolder {
        public TextView name;
        public TextView age;
        public TextView food;
    }
    public EntryAdapter(Context context, List<DataEntry> entries) {
        super(context, R.layout.entry_adapter, entries);
        this.context = context;
        this.entries = entries;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View listEntry = convertView;

        if (listEntry == null) {
            // Inflate the view and the adapter since there is no view that can be converter
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            listEntry = inflater.inflate(R.layout.entry_adapter, parent, false);
            // Create a new holder to reduce lookup
            ViewHolder viewHolder = new ViewHolder();

            // Populate the viewholder with the required views
            viewHolder.name = (TextView) listEntry.findViewById(R.id.eaName);
            viewHolder.age = (TextView) listEntry.findViewById(R.id.eaAge);
            viewHolder.food = (TextView) listEntry.findViewById(R.id.eaFood);

            listEntry.setTag(viewHolder);
        }

        // Fill the data of the holders from the tag.
        ViewHolder viewHolder = (ViewHolder) listEntry.getTag();
        viewHolder.name.setText(entries.get(position).getName());
        viewHolder.age.setText(Long.toString(entries.get(position).getAge()));
        viewHolder.food.setText(entries.get(position).getFood());

        return listEntry;
    }
}