import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class SocketThread implements Runnable {
    private InputStream sin;
    private OutputStream sout;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private String name;
    ServerApp parent;

    public SocketThread(Socket socket, ServerApp serverApp){
        this.socket=socket;
        this.parent=serverApp;


        try {
            // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиенту.
            sin = socket.getInputStream();
            sout = socket.getOutputStream();

            // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            in = new DataInputStream(sin);
            out = new DataOutputStream(sout);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public DataOutputStream getOut() {
        return out;
    }

    @Override
    public void run() {
        try {
            name=this.in.readUTF();//получаем имя пользователя с клиента
            //System.out.println("Новый пользователь: "+name);

            parent.sendMessageInAll(name);//сообщаем всем, что пользователь вошел

            while (socket.isConnected()){//ждем пока клиент подлючен
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try{
                    out.writeUTF("");
                }catch (SocketException e){
                    break;
                }

            }
            parent.sendMessageOutAll(name);//сообщаем всем, что пользователь вышел

            socket.close();
            //System.out.println("Пользователь " + name + " вышел");

        } catch (SocketException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
