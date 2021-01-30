
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    static String path_save_file = "E:/Video/";

    public static void main(String[] args) throws IOException {

        while (true) {
            Scanner sc = new Scanner(System.in);
            int mode = sc.nextInt();

            if (mode == 0) {
                System.out.println("End Copy");
                System.exit(0);

            } else if (mode == 1) {

                //Connect to server
                SocketAddress socketAddress = new InetSocketAddress("192.168.1.103", 9890);
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(socketAddress);
                System.out.println(socketChannel);

                //read file length from server
                ByteBuffer request_file_length = ByteBuffer.allocate(16);
                socketChannel.read(request_file_length);
                request_file_length.flip();
                long length = request_file_length.getLong();
                System.out.println("length: " + length);

                //tranferfrom server
                FileOutputStream fos = new FileOutputStream(path_save_file + "zerocopy" + "1" + ".mp4");
                FileChannel fileChannel = fos.getChannel();

                long start_time = System.currentTimeMillis();
                long totalByteTranferFrom = 0;
                System.out.println("Start tranferfrom server.......");
                while (totalByteTranferFrom < length) {
                    long tranferFromByteCount = fileChannel.transferFrom(socketChannel, totalByteTranferFrom, length - totalByteTranferFrom);
                    System.out.println("tranferBytecount: " + tranferFromByteCount);
                    if (tranferFromByteCount <= 0) {
                        break;
                    }
                    totalByteTranferFrom += tranferFromByteCount;
                    System.out.println("file_recieve: " + totalByteTranferFrom);
                }
                socketChannel.close();
                long end_time = System.currentTimeMillis();
                double time = (double) (end_time - start_time) / 1000;
                System.out.println("Total time copy: " + time + " second.");
                System.out.println("Done");

                System.exit(0);

            } else {
                System.out.println("Invalid mode");
                System.exit(0);
            }
        }
    }
}
