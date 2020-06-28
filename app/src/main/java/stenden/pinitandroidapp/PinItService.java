package stenden.pinitandroidapp;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Jeroen on 17-1-2017.
 */

// creates a new service class

public class PinItService extends Service {
    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private Looper mServiceLooper;
    private Handler mServiceHandler;



    private static Timer timer = new Timer();

    //this handles our events
    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg)
        {

            try {


                // The thread will run for 6 hours when the tread it can send new notifications
                Thread.sleep(10800000);

            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);

        }
    }


    private class sendTask extends TimerTask
    {
        public void run()
        {
            catchMessage();
        }
    }

    // sends an mqtt message when it receives a new messages it will send a notification
    public void catchMessage()
    {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String topicAgenda = "/PinIt/Inf/AgendaRequest/" + androidId;

        String payloadAgenda = "test";
        byte[] encodedPayloadAgenda = new byte[0];
        try {
            encodedPayloadAgenda = payloadAgenda.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayloadAgenda);
            client.publish(topicAgenda, message);
        }
        catch (UnsupportedEncodingException | MqttException e) {e.printStackTrace();
        }

        String topicReminder = "/PinIt/Inf/ReminderRequest/" + androidId;

        String payloadReminder = "test";
        byte[] encodedPayloadReminder = new byte[0];
        try {
            encodedPayloadReminder = payloadReminder.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayloadReminder);
            client.publish(topicReminder, message);
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
                String AndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                if (topic.contains(AndroidID)) {

                    sendNotification();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }


    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        connectMqtt();


    }
    //connects the mqqt service with the options we want.
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
                    // We are connected

                    try{
                        final String topic = "/PinIt/Inf/#";
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Runnable r = new Runnable() {
            @Override
            public void run() {

                // every half our it calls this method
                timer.scheduleAtFixedRate(new sendTask(),1800000, 1800000);
            }
        };
        Thread t = new Thread(r);
        t.start();
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    // destroys the service
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    //sends a new notification
    public void sendNotification()
    {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setContentTitle("You have a new message from PinIt")
                .setContentText("Open the app to see the message")
                .setSmallIcon(R.drawable.pinit);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }

}
