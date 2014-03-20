package com.bluezero.sinon;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluezero.sinon.models.Station;

public class StationArrayAdapter extends ArrayAdapter<Station> {
	private final Context _context;
	private final List<Station> _values;
 
	public StationArrayAdapter(Context context, List<Station> values) {
		super(context, R.layout.new_sighting_item, values);
		_context = context;
		_values = values;		
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.new_sighting_item, parent, false);
		TextView textView = (TextView)rowView.findViewById(R.id.textViewStation);
		textView.setText(_values.get(position).Name);
  
		return rowView;
	}
	
	@Override
	public long getItemId(int position) {
	    return _values.get(position).Id;
	}
}
