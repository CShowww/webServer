package com.cangjian.server.ThreadsPoolServer;

public enum LoggerType {
    FORBIDDEN("HTTP/1.1 403 Forbidden\r\n" +
        "Connection: close\r\n" +
        "Content-Type: text/html\r\n\r\n" +
        "<html><head>\r\n" +
        "<title>403 Forbidden</title>\r\n" +
        "</head><body>\r\n" +
        "<h1>Forbidden</h1>\r\n " +
        "The requested URL, file type or operation is not allowed on this simple static file webserver.\r\n" +
        "</body></html>\r\n"),
    NOTFOUND("HTTP/1.1 404 Not Found\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/html\r\n\r\n" +
            "<html><head>\r\n" +
            "<title>404 Not Found</title>\r\n" +
            "</head><body>\r\n" +
            "<h1>Not found</h1>\r\n " +
            "The requested URL, file was not found on this server.\r\n" +
            "</body></html>\r\n"),
    LOG("LOG");
    private final String name;
    LoggerType(String name){
        this.name=name;
    }

    protected String getName() {
        return name;
    }
}
