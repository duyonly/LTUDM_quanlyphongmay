package com.phongmay;

import com.phongmay.client.ServerConnection;
import com.phongmay.common.CommandType;
import com.phongmay.common.Message;
import com.phongmay.common.Response;

public class adminApp {
     public static void main(String[] args) {
        String host="localhost";
        int port=12345;
        ServerConnection connection =
                new ServerConnection(host, port);
                if (!connection.connect()) {
                    System.out.println("Không thể kết nối Server!");
                    return;
                }
        
                System.out.println("Đã kết nối Server!");
        
try{
    

   connection.connect();
   System.out.println("Đã kết nối Server!");
        Message request = new Message(
    "req001",
    CommandType.GET_CLIENTS,
    null,
    null
);

Response response =
    connection.sendRequest(request);

    if (response != null) {

        System.out.println("[SERVER]");
        System.out.println("Success: "
                + response.isSuccess());

        System.out.println("Message: "
                + response.getMessage());

        System.out.println("Data: "
                + response.getData());

    } else {
        System.out.println("Server không trả Response.");
    }

        

} catch (Exception e) {
  
    e.printStackTrace();
}
     }
    }
    

