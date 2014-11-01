package server.android.paying.com.payingmobileserver;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

public class PayingServer implements Runnable {

    private Socket connection;

    public static void startServer(int port){
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
            }
        } catch (Exception e) {
        }

    }

   /* static class HandleRequestAsync extends AsyncTask<Void, Void, String> {
        Socket connection;
        public HandleRequestAsync(Socket conn){
            this.connection = conn;
        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                BufferedInputStream is = new BufferedInputStream(
                        connection.getInputStream());
                InputStreamReader isr = new InputStreamReader(is,"UTF-8");
                int character;
                StringBuffer process = new StringBuffer();
                while ((character = isr.read()) != 13) {
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
        }
    }*/


    PayingServer(Socket s, int i) {
        this.connection = s;
    }

    public String processRequest(String req){
        System.out.println("Request is being processed: " + req);

        return "Respond : " + req;

    }

    public void run() {
        try {
            BufferedInputStream is = new BufferedInputStream(
                    connection.getInputStream());
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