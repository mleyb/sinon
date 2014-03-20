package com.bluezero.sinon;

import org.joda.time.DateTime;

public class SightingsListItem {
	public int Id;
	public String Name;
	public DateTime Date;	
	public String Latitude;
    public String Longitude;

	public SightingsListItem(int id, String name, DateTime date, String latitude, String longitude) {
		Id = id;
		Name = name;
		Date = date;
        Latitude = latitude;
        Longitude = longitude;
	}
}

