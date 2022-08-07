package com.bibta.miti;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment containing a calendar for a particular year and month.
 */
public class CalendarFragment extends Fragment {

    private CalendarAdapter mAdapter;
    private GridView mCalendar;
    private GridView mCalendarHeaders;
    private Date mCurrentDate = new Date(2000, 1, 1);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        changeTitle(view);

        // Initialize grid views

        mAdapter = new CalendarAdapter(getContext(), mCurrentDate);
        mCalendar = (GridView)view.findViewById(R.id.calendar);
        mCalendar.setAdapter(mAdapter);

        mCalendarHeaders = (GridView)view.findViewById(R.id.calendar_headers);
        mCalendarHeaders.setAdapter(new CalendarHeaderAdapter(getContext()));

        // Set vertical spacing of calendar according to display height

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int spacing = (int) (metrics.heightPixels / 800f * 17f);
        mCalendar.setVerticalSpacing(spacing);
        mCalendarHeaders.setVerticalSpacing(spacing);

        return view;
    }

    private void changeTitle(View view) {
        String nepali = NepaliTranslator.getNumber(mCurrentDate.year + "") + " "
                + NepaliTranslator.getMonth(mCurrentDate.month);

        Date eDate1 = new Date(mCurrentDate.year, mCurrentDate.month, 1).convertToEnglish();
        Date eDate2 = new Date(mCurrentDate.year, mCurrentDate.month, 26).convertToEnglish();

        String english = getEnglishMonth(eDate1.month) + "/"
                + getEnglishMonth(eDate2.month);
        english += " " + eDate1.year + (eDate1.year==eDate2.year?"":"/"+eDate2.year);

        String monthYear = nepali + "  " + english;
        ((TextView)view.findViewById(R.id.monthYear)).setText(monthYear);

        view.findViewById(R.id.calendar_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).toggleCalendar();
            }
        });
    }

    private static String getEnglishMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.MONTH, month - 1);
        return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }

    /**
     * Set year and month for this calendar.
     * @param year Year to display.
     * @param month Month to display.
     */
    public void set(int year, int month) {
        mCurrentDate.year = year;
        mCurrentDate.month = month;

        if (mAdapter != null)
            mAdapter.changeDate(mCurrentDate);
    }
}
