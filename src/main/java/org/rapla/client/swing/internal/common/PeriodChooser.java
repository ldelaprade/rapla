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
package org.rapla.client.swing.internal.common;

import org.rapla.RaplaResources;
import org.rapla.components.util.DateTools;
import org.rapla.components.util.TimeInterval;
import org.rapla.entities.domain.Period;
import org.rapla.facade.RaplaFacade;
import org.rapla.facade.client.ClientFacade;
import org.rapla.facade.PeriodModel;
import org.rapla.framework.Disposable;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaInitializationException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.util.Date;
import java.util.Locale;

public class PeriodChooser extends JComboBox implements Disposable
 {
    private static final long serialVersionUID = 1L;
    
    Date selectedDate = null;
    Period selectedPeriod = null;

    public static int START_ONLY = 1;
    public static int START_AND_END = 0;
    public static int END_ONLY = -1;

    int visiblePeriods;
    RaplaResources i18n;
    PeriodModel periodModel;
    private boolean listenersEnabled = true;
    private boolean isWeekOfPeriodVisible = true;

    public PeriodChooser( RaplaResources i18n, RaplaFacade raplaFacade) throws RaplaInitializationException {
        this(i18n, raplaFacade,START_AND_END);
    }

    public PeriodChooser(RaplaResources i18n, RaplaFacade raplaFacade,int visiblePeriods) throws RaplaInitializationException {
        //      super(RaplaButton.SMALL);
        this.visiblePeriods = visiblePeriods;
        this.i18n = i18n;
        final PeriodModel periodModel;
        try
        {
            periodModel = raplaFacade.getPeriodModel();
        }
        catch (RaplaException e)
        {
            throw new RaplaInitializationException(e);
        }
        setPeriodModel(periodModel);
    }


    @SuppressWarnings("unchecked")
	public void setPeriodModel(PeriodModel model) {
        this.periodModel = model;
        if ( periodModel != null ) {
            try {
                listenersEnabled = false;
				DefaultComboBoxModel aModel = new DefaultComboBoxModel(model.getAllPeriods());
				this.setModel(aModel);
            } finally {
                listenersEnabled = true;
            }
        }
        setRenderer(new PeriodListCellRenderer());
        update();
    }
    
    public void dispose() {
        listenersEnabled = false;
    }

    private String formatPeriod(Period period) {
        if ( !isWeekOfPeriodVisible)
        {
            return period.getName();
        }

        int lastWeek = period.getWeeks();
        int week = weekOf(period,selectedDate);
        if (week != 1 && week >= lastWeek) {
            return i18n.format(
                               "period.format.end"
                               ,period.getName()
                               );
        } else {
            return i18n.periodFormatWeek(weekOf(period,selectedDate),period.getName());
        }
    }
    
    public int weekOf(Period period, Date date) {
    	Date start = period.getStart();
        if (!period.contains(date) || start == null)
            return -1;
        long duration = date.getTime() - start.getTime();
        long weeks = duration / (DateTools.MILLISECONDS_PER_WEEK);
        // setTimeInMillis has protected access in JDK 1.3.1
        final Date date1 = new Date(date.getTime() - weeks * DateTools.MILLISECONDS_PER_WEEK);
        Locale locale = i18n.getLocale();
        int week_of_year = DateTools.getWeekInYear( date1, locale);
        int week_of_year_start = DateTools.getWeekInYear( start, locale);
        return ((int)weeks) + 1
            + (((week_of_year) != week_of_year_start)? 1 :0);
    }


    private String formatPeriodList(Period period) {
        if (visiblePeriods == START_ONLY) {
            return i18n.format(
                               "period.format.start"
                               ,period.getName()
                               );
        } else if (visiblePeriods == END_ONLY) {
            return i18n.format(
                               "period.format.end"
                               ,period.getName()
                               );
        } else {
              return period.getName();
        }
    }

     public void setDate(Date date, Date endDate) {
        try {
            listenersEnabled = false;
            
            if (date != selectedDate) // Compute period only on date change 
            {
                selectedPeriod = getPeriod(date, endDate);
            }
            
            if ( selectedPeriod != null ) 
            {
                selectedDate = date;
                setSelectedItem(selectedPeriod);
            } 
            else 
            {
                selectedDate = date;
                setSelectedItem(null);
            }
            repaint();
            revalidate();
        } finally {
            listenersEnabled = true;
        }
    }

     public void setDate(Date date) {
	 setDate(date, null);
    }

    private String getSelectionText() {
        Period period = selectedPeriod;
        if ( period != null ) {
            return formatPeriod(period);
        } else {
            return i18n.getString("period.not_set");
        }
    }

    public void setSelectedPeriod(Period period) {
        selectedPeriod = period; // EXCO
        listenersEnabled = false;
        setSelectedItem(period);
        listenersEnabled = true;
        if (visiblePeriods == END_ONLY) {
            selectedDate = period.getEnd();
        } else {
            selectedDate = period.getStart();
        }
    }


    public Period getPeriod() {
        return selectedPeriod; // getPeriod(selectedDate);
    }

    private Period getPeriod(Date date, Date endDate) {
    	if (periodModel == null )
    		return null;
        if ( visiblePeriods == END_ONLY) {
            return periodModel.getNearestPeriodForEndDate(date);
        } else {
            return periodModel.getNearestPeriodForStartDate(new TimeInterval(date, endDate));
        }
    }

    public Date getDate() {
        return selectedDate;
    }

    private void update() {
        setVisible(periodModel != null && periodModel.getSize() > 0);
        setDate(getDate());
    }

    protected void fireActionEvent() {
        if ( !listenersEnabled )
        {
            return ;
        }
        Period period = (Period) getSelectedItem();
        selectedPeriod = period; // EXCO
        if (period != null) 
        {
            if (visiblePeriods == END_ONLY) {
                selectedDate = period.getEnd();
            } else {
                selectedDate = period.getStart();
            }
        }
        super.fireActionEvent();
    }


    class PeriodListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(
                                                      JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            if (index == -1) {
                value = getSelectionText();
            } else {
                Period period = (Period) value;
                value = formatPeriodList(period);
            }
            return super.getListCellRendererComponent(list,
                                                      value,
                                                      index,
                                                      isSelected,
                                                      cellHasFocus);
        }
    }


    public boolean isWeekOfPeriodVisible()
    {
        return isWeekOfPeriodVisible;
    }

    public void setWeekOfPeriodVisible( boolean isWeekOfPeriodVisible )
    {
        this.isWeekOfPeriodVisible = isWeekOfPeriodVisible;
    }

    

}
