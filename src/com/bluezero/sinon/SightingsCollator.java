package com.bluezero.sinon;

import com.bluezero.sinon.models.Sighting;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SightingsCollator {
	public static ArrayList<SightingsListGroup> GetSightingGroups(List<Sighting> sightings) {
    	ArrayList<SightingsListGroup> groups = new ArrayList<SightingsListGroup>();
    	
    	SightingsListGroup last15MinutesGroup = new SightingsListGroup(SightingsListGroup.Last15MinutesGroupName);
    	SightingsListGroup last30MinutesGroup = new SightingsListGroup(SightingsListGroup.Last30MinutesGroupName);
    	SightingsListGroup last60MinutesGroup = new SightingsListGroup(SightingsListGroup.Last60MinutesGroupName);
    	SightingsListGroup allOthersGroup = new SightingsListGroup(SightingsListGroup.AllOthersGroupName);
    	    	
    	final DateTime now = DateTime.now(DateTimeZone.forID("Europe/London"));
    	
    	for (Sighting sighting : sightings) {
    		DateTime sightingDateTime = DateTime.parse(sighting.DateTime).toDateTime(DateTimeZone.forID("Europe/London"));
    		
    		Period period = new Interval(sightingDateTime, now).toPeriod();
    		    		
    		SightingsListItem listItem = new SightingsListItem(sighting.Id, sighting.StationName, sightingDateTime, sighting.Latitude, sighting.Longitude);
    		
    		if (period.getHours() >= 1) {
    			allOthersGroup.Items.add(listItem);
    		}
    		else {
        		int minutes = period.getMinutes();
    			
    			if (minutes <= 15) {
        			last15MinutesGroup.Items.add(listItem);    			
        		}    		
        		else if (minutes <= 30) {
        			last30MinutesGroup.Items.add(listItem);
        		}
        		else if (minutes <= 60) {
        			last60MinutesGroup.Items.add(listItem);    			
        		}
    		}
    	}
    	        
    	Collections.sort(last15MinutesGroup.Items, SightingDateComparator);
    	Collections.sort(last30MinutesGroup.Items, SightingDateComparator);
    	Collections.sort(last60MinutesGroup.Items, SightingDateComparator);
    	Collections.sort(allOthersGroup.Items, SightingDateComparator);
    	
    	groups.add(last15MinutesGroup);    	
    	groups.add(last30MinutesGroup);
    	groups.add(last60MinutesGroup);
    	groups.add(allOthersGroup);
    	
        return groups;
    }
	
	public static Comparator<SightingsListItem> SightingDateComparator = new Comparator<SightingsListItem>() {
		public int compare(SightingsListItem sighting1, SightingsListItem sighting2) {

			DateTime sightingDate1 = sighting1.Date;
			DateTime sightingDate2 = sighting2.Date;

			//ascending order
			return sightingDate2.compareTo(sightingDate1);

			//descending order
			//return fruitName2.compareTo(fruitName1);
		}
	};
}
