# webServer
通过java实现一个简单的web服务器，其中涉及了单线程、多线程、线程池、业务分割、页面缓存与置换，涵盖了java并发编程、计算机网络、操作系统等知识。

# 项目框架
此项目一共包含了5个文件夹，分别是单线程模型、多线程模型、线程池模型、业务分割模型以及页面缓存与置换模型。每一个文件夹下都包含了一个模型类、日志类、日志类型类以及日志格式文件。

## SingleThreadServer
1. log.properties  
log.properties是日志格式的配置文件，其基本内容为:
```
#用log.size 来配置文件的默认大小，单位默认为K -注意：注释前面不要带空格
log.size=5
#用log.control 来配置日志的开关，open代表开，close代表不生成日志
log.control=open
#用log.location来配置日志文件的生成位置，如果不配置，则默认生成到以下目录
log.location=C:\\Users\\Administrator\\Desktop\\web\\
#用log.dateFormat来配置生成日志时前面附带的时间格式，默认即为前面的这种形式
log.dateFormat=yyyy-MM-dd HH:mm:ss
```
2. LogGenerate.java  
logGenerate.java文件是用来生成日志文件，其中包括了日志文件生成的位置、时间格式以及最重要的日志类型。  
规定每个日志文件的大小不能超过5M,采用追加写的方式，当某一个日志文件达到5M,重新生成一个新的日志文件继续写。
3. Loggertype.java  
LoggerType.java的主体是一个枚举类，主要的作用是确定输出文件的日志类型。
4. SingleThreadServer.java  
该文件是模型的主体文件，使用单线程处理客户端的请求，包括解析请求，从服务器端读取请求的文件以及将文件内容写到客户端。其核心代码在service方法中。
```
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
```




## MultiThreadsServer
1. 单线程最大的缺点就在于不能并发的处理客户端的请求，大多的http请求都会被阻塞在服务器端的accept()方法处，从而导致响应速度慢、用户体验不好。为了解决这个问题，在第二个实验中采用了多线程模型，这样就可以并发的处理客户端的请求了。  
2. LoggerType.java、logGenerate.java、log.properties三个文件和单线程的一致。  
3. MultiThreadsServer.java，在接收Http请求出，使用了多线程来提高并发量。
```
new Thread(() -> service(client)).start();
```


## ThreadsPoolServer
1. 多线程模型虽然很好的解决了响应速度慢的问题，但是他同样有一些缺陷，例如来一个任务就要创建一个线程，一个任务完成后就要销毁这个线程，这样的过程非常的消耗资源。针对于此，实现三提出了线程池模型，将创建好的线程放入到池子中，来任务直接处理即可。
```
ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,8,0,TimeUnit.SECONDS,new LinkedBlockingDeque<>());
```
2. 实验三采用构造函数的方法创建线程池，其中核心线程数为5，最大线程数为8，其他参数如上文所示。之所以不适用Excutor框架提供的创建线程池的方法是因为，如果是单线程线程池或者固定线程数的线程池，当任务越来越多，阻塞队列无法容纳会出现OOM的问题，如果是不设固定数量的线程池的话，来一个任务创建一个线程，最终会因线程数量太多而出现OOM的问题。  
3. LoggerType.java、logGenerate.java、log.properties三个文件和单线程的一致。 

## ServiceSegmentationServer
1. 为了能够进一步提高服务器端处理客户端请求的速度，实验四提出了业务分割模型。BS交互过程可以划分为三个步骤，第一是服务器端解析客户端的请求，第二是服务器端从自己的文件系统中读取客户端需要的资源，第三是服务器端将读取的内容写回到客户端。
2. 针对以上服务器端响应客户端请求的三个步骤，实验四一共创建三个线程池，分别来处理每一部分，这样就形成了流水线式的工作流程，并发度大大提高，响应速度也实现了质的飞跃。
```
read_httpRequest_Pool = new ThreadPoolExecutor(5,8,0,TimeUnit.SECONDS,new LinkedBlockingDeque<>());
read_file_Pool = new ThreadPoolExecutor(5,8,0, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
write_httpResponse_Pool = new ThreadPoolExecutor(5,8,0,TimeUnit.SECONDS, new LinkedBlockingDeque<>());
```

