package server.android.paying.com.payingmobileserver;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;

public class PayingServer implements Runnable {

    private Socket connection;
    public Gson gson = new GsonBuilder().create();
    public JsonParser parser = new JsonParser();

    public static void startServer(int port, Context context){
        int count = 0;
        try {

            ServerSocket socket1 = new ServerSocket(port);
            System.out.println("Paying Restaurant Internal Server Initialized");

            while (true) {
                Socket connection = socket1.accept();
                System.out.println("Request geldi");
                //new HandleRequestAsync(connection).execute();
                System.out.println("Paralel çalışıyor");
                Runnable runnable = new PayingServer(connection, ++count);
                Thread thread = new Thread(runnable);
                thread.start();
                //new HandleRequestAsync(connection, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

   static class HandleRequestAsync extends AsyncTask<Void, Void, String> {
        Socket connection;
       private Context context;
        public HandleRequestAsync(Socket conn, Context context){
            this.connection = conn;
            this.context = context;
        }
        @Override
        protected void onPreExecute() {

            Toast.makeText(context, "on Pre Execute", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                System.out.println("after try");
                BufferedInputStream is = new BufferedInputStream(
                        connection.getInputStream());
                InputStreamReader isr = new InputStreamReader(is,"UTF-8");
                System.out.println("Before Character");
                int character;
                StringBuffer process = new StringBuffer();
                while ((character = isr.read()) != 13 ) {
                    process.append((char) character);
                }

                String response = "Response is : " + process;
                response += (char) 13;

                System.out.println("Responsee: " + response);
                BufferedOutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(response);
                osw.flush();
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                }
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println(s);
            super.onPostExecute(s);
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        }
    }

    PayingServer(Socket s, int i) {
        this.connection = s;
    }

    public String processRequest(String req){
        String responseString = "";
        JsonObject obj = (JsonObject)parser.parse(req);
        int type = obj.get("type").getAsInt();

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        try{
            if(type == 1) {
                String tableid = obj.get("tableid").getAsString();
                response = httpclient.execute(new HttpGet("http://192.168.1.7:9000/api/restaurant/detail/" + tableid));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();

                }
            }

            else if(type == 2){
                String cardno = obj.get("cardno").getAsString();
                String cardname = obj.get("cardname").getAsString();
                String expiredate = obj.get("expiredate").getAsString();
                String ccv = obj.get("ccv").getAsString();
                String tableid = obj.get("tableid").getAsString();
                String paidamount = obj.get("paidamount").getAsString();

                response = httpclient.execute((new HttpGet("http://192.168.1.7:9000/api" + cardno + cardname + expiredate + ccv + tableid + paidamount)));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                }
            }
        } catch( Exception e){
            e.printStackTrace();
        }

        return responseString;

    }

    public void run() {
        try {
            BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
            InputStreamReader isr = new InputStreamReader(is,"UTF-8");
            int character;
            StringBuffer process = new StringBuffer();
            while ((character = isr.read()) != 13) {
                process.append((char) character);
            }

            String response = processRequest(process.toString());
            response += (char) 13;

            BufferedOutputStream os = new BufferedOutputStream(
                    connection.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(response);
            osw.flush();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
            }
        }
    }
}