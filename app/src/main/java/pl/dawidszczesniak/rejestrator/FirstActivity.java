package pl.dawidszczesniak.rejestrator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FirstActivity extends AppCompatActivity {

    Button button10, button11, button12;
    EditText editText, editText1, editText2;
    String url2 = "jdbc:mysql://";

    String url,user,pass;

    private static final String PREFERENCES_NAME = "myPreferences";
    private static final String PREFERENCES_TEXT_FIELD = "textField";
    private static final String PREFERENCES_TEXT_FIELD1 = "textField1";
    private static final String PREFERENCES_TEXT_FIELD2 = "textField2";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        preferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);

        button10 = (Button) findViewById(R.id.button10);
        button11 = (Button) findViewById(R.id.button11);
        button12 = (Button) findViewById(R.id.button12);
        editText=(EditText) findViewById(R.id.editText);
        editText1=(EditText) findViewById(R.id.editText1);
        editText2=(EditText) findViewById(R.id.editText2);

        button12.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {

                url=editText.getText().toString();

                user=editText1.getText().toString();

                pass=editText2.getText().toString();

                Intent intent = new Intent(FirstActivity.this, MapsActivity.class);
                intent.putExtra("url_value", url2+url);
                intent.putExtra("user_value", user);
                intent.putExtra("pass_value", pass);
                startActivity(intent);

            }
        });

        button10.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {

                saveData();
                showToast("Dane zapisane");

            }
        });

        button11.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {

                loadData();

            }
        });

    }

    private void saveData() {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        String editTextData = editText.getText().toString();
        String editTextData1 = editText1.getText().toString();
        String editTextData2 = editText2.getText().toString();
        preferencesEditor.putString(PREFERENCES_TEXT_FIELD, editTextData);
        preferencesEditor.putString(PREFERENCES_TEXT_FIELD1, editTextData1);
        preferencesEditor.putString(PREFERENCES_TEXT_FIELD2, editTextData2);
        preferencesEditor.commit();

    }

    private void loadData() {
        String textFromPreferences = preferences.getString(PREFERENCES_TEXT_FIELD, "");
        editText.setText(textFromPreferences);

        String textFromPreferences1 = preferences.getString(PREFERENCES_TEXT_FIELD1, "");
        editText1.setText(textFromPreferences1);

        String textFromPreferences2 = preferences.getString(PREFERENCES_TEXT_FIELD2, "");
        editText2.setText(textFromPreferences2);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
