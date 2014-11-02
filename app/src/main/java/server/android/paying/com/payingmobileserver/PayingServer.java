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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PayingServer implements Runnable {

    private Socket connection;
    public Gson gson = new GsonBuilder().create();
    public JsonParser parser = new JsonParser();
    public static JsonParser staticparser = new JsonParser();

    public static void processConn(Socket connection){
        try {
            BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
            InputStreamReader isr = new InputStreamReader(is,"UTF-8");
            int character;
            StringBuffer process = new StringBuffer();
            while ((character = isr.read()) != 13) {
                process.append((char) character);
            }

            String response = processRequestStatic(process.toString());
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
                processConn(connection);
                /*Runnable runnable = new PayingServer(connection, ++count);
                Thread thread = new Thread(runnable);
                thread.start();*/
                //new HandleRequestAsync(connection, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }


    PayingServer(Socket s, int i) {
        this.connection = s;
    }

    public String processRequest(String req){
        String responseString = "";
        JsonObject obj = (JsonObject)parser.parse(req);
        int type = obj.get("type").getAsInt();
        System.out.println("Gelen mesaj: " + req);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        try{
            if(type == 1) {
                System.out.println("Type 1");
                String tableid = obj.get("tableId").getAsString();
                response = httpclient.execute(new HttpGet("http://192.168.1.8:9000/api/restaurant/detail/" + tableid));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    System.out.println("Type 1 response: " + responseString);
                }else
                    System.out.println("TYPe 1 OK GELMEDI");
            }

            else if(type == 2){
                JsonObject card = obj.get("creditCard").getAsJsonObject();
                String cardNumber = card.get("cardNumber").getAsString();
                String userName = card.get("userName").getAsString();
                userName = userName.replace(" ","%20");
                String expireDate = card.get("expireDate").getAsString();
                String ccv = card.get("ccv").getAsString();
                String tableId = obj.get("tableId").getAsString();
                Double amountToPay = obj.get("amountToPay").getAsDouble();

                response = httpclient.execute((new HttpGet("http://192.168.1.8:9000/api/restaurant/cardPayment?" +
                        "cardNumber="+ cardNumber + "&userName?=" + userName + "&expireDate=" + expireDate + "&ccv=" + ccv +
                                "&tableId=" + tableId + "&amountToPay=" + amountToPay)));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    System.out.println(responseString);
                }
            }
        } catch( Exception e){
            e.printStackTrace();
        }

        return responseString;

    }


       public static String processRequestStatic(String req){
           String responseString = "";
           JsonObject obj = (JsonObject)staticparser.parse(req);
           int type = obj.get("type").getAsInt();
           System.out.println("Gelen mesaj: " + req);
           HttpClient httpclient = new DefaultHttpClient();
           HttpResponse response = null;
           try{
               if(type == 1) {
                   System.out.println("TYPE 1");
                   String tableid = obj.get("tableId").getAsString();
                   response = httpclient.execute(new HttpGet("http://192.168.1.8:9000/api/restaurant/detail/" + tableid));
                   StatusLine statusLine = response.getStatusLine();
                   if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                       ByteArrayOutputStream out = new ByteArrayOutputStream();
                       response.getEntity().writeTo(out);
                       out.close();
                       responseString = out.toString();
                       System.out.println("TYPE 1 response : " + responseString);
                   } else
                       System.out.println("TYPE 1 OK GELMEDI");
               }

               else if(type == 2){
                   System.out.println("TYPE 2");
                   JsonObject card = obj.get("creditCard").getAsJsonObject();
                   String cardNumber = card.get("cardNumber").getAsString();
                   String userName = card.get("userName").getAsString();
                   userName = userName.replace(" ","%20");
                   String expireDate = card.get("expireDate").getAsString();
                   String ccv = card.get("ccv").getAsString();
                   String tableId = obj.get("tableId").getAsString();
                   Double amountToPay = obj.get("amountToPay").getAsDouble();
                    String stat = "http://192.168.1.8:9000/api/restaurant/cardPayment?" +
                            "cardNumber="+ cardNumber + "&userName=" + userName + "&expireDate=" + expireDate + "&ccv=" + ccv +
                            "&tableId=" + tableId + "&amountToPay=" + amountToPay;
                   System.out.println("Yollanacak statement: " + stat);
                   response = httpclient.execute((new HttpGet(stat)));
                   StatusLine statusLine = response.getStatusLine();
                   if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                       ByteArrayOutputStream out = new ByteArrayOutputStream();
                       response.getEntity().writeTo(out);
                       out.close();
                       responseString = out.toString();
                       System.out.println(responseString);
                       System.out.println("TYPE 2 response: " + responseString);
                   }else
                       System.out.println("TYPE 2 OK GELMEDI");
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