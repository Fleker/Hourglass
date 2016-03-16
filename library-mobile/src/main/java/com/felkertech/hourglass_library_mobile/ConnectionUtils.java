package com.felkertech.hourglass_library_mobile;

/**
 * Created by guest1 on 1/10/2016.
 */

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by N on 5/29/2015.
 */
public class ConnectionUtils {

    /*
        NODE MANAGER
        These methods allow you to easily create listeners to handle finding nodes of a given capability
        Each module also requires a `values/wear.xml` which specifies the capabilities of said module.
        Specify which capability you're seeking for the given node and you're set.
        Below is an example of a capability file

        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="CAPABILITY_MOBILE">MOBILE</string>
            <string-array name="android_wear_capabilities">
                <item>@string/CAPABILITY_MOBILE</item>
            </string-array>
        </resources>

     */
    public static class NodeManager {
        private String transcriptionNodeId = null;
        private GoogleApiClient mGoogleApiClient;
        private String CAPABILITY;
        private String PATH;
        private String TAG = "hourglass_connectUtils";
        public NodeManager(final GoogleApiClient mGoogleApiClient) {
            this.mGoogleApiClient = mGoogleApiClient;
        }
        public NodeManager(final GoogleApiClient mGoogleApiClient, String CAPABILITY, String PATH) {
            this.mGoogleApiClient = mGoogleApiClient;
            this.CAPABILITY = CAPABILITY;
            this.PATH = PATH;
            setupNode();
        }
        private void setupNode() {
            Log.d(TAG, "Setting up the node mananger");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CapabilityApi.GetCapabilityResult result =
                            Wearable.CapabilityApi.getCapability(
                                    mGoogleApiClient, CAPABILITY,
                                    CapabilityApi.FILTER_REACHABLE).await();
                    updateTranscriptionCapability(result.getCapability());

                    CapabilityApi.CapabilityListener capabilityListener =
                            new CapabilityApi.CapabilityListener() {
                                @Override
                                public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                                    updateTranscriptionCapability(capabilityInfo);
                                }
                            };

                    Wearable.CapabilityApi.addCapabilityListener(
                            mGoogleApiClient,
                            capabilityListener,
                            CAPABILITY);
                }
            }).start();
        }
        private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
            if(capabilityInfo != null)
                Log.d(TAG, "Capability info: "+capabilityInfo.toString());
            else {
                Log.d(TAG, "No capability info");
                return;
            }
            Set<Node> connectedNodes = capabilityInfo.getNodes();

            transcriptionNodeId = pickBestNodeId(connectedNodes);
        }
        private String pickBestNodeId(Set<Node> nodes) {
            Log.d(TAG, "Found "+nodes.size());
            String bestNodeId = null;
            // Find a nearby node or pick one arbitrarily
            for (Node node : nodes) {
                Log.d(TAG, "Node "+node.getDisplayName()+", "+node.getId()+", "+node.isNearby());
                if (node.isNearby()) {
                    Log.d(TAG, node.getDisplayName()+" is nearby");
                    return node.getId();
                }
                bestNodeId = node.getId();
            }
            Log.d(TAG, "Picking node "+bestNodeId);
            return bestNodeId;
        }
        public void sendMessage(String msg) {
            Log.d(TAG, "Sending msg "+msg);
            byte[] voiceData = msg.getBytes();
            if (transcriptionNodeId != null) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, transcriptionNodeId,
                        PATH, voiceData).setResultCallback(
                        new ResultCallback() {
                            @Override
                            public void onResult(Result result) {
                                MessageApi.SendMessageResult sendMessageResult = (MessageApi.SendMessageResult) result;
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    // Failed to send message
                                    Log.e(TAG, "Failed to send message");
                                } else {
                                    Log.d(TAG, "Successful message sending to "+transcriptionNodeId);
                                }
                            }
                        }
                );
            } else {
                // Unable to retrieve node with transcription capability
                Log.e(TAG, "Unable to retrieve node with transcription capability");
            }
        }
        private Collection<String> getNodes() {
            HashSet<String> results = new HashSet<String>();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                results.add(node.getId());
            }
            return results;
        }
        public boolean foundNode() {
            return transcriptionNodeId != null;
        }
        public void broadcast(final String msg) {
            broadcast(msg, PATH);
        }
        public void broadcast(final String msg, final String path) {
            final byte[] voiceData = msg.getBytes();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes =
                            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        Log.d(TAG, "Node "+node.getDisplayName()+", "+node.getId()+", "+node.isNearby());
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                path, voiceData).setResultCallback(
                                new ResultCallback() {
                                    @Override
                                    public void onResult(Result result) {
                                        MessageApi.SendMessageResult sendMessageResult = (MessageApi.SendMessageResult) result;
                                        if (!sendMessageResult.getStatus().isSuccess()) {
                                            // Failed to send message
                                            Log.e(TAG, "Failed to broadcast message");
                                        } else {
                                            Log.d(TAG, "Broadcasted "+msg);
                                        }
                                    }
                                }
                        );
                    }
                }
            }).start();
        }
    }

    public static void sendLaunchCommand(final GoogleApiClient mGoogleApiClient) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for(Node node : nodes.getNodes()) {
                    Log.i("test", "pinging to: " + node.getDisplayName());
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/messageapi/launcher", "Hello World".getBytes()).await();
                    if(!result.getStatus().isSuccess()){
                        Log.e("test", "error");
                    } else {
                        Log.i("test", "success!! sent to: " + node.getDisplayName());
                    }
                }
            }
        }).start();
    }
    public static void sendData(GoogleApiClient mGoogleApiClient, String name, String type, Object value) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/dataitem/data");
        if(type.equals("boolean"))
            dataMap.getDataMap().putBoolean(name, (Boolean) value);
        if(type.equals("int"))
            dataMap.getDataMap().putInt(name, (Integer) value);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d("TAG", "onResult: " + dataItemResult.getStatus().toString());
            }
        });
    }
}