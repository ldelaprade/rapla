package org.rapla.client.gwt.components;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.rapla.client.gwt.components.util.Function;
import org.rapla.client.gwt.components.util.GWTDateUtils;
import org.rapla.client.gwt.components.util.JQueryElement;
import org.rapla.client.gwt.components.util.JS;
import org.rapla.client.gwt.components.util.JqEvent;
import org.rapla.components.i18n.BundleManager;
import org.rapla.components.i18n.I18nLocaleFormats;

import com.google.gwt.core.client.js.JsProperty;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;

public class DateRangeComponent extends Input
{
    public interface DateRangeChangeListener
    {
        void dateRangeChanged(Date startDate, Date endDate);
    }

    @JsType(prototype = "jQuery")
    public interface DateRangePickerJquery extends JQueryElement
    {
        DateRangePickerElement daterangepicker(DateRangeOptions options);
    }

    @JsType(prototype = "jQuery")
    public interface DateRangePickerElement extends JQueryElement
    {
        DateRangePicker data(String key);
    }

    @JsType(prototype = "DateRangePicker")
    public interface DateRangePicker extends JQueryElement
    {
        @JsProperty
        void setTimePicker(boolean timePicker);

        @JsProperty
        boolean isTimePicker();

        @JsProperty
        Locale getLocale();

        void setStartDate(Date start);

        void setEndDate(Date end);

        void updateView();
    }

    @JsType
    public interface DateRangeOptions
    {
        @JsProperty
        void setLocale(Locale locale);

        @JsProperty
        Locale getLocale();

        @JsProperty
        void setTimePicker(boolean timePicker);

        @JsProperty
        boolean isTimePicker();

        @JsProperty
        void setTimePicker24Hour(boolean timePicker);

        @JsProperty
        boolean isTimePicker24Hour();

        @JsProperty
        void setShowWeekNumbers(boolean showWeekNumbers);

        @JsProperty
        boolean isShowWeekNumbers();

        @JsProperty
        void setAutoApply(boolean autoApply);

        @JsProperty
        boolean isAutoApply();

        @JsProperty
        void setTimePickerIncrement(int timePickerIncrement);

        @JsProperty
        int getTimePickerIncrement();

    }

    @JsType
    public interface Locale
    {
        @JsProperty
        void setFormat(String format);

        @JsProperty
        String getFormat();
    }

    private static final Map<Character, Character> DATE_TIME_FORMAT_MAP = new HashMap<Character, Character>();

    static
    {
        DATE_TIME_FORMAT_MAP.put('y', 'Y');
        DATE_TIME_FORMAT_MAP.put('d', 'D');
        DATE_TIME_FORMAT_MAP.put('a', 'A');
    }

    private DateRangePicker datePicker = null;
    private boolean withTime;
    private final BundleManager bundleManager;
    private final DateRangeChangeListener changeListener;

    public DateRangeComponent(BundleManager bundleManager, DateRangeChangeListener changeListener)
    {
        super(InputType.TEXT);
        this.bundleManager = bundleManager;
        this.changeListener = changeListener;
        addStyleName("inputWrapper");
        addValueChangeHandler(new ValueChangeHandler<String>()
        {
            @Override
            public void onValueChange(ValueChangeEvent<String> event)
            {
                update();
            }
        });
    }

    private void update()
    {
        String newValue = getValue();
        if (newValue != null)
        {
            String[] split = newValue.split("-");
            DateTimeFormat dateTimeFormat = getDateTimeFormat();
            Date start = dateTimeFormat.parse(split[0].trim());
            Date end = dateTimeFormat.parse(split[1].trim());
            start = GWTDateUtils.gwtDateTimeToRapla(start, start);
            end = GWTDateUtils.gwtDateTimeToRapla(end, end);
            changeListener.dateRangeChanged(start, end);
        }
    }

    @Override
    protected void onAttach()
    {
        super.onAttach();
        DateRangeOptions options = JS.createObject();
        options.setShowWeekNumbers(true);
        options.setAutoApply(false);
        options.setTimePicker(withTime);
        options.setTimePicker24Hour(!bundleManager.getFormats().isAmPmFormat());
        options.setTimePickerIncrement(5);
        options.setLocale(JS.createObject());
        options.getLocale().setFormat(getFormat(withTime));
        DateRangePickerJquery jqdp = (DateRangePickerJquery) JQueryElement.Static.$(getElement());
        DateRangePickerElement ele = jqdp.daterangepicker(options);
        ele.on("apply.daterangepicker", new Function()
        {
            @Override
            public Object call(JqEvent event, Object... params)
            {
                update();
                return null;
            }
        });
        datePicker = ele.data("daterangepicker");
        reconfigure();
    }

    public void setWithTime(boolean withTime)
    {
        this.withTime = withTime;
        if (datePicker != null)
        {
            reconfigure();
        }
    }

    private void reconfigure()
    {
        datePicker.setTimePicker(withTime);
        String format = getFormat(withTime);
        updateTime(format);
        datePicker.getLocale().setFormat(format);
        datePicker.updateView();
    }

    private String convertToJsFormat(String format)
    {
        final StringBuilder fb = new StringBuilder(format);
        for (int i = 0; i < fb.length(); i++)
        {
            if (DATE_TIME_FORMAT_MAP.containsKey(fb.charAt(i)))
            {
                fb.setCharAt(i, DATE_TIME_FORMAT_MAP.get(fb.charAt(i)));
            }
        }
        return fb.toString();
    }

    public String getFormat(boolean withTime)
    {
        final String formatPattern = getFormatPattern(withTime);
        String jsFormat = convertToJsFormat(formatPattern);
        return jsFormat;
    }

    private void updateTime(String format)
    {
        // TODO
        //        String value = getValue();
        //        String[] dates = value.split("-");
        //        String oldFormat = getFormat(!withTime);
    }

    public void updateStartEnd(Date start, Date end)
    {
        start = GWTDateUtils.raplaToGwtDateTime(start);
        end = GWTDateUtils.raplaToGwtDateTime(end);
        DateTimeFormat format = getDateTimeFormat();
        setValue((start != null ? format.format(start) : "") + " - " + (end != null ? format.format(end) : ""), false);
        if (datePicker != null)
        {
            datePicker.setStartDate(start);
            datePicker.setEndDate(end);
        }
    }

    public DateTimeFormat getDateTimeFormat()
    {
        final String pattern = getFormatPattern(withTime);
        DateTimeFormat format = DateTimeFormat.getFormat(pattern);
        return format;
    }

    public String getFormatPattern(boolean withTime)
    {
        I18nLocaleFormats formats = bundleManager.getFormats();
        final String pattern = withTime ? formats.getFormatDateShort() + " " + formats.getFormatHour() + (formats.isAmPmFormat() ? " A" : "")
                : formats.getFormatDateShort();
        return pattern;
    }
}
