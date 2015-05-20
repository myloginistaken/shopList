package anton.shoplist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private ArrayList<ListItem> items;
    private EditText input;
    private int STRING_LENGTH = 30;
    private String filename = "shopListCurrentState.csv";;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null || !savedInstanceState.containsKey("listItems")) {
            items = new ArrayList<>();
            //if there is a file with list items, populate this list with those previously saved items
            try {
                readFromFile();
            } catch (IOException io){
                Log.e("READ", "IO Exception");
            }
        } else {
            items = savedInstanceState.getParcelableArrayList("listItems");
        }

        input = (EditText) this.findViewById(R.id.input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    addItem(input);
                }
                return false;
            }
        });

        fillTable();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(getResources().getString(R.string.info_text));
            builder1.setCancelable(true);
            builder1.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
            return true;
        }
        if (id == R.id.action_clear){
            saveDataToFile();
            //prepare items to delete
            ArrayList newItems = new ArrayList<>();
            for(int i=0;i<items.size();i++){
                if(!items.get(i).checkItem()){
                    newItems.add(items.get(i));
                }
            }
            items = newItems;
            fillTable();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Handle data save on activity rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (items!=null) {
            outState.putParcelableArrayList("listItems", items);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        items = savedInstanceState.getParcelableArrayList("listItems");
        fillTable();
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void fillTable(){
        TableLayout tl=(TableLayout) this.findViewById(R.id.maintable);
        //first step is to clean the previous table
        tl.removeAllViews();
        if(items.size() != 0) {
            //second step is to fill it again after the change
            for (int i = 0; i < items.size(); i++) {
                TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.row, null);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                CheckedTextView chk = ((CheckedTextView) row.findViewById(R.id.check));
                chk.setId(i);
                chk.setText(cutString(items.get(i).toString()));
                Button button = ((Button) row.findViewById(R.id.showMore));
                button.setId(i);
                if(items.get(i).toString().length()>=STRING_LENGTH){
                    button.setVisibility(View.VISIBLE);
                } else {
                    button.setVisibility(View.GONE);
                }
                if(items.get(i).checkItem()){
                    chk.setChecked(true);
                    chk.setPaintFlags(chk.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    chk.setChecked(false);
                }
                tl.addView(row);
            }
        }
    }

    public void changeTableState(View v){
        CheckedTextView chk = (CheckedTextView)v;
        int chkId = chk.getId();
        chk.toggle();
        if(items.get(chkId).checkItem()){
            items.set(chkId, new ListItem(items.get(chkId).toString(), false));
            chk.setPaintFlags( chk.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        else {
            items.set(chkId, new ListItem(items.get(chkId).toString(), true));
            chk.setPaintFlags(chk.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        //update file, when the element was checked
        saveDataToFile();
    }

    public void addItem(View v){
        if(!input.getText().toString().equals("")) {
            items.add(new ListItem(input.getText().toString()));
            fillTable();
        }
        //update file, when the new element was added
        saveDataToFile();
        input.setText("");
    }

    private void saveDataToFile(){
        String data = "";
        //create file
        new File(getApplicationContext().getFilesDir(), filename);
        FileOutputStream outputStream;

        for (int i = 0; i < items.size(); i++) {
            if(!items.get(i).checkItem()) {
                data += items.get(i).toString()+'\n';
            }
        }
        //write to file
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e("SAVE", "File is missing");
        }
    }

    private void readFromFile() throws IOException{
        FileInputStream in = openFileInput(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            items.add(new ListItem(line));
        }
    }

    private String cutString(String source){
        String prepared;
        if(source.length()>=STRING_LENGTH){
            prepared = source.substring(0, STRING_LENGTH);
        } else {
            prepared = source;
        }
        return prepared;
    }
    public void showText(View v){
        Button button = (Button)v;
        int id = button.getId();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(items.get(id).toString());
        builder1.setCancelable(true);
        builder1.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
