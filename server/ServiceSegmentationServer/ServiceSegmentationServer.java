package com.cangjian.server.ServiceSegmentationServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

public class ServiceSegmentationServer {
    private static ThreadPoolExecutor ReadHttpRequestPool;
    private static ThreadPoolExecutor ReadFilePool;
    private static ThreadPoolExecutor WriteHttpResponsePool;

    static {
        ReadHttpRequestPool = ThreadPools.getRead_httpRequest_Pool();
        ReadFilePool = ThreadPools.getRead_file_Pool();
        WriteHttpResponsePool = ThreadPools.getWrite_httpResponse_Pool();
    }

    public static void getFileName(Socket client){
        FileInputStream fis = null;
        try {
            InputStream is = client.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            String line = bf.readLine();

            if (line == null) {
                //Response to browser that access forbidden
                //Only simple GET requests are allowed
                LogGenerate.logger(LoggerType.FORBIDDEN, "Only simple GET requests are allowed", client);
            } else {
                System.out.println("Requested information:" + line);
                //log record

                String[] arr = line.split(" ");
                if (!arr[0].toUpperCase().equals("GET")) {
                    //Forbidden log
                    LogGenerate.logger(LoggerType.FORBIDDEN, "Only simple GET requests are allowed", client);
                }

                StringBuilder filePath = new StringBuilder("C:\\Users\\Administrator\\Desktop\\web\\");
                if (arr[1].equals("/")) {
                    filePath.append("index.html");
                } else {
                    filePath.append(arr[1].substring(1));
                }

                ThreadPools.DealReadFile dealReadFile = new ThreadPools.DealReadFile(client,filePath.toString());
                ReadFilePool.execute(dealReadFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getFileContentByFileName(String fileName,Socket client){
        FileInputStream fis;
        try {
            byte[] con=new byte[Integer.MAX_VALUE/100];
            System.out.println(fileName);
            fis = new FileInputStream(fileName);
            int len;
            byte[] bytes = new byte[2048];
            int len1=0;
            while((len = fis.read(bytes))!=-1){
                System.arraycopy(bytes, 0, con, len1, len);
                len1 = len1 + len;
            }
            //文件读取完成之后进行第三类任务的生成
            ThreadPools.DealWriteHttpResponse dealWriteHttpResponse = new ThreadPools.DealWriteHttpResponse(client,con,len1);
            //通过第三类线程池进行对于任务的执行
            WriteHttpResponsePool.execute(dealWriteHttpResponse);
        } catch (IOException fileNotFoundException) {
            //文件找不到则日志输出404页面
            LogGenerate.logger(LoggerType.NOTFOUND,"Not Found",client);
            //  fileNotFoundException.printStackTrace();
        }
    }


    public static void writeFileContentToBrowser(Socket client, byte[] fileContent, int fileLength) {
        try{
            OutputStream os = client.getOutputStream();
            os.write("HTTP/1.1 200 OK\r\n".getBytes());
            os.write("Content-Type:text/html\r\n".getBytes());
            os.write("\r\n".getBytes());
            LogGenerate.logger(LoggerType.LOG,"HTTP/1.1 200 OK\r\n",client);
            os.write(fileContent,0,fileLength);
        } catch (IOException e) {
            // e.printStackTrace();
        }
        finally {
            try {
                client.close();
            } catch (IOException e) {
                //  e.printStackTrace();
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
        if(server!=null){
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+" webserver has started...");
        }

        while(true){
            Socket client;
            try {

                assert server != null;
                client=server.accept();
                if(client!=null){

                    ThreadPools.DealReadHttpRequest dealReadHttpRequestTask=new ThreadPools.DealReadHttpRequest(client);

                    ReadHttpRequestPool.execute(dealReadHttpRequestTask);

                    System.out.println("The number of positive threads："+ReadHttpRequestPool.getActiveCount());
                }
            } catch (IOException e) {
                //   e.printStackTrace();
            }
        }
    }
}
