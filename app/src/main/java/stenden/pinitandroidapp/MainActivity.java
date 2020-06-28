package stenden.pinitandroidapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import stenden.pinitandroidapp.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.eclipse.paho.android.service.*;
import org.eclipse.paho.client.mqttv3.*;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;

//Main Activity of the application
public class MainActivity extends AppCompatActivity{

    private ArrayList<String> announcements;
    private ArrayAdapter<String> announcementsAdapter;

    private ArrayList<String> agenda;
    private ArrayAdapter<String> agendaAdapter;

    private ArrayList<String> presence;
    private ArrayAdapter<String> presenceAdapter;

    private Button button;
    private int check = 0;
    private String scanContent;
    private Menu menu;
    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private EditText messageSend;
    private EditText header;
    private String userId;
    private String IdMessage;
    private String returnString;

    private String androidMessage;

    private EditText daysOnline;
    private EditText day;
    private EditText month;
    private EditText year;
    public SharedPreferences sharedpreferences;

    //Name of SharedPreference. This will be the name of the file that saves the preferences. This file will only be visible if your phone is rooted.
    public static final String mypreference = "mypref";
    //boolean "student" with a default of false
    public static  boolean student = false;
    //boolean "teacher" with a default of false
    public static final boolean teacher = false;

    //Creates the menu when application starts up
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(stenden.pinitandroidapp.R.menu.main, menu);

        this.menu = menu;

        if(check()) {

            MenuItem available = menu.findItem(R.id.available);
            available.setVisible(true);
            MenuItem post = menu.findItem(R.id.post_page);
            post.setVisible(true);
            setContentView(R.layout.availablebuttonlayout);
            checkToggleButton();
            MenuItem exitApp = menu.findItem(R.id.exit_app);
            exitApp.setVisible(false);
            MenuItem exitRss = menu.findItem(R.id.exitRss);
            exitRss.setVisible(false);

        }

