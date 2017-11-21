import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp extends JFrame {
    //визуальные компоненты
    private JLabel label1;
    private JTextField port;
    private JButton startButton;
    private JPanel panel;

    private ServerSocket serverSocket;
    private CopyOnWriteArrayList<SocketThread> socketList;//набор сокетов, каждый для одтельно подсоединения клиента


    //private OutputStream sout;
    private DataOutputStream out;
    ExecutorService executor;

    //конструктор
    public ServerApp() {
        socketList=new CopyOnWriteArrayList<>();

        setSize(300,350);
        setTitle("ChatServer");

        panel = new JPanel();
        add(panel);

        this.label1=new JLabel("Номер порта:");
        panel.add(label1);

        this.port=new JTextField("8901");
        panel.add(port);

        this.startButton=new JButton("Запустить сервер");
        panel.add(startButton);


        setVisible(true);

        this.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e){
                        System.exit(0);
                    }
                }
        );

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                start();
            }
        };

        this.startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    public static void main(String [] args){

        ServerApp serverApp=new ServerApp();

    }
    //отпраить сообщению клиенту о том, что кто-то вошел
    private void sendMessageIn(SocketThread st,String name){
        try {
            out = st.getOut();
            System.out.println("Вошел клиент "+name);
            try {
                if(st.getSocket().isConnected()) {
                    out.writeUTF("Вошел клиент " + name);
                }
            }catch (SocketException e1){
                e1.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //отпраить сообщению клиенту о том, что кто-то вышел
    private void sendMessageOut(SocketThread st, String name){
        try {
            out = st.getOut();
            System.out.print("Вышел клиент "+name);
            try {
                if(st.getSocket().isConnected()) {
                    out.writeUTF("Вышел клиент " + name);
                }
            }catch (SocketException e1){
                e1.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //отпарвить сообщения всем о том, что кто-то вошел
    public void sendMessageInAll(String name){
        for(SocketThread st:socketList){
            if(st!=null){
                sendMessageIn(st,name);
            }
        }
    }
    //отпарвить сообщения всем о том, что кто-то вышел
    public void sendMessageOutAll(String name){
        for(SocketThread st:socketList){
            if(st!=null){
                sendMessageOut(st,name);
               }
        }
    }

    private void start(){
        System.out.println("Запускаем сервер");
        int portNumber=Integer.parseInt(this.port.getText());

        try {
            serverSocket=new ServerSocket(portNumber);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int count =10;//максимальное количество подсоединений
        executor = Executors.newFixedThreadPool(count);//создаем Executor
        Socket  socket;
        SocketThread st=null;
        for(int i=0;i<count;i++){
            try {
                socket= serverSocket.accept();//ожидаем подлючения
                st=new SocketThread(socket,this);
                socketList.add(st);//добавляем клиента в список
            } catch (IOException e) {
                e.printStackTrace();
            }
            executor.execute(st);//выполняем потоки
        }
        executor.shutdown();//закрываем потоки
    }
}
