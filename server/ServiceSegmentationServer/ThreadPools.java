package com.cangjian.server.ServiceSegmentationServer;

import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.Data;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
public class ThreadPools {
    //deal with the request from browser
    private static ThreadPoolExecutor read_httpRequest_Pool;
    //read the file from webserver
    private static ThreadPoolExecutor read_file_Pool;
    //response the file to browser
    private static ThreadPoolExecutor write_httpResponse_Pool;

    public static ThreadPoolExecutor getRead_httpRequest_Pool() {
        return read_httpRequest_Pool;
    }

    public static ThreadPoolExecutor getRead_file_Pool() {
        return read_file_Pool;
    }

    public static ThreadPoolExecutor getWrite_httpResponse_Pool() {
        return write_httpResponse_Pool;
    }

    //initialize
    static {
        read_httpRequest_Pool = new ThreadPoolExecutor(5,8,0,TimeUnit.SECONDS,new LinkedBlockingDeque<>());
        read_file_Pool = new ThreadPoolExecutor(5,8,0, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
        write_httpResponse_Pool = new ThreadPoolExecutor(5,8,0,TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    public static class DealReadHttpRequest implements Runnable{
        private final Socket client;
        public DealReadHttpRequest(Socket client){this.client = client;}

        @Override
        public void run() {
            //deal with the request from the browser
            ServiceSegmentationServer.getFileName(client);
        }

    }

    public static class DealReadFile implements Runnable{
        private final Socket client;
        private final String fileName;
        public DealReadFile(Socket client,String fileName){
            this.client = client;
            this.fileName = fileName;
        }


        @Override
        public void run() {
            //read the file from webserver
            ServiceSegmentationServer.getFileContentByFileName(fileName,client);
        }
    }

    public static class DealWriteHttpResponse implements Runnable{
        private final Socket client;
        private final byte[] fileContent;
        private final int fileLength;
        public DealWriteHttpResponse(Socket client,byte[] fileContent,int fileLength){
            this.client = client;
            this.fileContent = fileContent;
            this.fileLength = fileLength;
        }


        @Override
        public void run() {
            //response the file to browser
            ServiceSegmentationServer.writeFileContentToBrowser(client,fileContent,fileLength);
        }
    }
}
