package server.android.paying.com.payingmobileserver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MainActivity extends Activity implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener {

    public static final String TAG = "SERVER_MAIN_ACTIVITY";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice device;
    ProgressDialog progressDialog;

    private BroadcastReceiver receiver = null;

    private final IntentFilter intentFilter = new IntentFilter();

    @Override
    protected void onPause(){
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        Intent intent = new Intent(this, ServerService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), MainActivity.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    public void onClickDiscover(View view) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
            }

        });
       // new SendRequestAsyncTask().execute();
    }

    class SendRequestAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this, "Client starting", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return PayingClient.sendRequest("192.168.1.8",9293, "hehshs");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, "Response is: " + s, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onClickConnect(View view) {
        Intent intent = new Intent(this, ServerService.class);
        startService(intent);
    }

    public void onClickStartServer(View view) {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub

                Toast.makeText(MainActivity.this, "Create Group on Success",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub

                Toast.makeText(MainActivity.this, "Create Group on Fail",
                        Toast.LENGTH_SHORT).show();
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChannelDisconnected() {

    }

    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {


        Toast.makeText(MainActivity.this, "askdfhaskdjhf", Toast.LENGTH_SHORT).show();
        if(peers.getDeviceList().size() > 0) {
            Toast.makeText(MainActivity.this, "onPeersAvailable", Toast.LENGTH_SHORT).show();
            device = (WifiP2pDevice)peers.getDeviceList().toArray()[0];
        }
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