        else {
            MenuItem available = menu.findItem(R.id.available);
            available.setVisible(false);
            MenuItem post = menu.findItem(R.id.post_page);
            MenuItem exitRss = menu.findItem(R.id.exitRss);
            exitRss.setVisible(false);
        }
        return true;

    }


    //Creates a layout and hides the menu when application starts up
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        connectMqtt();
        getSupportActionBar().hide();

        //maakt een nieuwe service aan
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, PinItService.class);
        startService(intent);

        announcements = new ArrayList<>();
        agenda = new ArrayList<>();
        presence = new ArrayList<>();

        check();
        setContentView(R.layout.activity_main);
    }

    public  void logout(){
        SharedPreferences sharedpreferences = getSharedPreferences(MainActivity.mypreference, Context.MODE_PRIVATE);
        sharedpreferences.edit().clear().apply();
        if(sharedpreferences.getAll().isEmpty()== true){
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }


    }

    public void connectMqtt()
    {
        options = new MqttConnectOptions();
        options.setUserName("AndroidApp");
        options.setPassword("innovate".toCharArray());
        String clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://194.171.181.139:1883",
                clientId);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    try{

                        String topic = "/PinIt/Inf/#";
                        IMqttToken subToken = client.subscribe(topic, 1);
                        subToken.setActionCallback(new IMqttActionListener(){
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken)
                            {


                            }
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                            {

                            }
                        });
                    }catch (MqttException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public boolean check() {

        //When app is started, save the name and mode
        //MODE_PRIVATE means that the preferences are private and can not be seen
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        //if the teacher boolean, with a default of false, is true, set check() on true.
        if(sharedpreferences.getBoolean("teacher",false)) {
            getSupportActionBar().show();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void sendPresenceAvailable()
    {
        String topicSend = "/PinIt/Inf/Presence/";
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String payload = androidId;
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage messageSend = new MqttMessage(encodedPayload);
            client.publish(topicSend, messageSend);

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
        Toast toast = Toast.makeText(getApplicationContext(),
                "You are now Available", Toast.LENGTH_SHORT);
        toast.show();
    }
    public void sendPresenceNotAvailable()
    {
        String topicSend = "/PinIt/Inf/Presence/";
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String payload = androidId;
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage messageSend = new MqttMessage(encodedPayload);
            client.publish(topicSend, messageSend);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    //Turns Availability on and off
    public void availableToggle(View view) {
        if (sharedpreferences.getBoolean("available", false)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean("available", false);
            editor.commit();
            sendPresenceNotAvailable();

        } else {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean("available", true);
            editor.commit();
            sendPresenceAvailable();

        }
    }

    public void goToMain_Page(){
        setContentView(R.layout.news_board);

        getSupportActionBar().show();
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        announcements.clear();
        agenda.clear();

        announcementsAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, announcements);
        ListView announcementListView = (ListView) findViewById(R.id.announcement_list);
        announcementListView.setAdapter(announcementsAdapter);



        agendaAdapter = new ArrayAdapter(this, R.layout.list_layout, agenda);
        ListView agendaListView = (ListView) findViewById(R.id.agenda_list);
        agendaListView.setAdapter(agendaAdapter);
        if(agenda.size() == 0){
            String topic = "/PinIt/Inf/AgendaRequest/" + androidId;

            String payload = "";
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        if(announcements.size() == 0){
            String topic = "/PinIt/Inf/ReminderRequest/" + androidId;

            String payload = "";
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connectMqtt();
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String AndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                if (topic.equals("/PinIt/Inf/ReminderMessages/" + AndroidID)) {

                    String[] messageArray = message.toString().split("@");
                    announcements.add("Posted By: " + messageArray[0] + "\n" + messageArray[1] + "\n" + messageArray[2] +"\n\n");
                    announcementsAdapter.notifyDataSetChanged();
                    Collections.sort(announcements, Collections.<String>reverseOrder());
                }
                if (topic.equals("/PinIt/Inf/ReminderMessagesTMP/")) {

                    String[] messageArray = message.toString().split("@");
                    announcements.add("Posted by: " + messageArray[0] + "\n"+ messageArray[1] + "\n" + messageArray[2]  +"\n\n");
                    announcementsAdapter.notifyDataSetChanged();
                    Collections.sort(announcements, Collections.<String>reverseOrder());

                }
                if (topic.equals("/PinIt/Inf/AgendaMessages/" + AndroidID)) {

                        String[] messageArray = message.toString().split("@");
                        agenda.add(messageArray[0] + "\n" + messageArray[1] + "\n" + messageArray[2]+"\n\n");
                        agendaAdapter.notifyDataSetChanged();
                        Collections.sort(agenda, Collections.<String>reverseOrder());
                }
                if (topic.equals("/PinIt/Inf/AgendaMessagesTMP/")) {
                    String[] messageArray = message.toString().split("@");
                    agenda.add("\n" + messageArray[0] + "\n" + messageArray[1] + "\n"+ messageArray[2]+"\n\n");
                    agendaAdapter.notifyDataSetChanged();
                    Collections.sort(agenda, Collections.<String>reverseOrder());


                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }


    public void goToPresence() {
        setContentView(R.layout.presence_layout);

        getSupportActionBar().show();
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        presence.clear();

        presenceAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, presence);
        ListView presenceListView = (ListView) findViewById(R.id.presence_list);
        presenceListView.setAdapter(presenceAdapter);

        if(presence.size() == 0){
            String topic = "/PinIt/Inf/PresenceRequest/" + androidId;

            String payload = "";
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connectMqtt();
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String AndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                if (topic.equals("/PinIt/Inf/PresenceState/" + AndroidID)) {

                    String[] messageArray = message.toString().split("@");
                    presence.add(messageArray[0] + "");
                    presenceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }
    //Opens Qr scanner
    public void goToScanner(View view){
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    public void loginAccept(View view){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("teacher",true);
        String[] scanContentArray = scanContent.split("@");
        editor.putString("teacherId", scanContentArray[0]);
        editor.commit();
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String topic = "/PinIt/Inf/AccountActivation/";

        try{
            MqttMessage m = new MqttMessage((userId +"@" + androidId).getBytes());
            client.publish(topic.toString(), m);

        } catch (MqttException e){
            e.printStackTrace();
        }

        goToAvailable();
        check();
    }

    public void loginDenied(View view){
        check();
        setContentView(R.layout.activity_main);
        goToScanner(view);

    }

    //Changes the scanned content and shows the result in a toast if the content is not false
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        scanContent = scanningResult.getContents();
        if (scanContent != null) {
            setContentView(R.layout.account_confirm_layout);
            userId = scanContent.substring(0, scanContent.indexOf("@"));
                    //String scanFormat = scanningResult.getFormatName();
            TextView textView = (TextView) findViewById(R.id.nameTeacher);
            textView.setText(scanContent);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public void goToPostMessage_Page(){
        if(check()){

                setContentView(R.layout.post_message_layout);

        }
    }

    //Gets precsence

    // sends Mqtt remindes to the board.
    public void sendReminder(View view)
    {
        if(check()) {
            String AndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                header = (EditText) findViewById(R.id.editText4);
                messageSend = (EditText) findViewById(R.id.editText);
                daysOnline = (EditText) findViewById(R.id.editText6);
                if (header.getText().toString().length() == 0 || messageSend.getText().toString().length() == 0 || daysOnline.getText().toString().length() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please enter a header, a message and the time you want the application visible on the board", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if ((header.getText().toString().length() >= 20) || (messageSend.getText().toString().length() >= 100)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Your header cannot contain more than 20 characters, and your message cannot contain more than 100 characters", Toast.LENGTH_LONG);
                        toast.show();
                    } else {
                        if ((Integer.parseInt(daysOnline.getText().toString()) > 366)) {
                            Toast toast = Toast.makeText(getApplicationContext(), "The maximum time you can keep a message on the PinIt board is 1 year", Toast.LENGTH_LONG);
                            toast.show();
                        } else {


                            String topic = "/PinIt/Inf/Reminder/" + AndroidID;


                            try {
                                MqttMessage m = new MqttMessage((Integer.parseInt(daysOnline.getText().toString()) + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                client.publish(topic, m);
                                Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                toast.show();
                                goToMain_Page();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
        }
    }

    //sends a new agenda message it checks the message for length and dates, if it is incorrect it wont send the message
    public void sendAgenda(View view)
    {
        if(check()) {
            String AndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            header = (EditText) findViewById(R.id.editText4);
            messageSend = (EditText) findViewById(R.id.editText);
            day = (EditText) findViewById(R.id.dag);
            month =(EditText) findViewById(R.id.maand);
            year = (EditText) findViewById(R.id.year);
            String date = null;
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (header.getText().toString().length() == 0 || messageSend.getText().toString().length() == 0 || day.getText().toString().length() == 0 ||month.getText().toString().length() == 0 ||year.getText().toString().length() == 0) {
                Toast toast = Toast.makeText(getApplicationContext(), "Please enter a header, a message and the day the event is online, so it will be visible on the board", Toast.LENGTH_LONG);
                toast.show();
            } else {
                if ((header.getText().toString().length() >= 20) || (messageSend.getText().toString().length() >= 100)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Your header cannot contain more than 20 characters, and your message cannot contain more than 100 characters", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if ((Integer.parseInt(day.getText().toString()) >= 31) || (Integer.parseInt(year.getText().toString()) < currentYear) || Integer.parseInt(month.getText().toString())> 12 ||Integer.parseInt(year.getText().toString())> (currentYear + 3)){
                        Toast toast = Toast.makeText(getApplicationContext(), " please fill in a correct date", Toast.LENGTH_LONG);
                        toast.show();
                    } else {
                        if(Integer.parseInt(day.getText().toString()) < 10 && Integer.parseInt(month.getText().toString()) < 10)
                        {
                            date = year.getText().toString() + "-0" + month.getText().toString() + "-0" + day.getText().toString();
                        }
                        else if(Integer.parseInt(day.getText().toString()) >= 10 && Integer.parseInt(month.getText().toString()) < 10)
                        {
                            date = year.getText().toString() + "-" + month.getText().toString() + "-0" + day.getText().toString();
                        }
                        else if(Integer.parseInt(day.getText().toString()) < 10 && Integer.parseInt(month.getText().toString()) >= 10)
                        {
                            date = year.getText().toString() + "-0" + month.getText().toString() + "-" + day.getText().toString();
                        }
                        else
                        {
                            date = year.getText().toString() + "-" + month.getText().toString() + "-" + day.getText().toString();
                        }

                            if (Integer.parseInt(month.getText().toString()) == 1 && Integer.parseInt(day.getText().toString()) <= 31) {


                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;
                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 2 && Integer.parseInt(day.getText().toString()) <= 27) {


                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;
                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 3 && Integer.parseInt(day.getText().toString()) <= 31) {


                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 4 && Integer.parseInt(day.getText().toString()) <= 30) {

                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 5 && Integer.parseInt(day.getText().toString()) <= 31) {


                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            } else if (Integer.parseInt(month.getText().toString()) == 6 && Integer.parseInt(day.getText().toString()) <= 30) {

                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 7 && Integer.parseInt(day.getText().toString()) <= 31) {

                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            } else if (Integer.parseInt(month.getText().toString()) == 8 && Integer.parseInt(day.getText().toString()) <= 31) {
                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            } else if (Integer.parseInt(month.getText().toString()) == 9 && Integer.parseInt(day.getText().toString()) <= 30) {
                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 10 && Integer.parseInt(day.getText().toString()) <= 31) {
                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else if (Integer.parseInt(month.getText().toString()) == 11 && Integer.parseInt(day.getText().toString()) <= 30) {
                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            } else if (Integer.parseInt(month.getText().toString()) == 12 && Integer.parseInt(day.getText().toString()) <= 31) {

                                String topic = "/PinIt/Inf/Agenda/" + AndroidID;

                                try {
                                    MqttMessage m = new MqttMessage((date + "@" + header.getText().toString() + "@" + messageSend.getText().toString()).getBytes());
                                    client.publish(topic, m);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT);
                                    toast.show();
                                    goToMain_Page();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Please enter an correct date format", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                    }
                }
            }
        }
    }
    public void goToAgenda(View view)
    {
        setContentView(R.layout.post_agenda_layout);
    }

    public void goToReminder(View view)
    {
        setContentView(R.layout.post_message_layout);
    }

    //Opens the newsboard layout and creates two listview items. Also shows the menu bar
    public void goToHome(View view){
        getSupportActionBar().show();
        MenuItem available = menu.findItem(R.id.available);
        available.setVisible(false);
        MenuItem post = menu.findItem(R.id.post_page);
        post.setVisible(false);
        setContentView(R.layout.availablebuttonlayout);
        checkToggleButton();
        goToMain_Page();
    }

    public void goToAvailable() {
        if (check()) {
            setContentView(R.layout.availablebuttonlayout);
            checkToggleButton();
        }
    }

    public void checkToggleButton()
    {
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        if (sharedpreferences.getBoolean("available", false)) {
            ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
            button.setChecked(true);
        } else {
            ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
            button.setChecked(false);
        }
        /*if(check()) {
            setContentView(R.layout.availablebuttonlayout);

            //Verstuur ding maken
            // Topic = /PinIt/Inf/PresenceRequestUser/androidId
            //
            final String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String topicSend = "/PinIt/Inf/PresenceRequestUser/" + androidId;

            String payload = androidId;
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage messageSend = new MqttMessage(encodedPayload);
                client.publish(topicSend, messageSend);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connectMqtt();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(topic.equals("/PinIt/Inf/PresenceRequestUserResponse/" + androidId))
                    {
                        if(message.toString().equals(1)) {

                        ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
                            button.setChecked(true);
                        }
                        else {
                            ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
                            button.setChecked(false);
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }*/

    }

    //Gives functionality to the different menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Goes to the available layout when pressed
        if (id == R.id.available) {
            if(check()) {
                goToAvailable();
            }

            //Closes the app when pressed
        } else if (id == R.id.exit_app) {
            if(check() == false) {
                logout();
            }


            //Goes to news board when pressed and creates two listview items
        } else if (id == R.id.main_page){

            Intent intent = new Intent(this, PinItService.class);
            startService(intent);

            goToMain_Page();

            //Goes to post message when pressed
        } else if (id == R.id.post_page){
            if(check()) {
                goToPostMessage_Page();
            }

            //Goes to present page when pressed and makes a listview items of available teachers
        } else if (id == R.id.presence_page) {

            goToPresence();


            //Goes to rss feed when pressed
        }else if (id == R.id.rss_feed) {

            final Context context = this;
            Intent intent = new Intent(context, RssFeed.class);
            context.startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

}
