package com.straphq.strapkit.strapkit_lib.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.straphq.strapkit.strapkit_lib.util.StrapKitConstants;
import com.straphq.strapkit.strapkit_lib.util.StrapKitJsInterface;

import java.util.List;

public class StrapKitMessageService extends Service implements DataApi.DataListener, MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = StrapKitMessageService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private StrapKitJsInterface mJsInterface;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private Handler mHandler = new Handler();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        StrapKitMessageService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StrapKitMessageService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //init();
        Log.d(TAG, "Successfully created service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        Log.d(TAG, "Initialized and started service");
        return START_STICKY;
    }

    private void init() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void initializeJs() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mJsInterface = new StrapKitJsInterface(StrapKitMessageService.this);
                mJsInterface.startWebView();
            }
        });
    }

    public void sendMessage(final String path, final String message) {
        Log.d(TAG, "Sending message: " + path + ", " + message);
        Log.d(TAG, "Google Api Client: " + mGoogleApiClient.isConnected());
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodesResult) {
                List<Node> nodes = nodesResult.getNodes();
                Log.d(TAG, "node size: " + nodes.size());
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
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Handle Google Api Client connected
        Log.d(TAG, "Successfully connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        initializeJs();
    }

    @Override
    public void onConnectionSuspended(int suspendedReason) {
        // Handle a connection suspended event
        Log.d(TAG, "Connection suspended: " + suspendedReason);
    }

    /*
    onDataChanged: will be used for getting sensor data from Android Wear if developer requests
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        Log.d(TAG, "message received: " + messageEvent.getPath());
        if (messageEvent.getPath().equals(StrapKitConstants.ACTION_START_UI)) {
            initializeJs();
        } else if (messageEvent.getPath().equals("/onClick")) {
            mJsInterface.evaluateJs(new String(messageEvent.getData()));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // The Android Wear app is not installed... we need to alert the app
            Log.d(TAG, "Wear api is not available");
        }
    }
}
