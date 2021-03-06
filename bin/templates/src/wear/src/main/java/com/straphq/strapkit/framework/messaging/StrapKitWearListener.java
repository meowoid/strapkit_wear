package com.straphq.strapkit.framework.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.straphq.strapkit.framework.StrapKitSplashActivity;
import com.straphq.strapkit.framework.util.StrapKitBridge;
import com.straphq.strapkit.framework.util.StrapKitConstants;
import com.straphq.wear_sdk.Strap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by martinza on 1/6/15.
 */
public class StrapKitWearListener extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = StrapKitWearListener.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private Strap mStrap;

    public static final String RECEIVER_ON_CLICK = "com.straphq.strapkit.framework.messaging.RECEIVER_ON_CLICK";

    public static final String ARG_ON_CLICK_FUNCTION = "on_click_function";

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        IntentFilter filter = new IntentFilter();
        filter.addAction(StrapKitSplashActivity.GET_APP_INFO_FILTER);
        registerReceiver(startAppReceiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        filter.addAction(RECEIVER_ON_CLICK);
        registerReceiver(onClickReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mGoogleApiClient.disconnect();
        unregisterReceiver(startAppReceiver);
        unregisterReceiver(onClickReceiver);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        StrapKitBridge.handleMessage(this, messageEvent.getPath(), new String(messageEvent.getData()));
        if (messageEvent.getPath().equals(StrapKitConstants.ACTION_START_UI) || messageEvent.getPath().equals(StrapKitConstants.ACTION_SHOW_PAGE)) {
            Intent intent = new Intent();
            intent.setAction(StrapKitSplashActivity.READY_TO_CLOSE_FILTER);
            sendBroadcast(intent);
        } else if (messageEvent.getPath().equals(StrapKitConstants.ACTION_INITIALIZE_SENSOR_DATA)) {
            mStrap = new Strap(mGoogleApiClient, getApplicationContext(), new String(messageEvent.getData()));
            mStrap.logEvent("/init");
        } else if (messageEvent.getPath().equals(StrapKitConstants.ACTION_LOG_EVENT)) {
            String data = new String(messageEvent.getData());
            try {
                JSONObject jsonObject = new JSONObject(data);
                String event = jsonObject.getString("event");
                String eventData = jsonObject.getString("data");
                if (eventData != null && !eventData.equals("null")) {
                    try {
                        mStrap.logEvent(event, new JSONObject(eventData));
                    } catch (Exception e) {
                        try {
                            JSONObject jsonData = new JSONObject();
                            jsonData.put("data", new JSONArray(eventData));
                            mStrap.logEvent(event, jsonData);
                        } catch (Exception ex) {
                            mStrap.logEvent(event);
                        }
                    }
                } else {
                    mStrap.logEvent(event);
                }
            } catch (Exception e) {
                Log.d(TAG, "Error logging", e);
            }

        }
        Log.d(TAG, "message received: " + messageEvent.getPath());
    }

    private BroadcastReceiver startAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received sendMessage");
            sendMessage(StrapKitConstants.ACTION_START_UI, "");
        }
    };

    private BroadcastReceiver onClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "receieved on Click ");
            String function = intent.getStringExtra(ARG_ON_CLICK_FUNCTION);
            sendMessage("/onClick", function);
        }
    };

    public void sendMessage(final String path, final String message) {
        Log.d(TAG, "Sending message: " + path + ", " + message);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodesResult) {
                List<Node> nodes = nodesResult.getNodes();
                for (Node node : nodes) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes() ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.d(TAG, "Received message result" + sendMessageResult.getRequestId());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
