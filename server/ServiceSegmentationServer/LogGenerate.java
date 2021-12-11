package com.cangjian.server.ServiceSegmentationServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 程序功能：读取配置文件，并进行日志输出
 * 日志初始时默认输出到 webLog_1.txt   最大容量为 5M
 * 当一个日志文件的内容容量大于最大容量时，则默认新建一个文件
 * 名字为webLog_2.txt文件中去，之后同理
 * 这些默认的东西都可以通过对log.properties中去配置
 *
 */
public class LogGenerate {
    //日志的默认大小为5K
    private static int size =5;
    //日志前面日期的默认格式如下
    private static String dateFormat="yyyy:MM:dd HH:mm:ss";
    //日志的默认输出位置
    private static String location="C:\\Users\\Administrator\\Desktop\\web\\";
    //日志功能是否打开，默认打开
    private static String control="open";
    static{
        //读取日志配置文件,由于配置文件在包路径下，所以直接基于class.getResourceAsStream
        try {
            Properties properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流,当利用以下方法进行加载时，路径前面不带/代表从当前包下面加载配置文件
            InputStream in = LogGenerate.class.getResourceAsStream("log.properties");
            // 使用properties对象加载输入流,当配置文件存在时
            if(in!=null){
                try {
                    properties.load(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //获取配置文件中日期的格式
            dateFormat= properties.getProperty("log.dateFormat");
            //获取大小
            size=Integer.parseInt(properties.getProperty("log.size"));
            //获取日志的输出位置
            location=properties.getProperty("log.location");
            //获取日志是否打开的开关
            control=properties.getProperty("log.control");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    /**
     * 功能：http服务端日志功能，同时还包含响应错误时的错误页面响应功能，如果是错误页面响应功能，在最后要对连接通道进行关闭
     * @param type     日志类型--是日志 还是错误页面的生成,用枚举来选择代表
     * @param content 日志或者错误页面的标志部分
     * @param client  连接通道
     */
    public static void logger(LoggerType type, String content, Socket client) {
        //用来存储连接后的日志信息
        StringBuilder logInf=new StringBuilder();
        try{
            switch (type){
                case FORBIDDEN:
                    client.getOutputStream().write(type.getName().getBytes());
                    client.close(); //关闭通道
                    logInf.append("Forbidden").append(content);
                    break;
                case NOTFOUND:
                    client.getOutputStream().write(type.getName().getBytes());
                    client.close(); //关闭通道
                    logInf.append("Not found").append(content);
                    break;
                case LOG:
                    logInf.append("LOG_INFO:").append(content);
                    break;
            }
            //下面为具体的日志记录，上面是日志信息的联合和错误页面的响应
            //将日志信息记录到日志文件中去，通过日志生成程序来进行写入
            LogGenerate.logGene(logInf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void logGene(String logInfo){
        //根据配置文件中的东西进行日志文件的创建，从webLog_1.txt文件开始找，直至找到一个文件的大小小于size兆的
        //这个时候才开始进行日志文件的写入，或者是日志文件的创建
        if(control.equals("open")){
            File file;
            FileWriter fileWriter=null;
            try{
                //先判断待输出的目录是否存在
                File directory=new File(location);
                if(!directory.exists()){
                    directory.mkdirs();
                }
                for (int i = 0; ; i++) {
                    file =new File(location+"webLog_"+i+".txt");
                    //先判断文件是否存在，如果不存在，则直接创建文件；如果存在，则再判断文件的大小是否超过了size兆
                    //如果超过了size兆，则直接进入下一次循环，进行新的文件的创建
                    if(!file.exists()){
                        file.createNewFile();//创建文件
                        break;
                    }
                    //如果文件存在且小于5K(即5*1024)，则退出循环后进行日志的写入
                    if(file.exists()&&file.length()<size*1024){
                        break;
                    }
                    //如果文件存在且大于5K，则进行到下一轮循环，进行下一次判断
                    if(file.exists()&&file.length()>=size*1024){
                        continue;
                    }
                }
                //退出后进行日志内容的写入
                //第一步：写入给定格式的日期-使用Date和SimpleDateFormat获取本地当前时间并制定格式
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                String log="业务分割模型  Logger-Date：" + sdf.format(d);
                //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                fileWriter = new FileWriter(file,true);
                fileWriter.write(log+"\r\n");

                //第二步：写入具体的日志内容，也即写入参数
                fileWriter.write(logInfo+"\r\n\r\n");

            }catch (IOException e){
                e.printStackTrace();
            }
            finally {
                try {
                    if(fileWriter!=null){
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}