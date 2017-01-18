package com.ciriousjoker.homingcompass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

class MyAdapter extends ArrayAdapter<MyLocationItem> {

    //protected static final String TAG = MyAdapter.class.getSimpleName();

    private ArrayList<MyLocationItem> items;
    private int layoutResourceId;
    private Context context;

    MyAdapter(Context context, int layoutResourceId, ArrayList<MyLocationItem> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    @NonNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View row, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        MyAdapterHolder holder = new MyAdapterHolder();
        holder.myLocationItem = items.get(position);

        row = inflater.inflate(layoutResourceId, parent, false);


        holder.button_RemoveLocationItem = (ImageButton) row.findViewById(R.id.button_remove_location);
        holder.button_RemoveLocationItem.setTag(holder.myLocationItem);

        holder.button_RenameLocationItem = (ImageButton) row.findViewById(R.id.button_rename_location);
        holder.button_RenameLocationItem.setTag(holder.myLocationItem);


        holder.textView_MyLocation_Item = (TextView) row.findViewById(R.id.textView_MyLocation_Item);
        holder.textView_MyLocation_Item.setTag(position);

        holder.editText_MyLocation_Item = (EditText) row.findViewById(R.id.editText_MyLocation_Item);

        holder.relativeLayout = (RelativeLayout) row.findViewById(R.id.relativeLayout_Location_Item);
        holder.divider_before = row.findViewById(R.id.divider_before_location);
        holder.divider_after = row.findViewById(R.id.divider_after_location);


        setupButtonListeners(holder);
        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(MyAdapterHolder holder) {
        holder.textView_MyLocation_Item.setText(holder.myLocationItem.getName());
        holder.editText_MyLocation_Item.setText(holder.myLocationItem.getName());
    }

    private void setupButtonListeners(final MyAdapterHolder holder) {
        holder.editText_MyLocation_Item.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.myLocationItem.setName(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        holder.button_RenameLocationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.myLocationItem.isEditing()) {
                    // START EDITING
                    holder.button_RenameLocationItem.setImageResource(R.drawable.ic_done_black_24dp);
                    holder.editText_MyLocation_Item.setVisibility(View.VISIBLE);
                    holder.textView_MyLocation_Item.setVisibility(View.INVISIBLE);

                    holder.divider_before.setVisibility(View.VISIBLE);
                    holder.divider_after.setVisibility(View.VISIBLE);

                    focusTextField(holder.editText_MyLocation_Item);

                    //Log.i(TAG, "Latitude: " + holder.myLocationItem.getLatitude());
                    //Log.i(TAG, "Longitude: " + holder.myLocationItem.getLongitude());

                    holder.relativeLayout.setBackgroundColor(Color.WHITE);
                    holder.myLocationItem.setEditing(true);
                } else {
                    if (!Objects.equals(holder.editText_MyLocation_Item.getText().toString(), "")) {
                        // STOP EDITING
                        holder.button_RenameLocationItem.setImageResource(R.drawable.ic_mode_edit_black_24dp);
                        holder.editText_MyLocation_Item.setVisibility(View.INVISIBLE);
                        holder.textView_MyLocation_Item.setVisibility(View.VISIBLE);
                        holder.textView_MyLocation_Item.setText(holder.myLocationItem.getName());

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(holder.textView_MyLocation_Item.getWindowToken(), 0);

                        holder.divider_before.setVisibility(View.INVISIBLE);
                        holder.divider_after.setVisibility(View.INVISIBLE);
                        holder.relativeLayout.setBackgroundResource(R.color.colorWhiteBackground);

                        holder.myLocationItem.setEditing(false);
                        saveMyLocations();
                    } else {
                        Toast.makeText(context, R.string.notice_no_name_set, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void focusTextField(EditText editText) {
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    ArrayList<MyLocationItem> getArrayList() {
        return items;
    }

    private void saveMyLocations() {
        String MY_PREFS_FILE = context.getString(R.string.shared_pref_file);
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Type listOfObjects = new TypeToken<ArrayList<MyLocationItem>>() {
        }.getType();

        Gson gson = new Gson();
        String strObject = gson.toJson(items, listOfObjects);
        prefsEditor.putString(context.getString(R.string.shared_pref_my_locations_file), strObject);
        prefsEditor.apply();
    }

    private static class MyAdapterHolder {
        MyLocationItem myLocationItem;
        RelativeLayout relativeLayout;
        TextView textView_MyLocation_Item;
        EditText editText_MyLocation_Item;
        ImageButton button_RemoveLocationItem;
        ImageButton button_RenameLocationItem;
        View divider_before;
        View divider_after;
    }
}