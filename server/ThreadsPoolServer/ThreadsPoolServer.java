package com.cangjian.server.ThreadsPoolServer;

import com.cangjian.server.MultiThreadsServer.LogGenerate;
import com.cangjian.server.MultiThreadsServer.LoggerType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadsPoolServer {

    public static void service(Socket client){
        FileInputStream fis = null;
        try {
            InputStream is = client.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            String line = bf.readLine();

            if(line == null){
                //Response to browser that access forbidden
                //Only simple GET requests are allowed
                com.cangjian.server.ThreadsPoolServer.LogGenerate.logger(com.cangjian.server.ThreadsPoolServer.LoggerType.FORBIDDEN,"Only simple GET requests are allowed",client);
            }
            else{
                System.out.println("Requested information:"+line);
                //log record

                String[] arr = line.split(" ");
                if(!arr[0].toUpperCase().equals("GET")){
                    //Forbidden log
                    com.cangjian.server.ThreadsPoolServer.LogGenerate.logger(com.cangjian.server.ThreadsPoolServer.LoggerType.FORBIDDEN,"Only simple GET requests are allowed",client);
                }

                StringBuilder filePath = new StringBuilder("C:\\Users\\Administrator\\Desktop\\web\\");
                if(arr[1].equals("/")){
                    filePath.append("index.html");
                }
                else{
                    filePath.append(arr[1].substring(1));
                }
                try{
                    System.out.println(filePath.toString());
                    fis = new FileInputStream(filePath.toString());
                    OutputStream os = client.getOutputStream();

                    //The format of HTTP request
                    /**
                     * request head
                     * request row
                     *
                     * (requested data)
                     */
                    os.write("HTTP/1.1 200 OK\r\n".getBytes());
                    os.write("Content-Type:text/html\r\n".getBytes());
                    os.write("Connection:keep-alive\r\n".getBytes());
                    os.write("\r\n".getBytes());
                    //log record
                    com.cangjian.server.ThreadsPoolServer.LogGenerate.logger(com.cangjian.server.ThreadsPoolServer.LoggerType.LOG,"HTTP/1.1 200 OK\r\n",client);
                    int len;
                    byte[] bytes = new byte[2048];
                    while((len=fis.read(bytes)) != -1){
                        os.write(bytes);
                    }
                    System.out.println("The file has been transported successfully!");
                } catch (FileNotFoundException fileNotFoundException){
                    //Not found
                    com.cangjian.server.ThreadsPoolServer.LogGenerate.logger(com.cangjian.server.ThreadsPoolServer.LoggerType.NOTFOUND,filePath.append("Not Found").toString(),client);
                }

            }
        } catch (IOException e) {

        }
        finally {
            //释放资源
            try {
                if(fis!=null){
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(client!=null){
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public class Mytask implements Runnable{
        Socket client;
        public Mytask(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            service(client);
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,8,0,
                TimeUnit.SECONDS,new LinkedBlockingDeque<>());
        ServerSocket server;
        try {
            server = new ServerSocket(8081);
            if(server != null){
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " webserver has started...");
            }
            while(true){
                Socket client;
                client = server.accept();
                if(client !=null){
                    Mytask mytask = new Mytask(client);
                    threadPoolExecutor.execute(mytask);
                    System.out.println("The number of positive threads："+threadPoolExecutor.getActiveCount());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
