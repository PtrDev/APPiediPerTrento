package com.example.appiedipertrento;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Pietro on 30/01/14.
 * Copyright Pietro 2014
 */
public class PickDate extends FragmentActivity {

    Button from_date,to_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pickdate_layout);

        from_date = (Button) findViewById(R.id.from_date_button);
        from_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment(true);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        to_date = (Button) findViewById(R.id.to_date_button);
        to_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment(false);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        Button date_ok = (Button) findViewById(R.id.date_ok);
        date_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishWithResult(true);
            }
        });
        Button date_cancel = (Button) findViewById(R.id.date_cancel);
        date_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishWithResult(false);
            }
        });

    }

    private void finishWithResult(boolean finish)
    {
        Bundle conData = new Bundle();
        conData.putString("param_result", "date_calculated");
        if (finish){
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_OK, intent);
        }else {
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }


    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        boolean first;

        public DatePickerFragment(boolean firstTime){
            first = firstTime;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            if (first)
                dialog.setTitle("Set Stating Date (oldest)");
            else
                dialog.setTitle("Set End Date (nearest)");

            return dialog;
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            // Do something with the date chosen by the user
            if (first){
                CO_Trend.calendar.set(y, m, d);
                CO_Trend.starting_date = CO_Trend.dayFormat.format(CO_Trend.calendar.getTime());
                CO_Trend.year = y;
                CO_Trend.month = m;
                CO_Trend.day = d;
                from_date.setText(CO_Trend.starting_date);
            }else {
                CO_Trend.calendar.set(y,m,d);
                CO_Trend.end_date = CO_Trend.dayFormat.format(CO_Trend.calendar.getTime());
                to_date.setText(CO_Trend.end_date);
            }

        }
    }
}
