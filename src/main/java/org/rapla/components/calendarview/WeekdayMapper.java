/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas, Bettina Lademann                |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.components.calendarview;

import org.rapla.framework.RaplaLocale;

import java.util.LinkedHashMap;
import java.util.Map;


/** maps weekday names to Calendar.DAY_OF_WEEK.
    Example:
   <pre>
       WeekdayMapper mapper = new WeekdayMapper();
       // print name of Sunday
       System.out.println(mapper.getName(Calendar.SUNDAY));
       // Create a weekday ComboBox
       JComboBox comboBox = new JComboBox();
       comboBox.setModel(new DefaultComboBoxModel(mapper.getNames()));
       // select sunday
       comboBox.setSelectedIndex(mapper.getIndexForDay(Calendar.SUNDAY));
       // weekday == Calendar.SUNDAY
       int weekday = mapper.getDayForIndex(comboBox.getSelectedIndex());
   </pre>

*/
public class WeekdayMapper {
    String[] weekdayNames;
    int[] weekday2index;
    int[] index2weekday;
    Map<Integer,String>  map = new LinkedHashMap<>();


    public WeekdayMapper(RaplaLocale raplaLocale, int firstDayInWeek)
    {
    	int days = 7;
        weekdayNames = new String[days];
        weekday2index = new int[days+1];
        index2weekday = new int[days+1];
        final String[] weekdays = raplaLocale.getFormats().getWeekdays();
        int weekday = firstDayInWeek;
        for (int i=0;i<7;i++) {
            weekday2index[weekday] = i;
            index2weekday[i] = weekday;
            String weekdayName = weekdays[weekday];
			weekdayNames[i] = weekdayName;
            map.put(weekday, weekdayName);
            weekday++;
            if ( weekday > 7)
            {
                weekday = 1;
            }
        }
    }
    

    public String[] getNames() {
        return weekdayNames;
    }

    public String getName(int weekday) {
        return map.get( weekday);
    }

    public int dayForIndex(int index) {
        return index2weekday[index];
    }

    public int indexForDay(int weekday) {
        return weekday2index[weekday];
    }

}

