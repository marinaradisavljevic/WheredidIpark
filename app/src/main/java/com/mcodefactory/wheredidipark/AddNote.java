package com.mcodefactory.wheredidipark;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class AddNote extends AppCompatActivity {

    private CheckBox garageCheckBox;
    private TextView levelLabel;
    private EditText levelNumber;
    private TextView rowLabel;
    private EditText rowNumber;
    private TextView zoneLabel;
    private EditText zoneText;
    private TextView spotNumberLabel;
    private EditText spotNumber;
    private EditText note;
    private Button saveButton;

    private boolean isInGarage;
    private String levelNumberValue;
    private String rowNumberValue;
    private String zoneValue;
    private String spotNumberValue;
    private String noteValue;

    public static final String LOCATION_NOTE_KEY = "note";
    public static final String IS_IN_GARAGE_KEY = "is_in_garage";
    public static final String LEVEL_KEY = "level";
    public static final String ROW_KEY = "row";
    public static final String ZONE_KEY = "zone";
    public static final String SPOT_NUMBER_KEY = "spot_number";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        sharedPreferences = getSharedPreferences(MapsActivity.SHARED_PREFERENCES_FILE_KEY, MODE_PRIVATE);

        isInGarage = false;
        garageCheckBox = (CheckBox)findViewById(R.id.garage_checkBox);
        garageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isInGarage = true;
                    collapseGarageViews();
                } else {
                    hideGarageViews();
                }
            }
        });
        levelLabel = (TextView)findViewById(R.id.level_label);
        levelNumber = (EditText)findViewById(R.id.level_number);
        rowLabel = (TextView)findViewById(R.id.row_label);
        rowNumber = (EditText)findViewById(R.id.row_text);
        zoneLabel = (TextView)findViewById(R.id.zone_label);
        zoneText = (EditText)findViewById(R.id.zone_text);
        spotNumberLabel = (TextView)findViewById(R.id.spot_number_label);
        spotNumber = (EditText)findViewById(R.id.spot_number);
        note = (EditText)findViewById(R.id.note_text);
        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(IS_IN_GARAGE_KEY, isInGarage);
                noteValue = note.getText().toString();
                if (!noteValue.isEmpty()) {
                    editor.putString(LOCATION_NOTE_KEY, noteValue);
                } else {
                    editor.putString(LOCATION_NOTE_KEY, MapsActivity.DEFAULT_STRING);
                }

                if (isInGarage) {
                    levelNumberValue = levelNumber.getText().toString();
                    if (!levelNumberValue.isEmpty()) {
                        editor.putString(LEVEL_KEY, levelNumberValue);
                    } else {
                        editor.putString(LEVEL_KEY, MapsActivity.DEFAULT_STRING);
                    }

                    rowNumberValue = rowNumber.getText().toString();
                    if (!rowNumberValue.isEmpty()) {
                        editor.putString(ROW_KEY, rowNumberValue);
                    } else {
                        editor.putString(ROW_KEY, MapsActivity.DEFAULT_STRING);
                    }

                    zoneValue = zoneText.getText().toString();
                    if (!zoneValue.isEmpty()) {
                        editor.putString(ZONE_KEY, zoneValue);
                    } else {
                        editor.putString(ZONE_KEY, MapsActivity.DEFAULT_STRING);
                    }

                    spotNumberValue = spotNumber.getText().toString();
                    if (!spotNumberValue.isEmpty()) {
                        editor.putString(SPOT_NUMBER_KEY, spotNumberValue);
                    } else {
                        editor.putString(SPOT_NUMBER_KEY, MapsActivity.DEFAULT_STRING);
                    }
                }
                editor.commit();
                onBackPressed();
            }
        });

        hideGarageViews();
    }

    private void collapseGarageViews() {
        levelLabel.setVisibility(View.VISIBLE);
        levelNumber.setVisibility(View.VISIBLE);
        rowLabel.setVisibility(View.VISIBLE);
        rowNumber.setVisibility(View.VISIBLE);
        zoneLabel.setVisibility(View.VISIBLE);
        zoneText.setVisibility(View.VISIBLE);
        spotNumberLabel.setVisibility(View.VISIBLE);
        spotNumber.setVisibility(View.VISIBLE);
    }

    private void hideGarageViews() {
        levelLabel.setVisibility(View.GONE);
        levelNumber.setVisibility(View.GONE);
        rowLabel.setVisibility(View.GONE);
        rowNumber.setVisibility(View.GONE);
        zoneLabel.setVisibility(View.GONE);
        zoneText.setVisibility(View.GONE);
        spotNumberLabel.setVisibility(View.GONE);
        spotNumber.setVisibility(View.GONE);
    }
}
