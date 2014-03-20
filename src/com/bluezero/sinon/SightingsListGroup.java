package com.bluezero.sinon;

import java.util.ArrayList;

public class SightingsListGroup {
    public static final String Last15MinutesGroupName = "Within last 15 minutes";
    public static final String Last30MinutesGroupName = "Within last 30 minutes";
    public static final String Last60MinutesGroupName = "Within last 60 minutes";
    public static final String AllOthersGroupName = "All others";

	public String Name;
	public ArrayList<SightingsListItem> Items;
	
	public SightingsListGroup(String name) {
		Name = name;
		Items = new ArrayList<SightingsListItem>();
	}
}
