package com.bluezero.sinon;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class SightingsListAdapter extends BaseExpandableListAdapter {
	private Context _context;
	private ArrayList<SightingsListGroup> _groups;
	
	public SightingsListAdapter(Context context, ArrayList<SightingsListGroup> groups) {
		_context = context;
		_groups = groups;
	}
	
	public void addItem(SightingsListItem item, SightingsListGroup group) {
		if (!_groups.contains(group)) {
			_groups.add(group);
		}
		
		int index = _groups.indexOf(group);
		
		ArrayList<SightingsListItem> ch = _groups.get(index).Items;
		
		ch.add(item);
		
		_groups.get(index).Items = ch;
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		return _groups.get(groupPosition).Items.get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {		
		SightingsListItem child = (SightingsListItem)getChild(groupPosition, childPosition);
		
		if (view == null) {
			LayoutInflater inf = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.sightings_list_item, null);
		}
		
		TextView tv1 = (TextView)view.findViewById(android.R.id.text1);
		tv1.setText(child.Name);
		tv1.setTag(child.Id);
		
		TextView tv2 = (TextView)view.findViewById(android.R.id.text2);
		tv2.setText(child.Date.toString("hh:mm aa"));

		return view;
	}

	public int getChildrenCount(int groupPosition) {
		return _groups.get(groupPosition).Items.size();
	}

	public Object getGroup(int groupPosition) {
		return _groups.get(groupPosition);
	}

	public int getGroupCount() {
		return _groups.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {
		SightingsListGroup group = (SightingsListGroup)getGroup(groupPosition);
		if (view == null) {
			LayoutInflater inf = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.sightings_list_group, null);
		}
		
		String label = group.Name + " " + String.format("(%d)", group.Items.size());
		
		TextView tv = (TextView) view.findViewById(R.id.textViewGroup);
		tv.setText(label);

		return view;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

}


