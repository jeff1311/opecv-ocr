package com.ljf.opencvocr;

import io.socket.emitter.Emitter;
import io.socket.engineio.client.Socket;
import org.json.JSONObject;

public class SocketIo {

    public static void main(String[] args)  {
        Socket socket;
        try {
            socket = new Socket("ws://192.168.6.237:8080/soket");
            //socket = new Socket("ws://localhost");
//			socket.on(Socket.EVENT_OPEN, new Emitter.Listener() {
//			  @Override
//			  public void call(Object... args) {
//			    //socket.send("hi");
//			    socket.close();
//			  }
//			});
            socket.open();

            //socket.connect();

            // Receiving an object
            socket.on("broadcast event", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    System.out.println(obj.toString());
                }
            });

            JSONObject obj = new JSONObject();
            obj.put("uuid", "123");
            socket.emit("join info", obj);

            // Sending an object

            while(true) {
                System.out.println("===");
                obj = new JSONObject();
                obj.put("user_id", "xs01");
                obj.put("user_name", "xs01");
                obj.put("score", "100");

                socket.emit("broadcast event", obj);
                Thread.currentThread().sleep(1000);
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
