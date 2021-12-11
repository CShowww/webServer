package com.cangjian.server.SingleThreadServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SingleThreadServer {

    public static void service(Socket client){
        FileInputStream fis = null;
        try {
            InputStream is = client.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            String line = bf.readLine();

            if(line == null){
                //Response to browser that access forbidden
                //Only simple GET requests are allowed
                LogGenerate.logger(LoggerType.FORBIDDEN,"Only simple GET requests are allowed",client);
            }
            else{
                System.out.println("Requested information:"+line);
                //log record

                String[] arr = line.split(" ");
                if(!arr[0].toUpperCase().equals("GET")){
                    //Forbidden log
                    LogGenerate.logger(LoggerType.FORBIDDEN,"Only simple GET requests are allowed",client);
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
                     * request row（状态行）
                     * request head （响应头）
                     *
                     * (requested data)
                     */
                    os.write("HTTP/1.1 200 OK\r\n".getBytes());
                    os.write("Content-Type:text/html\r\n".getBytes());
                    os.write("Connection:keep-alive\r\n".getBytes());
                    os.write("\r\n".getBytes());
                    //log record
                    LogGenerate.logger(LoggerType.LOG,"HTTP/1.1 200 OK\r\n",client);
                    int len;
                    byte[] bytes = new byte[2048];
                    //数据
                    while((len=fis.read(bytes)) != -1){
                        os.write(bytes);
                    }
                    System.out.println("The file has been transported successfully!");
                } catch (FileNotFoundException fileNotFoundException){
                    //Not found
                    LogGenerate.logger(LoggerType.NOTFOUND,filePath.append("Not Found").toString(),client);
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

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(server != null){
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " webserver has started...");

            while(true){
                Socket client = null;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(client != null){
                    service(client);
                }
            }
        }

    }
}
