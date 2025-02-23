package test.com.anarayan.test;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchToPhoneService extends Service implements GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mWatchApiClient;
    private List<Node> nodes = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mWatchApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();
        //and actually connect it
        mWatchApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWatchApiClient.disconnect();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Which cat do we want to feed? Grab this info from INTENT
        // which was passed over when we called startService
        Bundle extras = intent.getExtras();
        StringBuilder s = new StringBuilder();

        final String name = extras.getString("test.com.anarayan.test.name");
        final String party = extras.getString("test.com.anarayan.test.party");
        final String memberId = extras.getString("test.com.anarayan.test.memberid");
        final String endOfDate = extras.getString("test.com.anarayan.test.endofdate");
        final String t;
        int val = 0;
        String newLoc;
//        if (extras.getString("random") != null) {
//            Log.d("T", "RANDOM IS 4");
//            newLoc = extras.getString("random");
//            s.append("/" + "4" + newLoc);
//        }
        if (extras.getString("random") != null) {
            //s.append(extras.getString("type"));
            s.append(extras.getString("latitude") + "/");
            s.append(extras.getString("longitude"));
            t = "/random";
        }
        else {

            s.append(name + "/" + party + "/" + memberId + "/" + endOfDate);
//            final String name = extras.getString("test.com.anarayan.test.name");
//            Log.d("T", "NAME IS: " + name);
//            if (name != null && name.equals("Senator Loni Hancock")) {
//                val = 1;
//            }
//            else if (name != null && name.equals("Senator Mary Mcilroy")) {
//                val = 2;
//            }
//            else {
//                val = 3;
//            }
//            v = "/"+val;
            t = "/name";

        }
        final String v = s.toString();
        Log.d("T", "HELLO LOSER WHAT IS END OF DATE: " + s.toString());
        // Send the message with the cat name
        new Thread(new Runnable() {
            @Override
            public void run() {
                //first, connect to the apiclient
                mWatchApiClient.connect();
                //now that you're connected, send a massage with the cat name
                sendMessage(t, v);
            }
        }).start();

        return START_STICKY;
    }

    @Override //alternate method to connecting: no longer create this in a new thread, but as a callback
    public void onConnected(Bundle bundle) {

        Log.d("T", "in onconnected");
        Wearable.NodeApi.getConnectedNodes(mWatchApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        nodes = getConnectedNodesResult.getNodes();
                        Log.d("T", "found nodes");
                        //when we find a connected node, we populate the list declared above
                        //finally, we can send a message
                        sendMessage("/1", "Senator Loni Hancock");
                        Log.d("T", "IN HERE FOR THE FIRST TIME");
                        Log.d("T", "sent");
                    }
                });

    }

    @Override //we need this to implement GoogleApiClient.ConnectionsCallback
    public void onConnectionSuspended(int i) {}

    private void sendMessage(final String path, final String text ) {
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mWatchApiClient, node.getId(), path, text.getBytes());
        }
    }

}
