
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    static String path = "E:/Video/Anime&Cartoon/Kamen rider-Zero-One/";
    //static String path = "E:/Video/Movie/Fast&Furious Hobbs&Shaw/";

    public static void main(String[] args) {
        Thread server = new ZeroCopyServerHandler(path);
        server.start();
    }
}

class ZeroCopyServerHandler extends Thread {

    String path;

    ZeroCopyServerHandler(String path) {
        this.path = path;
    }

    @Override
    public void run() {

        try {

            // open server
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(9876)); //port 9876
            System.out.println("Server has Open!!!");
            System.out.println(serverSocketChannel);

            while (true) {

                System.out.print("Waiting for client...\n");
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("\nNew connected from:");
                System.out.println(socketChannel);

                File inputfile = new File(path + "zeroone" + "26" + ".mp4"); //File size: 490MB.
                //File inputfile = new File(path + "{MINI Super-HQ}_Fast.&.Furious.Presents.Hobbs.&.Shaw.2019.1080p.BrRip.DTS.x264_ZEZA@CtHts-Siambit" + ".mkv");   //File size: 4GB.
                Thread requestThread = new RequestFileHandler(socketChannel, inputfile);
                requestThread.start();
            }
            //read number file
            /*ByteBuffer request_file_number = ByteBuffer.allocate(16);
            socketChannel.read(request_file_number);
            request_file_number.flip();
            this.number_file = request_file_number.getLong();*/

        } catch (IOException ex) {
            Logger.getLogger(ZeroCopyServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class RequestFileHandler extends Thread {

    SocketChannel socketChannel;
    File file;
    FileInputStream fis;
    FileChannel fileChannel;

    public RequestFileHandler(SocketChannel socketChannel, File file) {
        this.socketChannel = socketChannel;
        this.file = file;
    }

    @Override
    public void run() {
        try {

            //send file length to client
            ByteBuffer request_file_length = ByteBuffer.allocate(16);
            request_file_length.putLong(file.length());
            request_file_length.flip();
            socketChannel.write(request_file_length);

            System.out.println(file);
            FileInputStream fis = new FileInputStream(file);
            FileChannel fileChannel = fis.getChannel();
            //System.out.println(socketChannel);
            System.out.println("file length: " + file.length());

            long totalBTF = 0;

            long start_time = System.currentTimeMillis();

            System.out.println("[Zero Copy in Process...............]");

            //Tranfers file to client
            long per_tmp = -1;
            while (totalBTF < file.length()) {
                long byteTranferred = fileChannel.transferTo(totalBTF, file.length() - totalBTF, socketChannel);

                totalBTF += byteTranferred;

                //if percent not change then do not print
                if (((totalBTF / 1000) * 100) / (file.length() / 1000) > per_tmp) {
                    System.out.println("tranferFile: " + totalBTF + " " + ((totalBTF / 1000) * 100) / (file.length() / 1000) + "%");
                    per_tmp = ((totalBTF/1000)*100) / (file.length()/1000);
                }

            }
            System.out.println();

            long end_time = System.currentTimeMillis();
            long total_time = (end_time - start_time) / 1000;
            System.out.println("tranferred to done!!!: total time " + total_time + " second.");

            socketChannel.close();

        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException ex) {
            Logger.getLogger(RequestFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
