package com.example.shoeta;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Calendar;
import java.util.Locale;

public class AppointmentBottomSheet extends BottomSheetDialogFragment {

    public interface OnAppointmentSetListener {
        void onAppointmentSet(String date, String time); // date=YYYY-MM-DD, time=HH:mm
    }

    private OnAppointmentSetListener listener;
    private String selectedDate = "";
    private String selectedTime = "";

    public void setOnAppointmentSetListener(OnAppointmentSetListener l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.appoinment_bottom_sheet, container, false);

        MaterialAutoCompleteTextView etDate = view.findViewById(R.id.set_date);
        MaterialAutoCompleteTextView etTime = view.findViewById(R.id.appointment_time);
        View btnSave  = view.findViewById(R.id.save);
        View btnClose = view.findViewById(R.id.close);

        // Turn these into picker triggers, not free-text fields
        etDate.setFocusable(false);
        etDate.setKeyListener(null);
        etTime.setFocusable(false);
        etTime.setKeyListener(null);

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (picker, year, month, day) -> {
                        selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                        etDate.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year));
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                    (picker, hour, minute) -> {
                        selectedTime = String.format(Locale.US, "%02d:%02d", hour, minute);
                        etTime.setText(selectedTime);
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            dialog.show();
        });

        btnClose.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(requireContext(), "তারিখ এবং সময় নির্বাচন করুন", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onAppointmentSet(selectedDate, selectedTime);
            dismiss();
        });

        return view;
    }
}