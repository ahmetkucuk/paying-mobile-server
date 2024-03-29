package server.android.paying.com.payingmobileserver;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends Activity implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener {

    public static final String TAG = "SERVER_MAIN_ACTIVITY";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice device;
    ProgressDialog progressDialog;
    private ListView tablesListView;

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

        tablesListView = (ListView)findViewById(R.id.table_list_listview);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), MainActivity.this);

        tablesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showAddCardDialog( (Table)parent.getItemAtPosition(position));
            }
        });

        new getTablesAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void showAddCardDialog(Table table) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_item_list);
        dialog.setTitle("Masa Detayi");
        //colorAlertDialogTitle(dialog, Color.RED);

        final ListView itemListView = (ListView)dialog.findViewById(R.id.item_listview);
        final Button buttonOk = (Button)dialog.findViewById(R.id.button_ok);

        final TextView amountTextView = (TextView)dialog.findViewById(R.id.amount_textview);
        final TextView paidTextView = (TextView) dialog.findViewById(R.id.amount_paid_textview);
        final TextView tobePaidTextView = (TextView)dialog.findViewById(R.id.amount_will_be_paid_textview);

        amountTextView.setText(table.getTotalAmount() + " TL");
        paidTextView.setText(table.getPaidAmount() + " TL");
        tobePaidTextView.setText((table.getTotalAmount()-table.getPaidAmount()) + " TL");

        List<Item> itemList = table.getItemList();
        itemListView.setAdapter(new ItemListViewAdapter(MainActivity.this, R.layout.items_listview, itemList));

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    class getTablesAsyncTask extends AsyncTask<Void, List<Table>, Void> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (true) {

                publishProgress(getTableListFromServer());

                try {

                    Thread.sleep(3000);

                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }
        }

        @Override
        protected void onProgressUpdate(List<Table>... values) {
            tablesListView.setAdapter(new TableListViewAdapter(MainActivity.this, R.layout.tables_listview, values[0]));
            super.onProgressUpdate(values);
        }

        public List<Table> getTableListFromServer() {
            String responseString = "";
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = null;
            try {
                response = httpclient.execute(new HttpGet(
                        "http://192.168.1.8:9000/api/restaurant/tables"));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    System.out.println("Get tables response : " + responseString);
                } else
                    System.out.println("Get tables OK gelmedi");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Type tableCollectionType = new TypeToken<List<Table>>() {
            }.getType();
            JsonObject firstElement = (JsonObject) new JsonParser()
                    .parse(responseString);
            List<Table> tables = new Gson().fromJson(
                    firstElement.getAsJsonArray("tables"), tableCollectionType);
            return tables;

        }
    }

    class SendRequestAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return PayingClient.sendRequest("192.168.1.8",9293, "hehshs");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.create_group) {
            manager.createGroup(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    // TODO Auto-generated method stub
                    Toast.makeText(MainActivity.this, "Kanal Olusturuldu", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    // TODO Auto-generated method stub
                    Toast.makeText(MainActivity.this, "Hata: Kanal olusturulamadi", Toast.LENGTH_SHORT).show();

                }
            });

        } else if(id == R.id.stop_server) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

                }

                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Gurup Kaldirildi.", Toast.LENGTH_SHORT).show();
                }

            });

        } else if(id == R.id.start_server) {
            Intent intent = new Intent(this, ServerService.class);
            startService(intent);
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
                Toast.makeText(MainActivity.this, "Baglandi.", Toast.LENGTH_SHORT).show();
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


        if(peers.getDeviceList().size() > 0) {
            device = (WifiP2pDevice)peers.getDeviceList().toArray()[0];
        }
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    class CheckAccountBalanceAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (true) {

                try {

                    Thread.sleep(3000);

                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
