import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Slave {
    private Socket socket;
    private long startTime, endtime;
    private final String filePath = "/home/jphan/IdeaProjects/DistributedCrawler/src/resources/scrapy_input.txt";

    public Slave() {
        Scanner scanner = new Scanner(System.in);
        String inputString = "";
        String[] split;
        while (true) {
            System.out.print("Input <Master IP Address>,<Port Number>: ");
            try {
                inputString = scanner.nextLine();
                split = inputString.split(",");
                socket = new Socket(split[0], Integer.parseInt(split[1]));
                createSocketInputStreamHandlerThread(socket);
                break;
            } catch (Exception e) {
                System.out.println("Invalid IP Or Port number: Try Again");
            }
        }

    }


    /**
     * This is called whenever a new socket channel is created. A new thread will constantly wait to see if new
     * input comes from that socket stream.
     */
    private void createSocketInputStreamHandlerThread(Socket socket) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // Reads the input Stream from the Socket
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String streamString = in.readLine();
                        readServerInputStream(streamString);
                        if (streamString.equals("null")) {
                            // Will Check whether the input Stream is Null - If it is null then socket connection failed
                            break;
                        }
                    } catch (Exception e) {
//                        System.out.println("Socket Closed");
                        break;
                    }
                }
                closeSocket(socket);
            }
        });
        t.start();
    }

    /**
     * This parses messages received by the socket streams. It will then call the corresponding method to
     * respond to the request. These messages are requests from other users
     */
    private void readServerInputStream(String s) {
        String[] split = s.split(":");
        String[] split2;
        if (split[0].equals("start")) {         // Read "in" or "read" from input stream
            System.out.println(s);
            split2 = split[1].split(",");
            startTime = System.currentTimeMillis();
            for(int i = 0; i<split2.length; i++){
                crawlPage(split2[i]);
            }
            finishedCrawling();
        }else {                                        // Got some other garbage - could be null or something else
            System.out.println(s);
        }
    }

    private void crawlPage(String page) {
        System.out.println("Crawling: " + page);
        saveScrapyInputToFile(filePath,page);
        String cmd = "scrapy crawl --nolog spider";
        Runtime run = Runtime.getRuntime();
        try {
            Process pr = run.exec(cmd);
            pr.waitFor();
//            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//            String line = "";
//            while ((line = buf.readLine()) != null) {
//                System.out.println(line);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Writes Tuple Space to a file
     */
    public void saveScrapyInputToFile(String filePath, String string) {
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.print(string);
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

    private void finishedCrawling(){
        endtime = System.currentTimeMillis();
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("finished:" + Long.toString(endtime - startTime));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * will close the socket.
     */
    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {
        new Slave();
    }
}
