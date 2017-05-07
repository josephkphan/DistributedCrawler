import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;


public class Master {
    private final static int START = 1025;              // Constant - Minimum port number allowed
    private final static int END = 65525;               // Constant - Maximum Port Number allowed
    private ServerSocket listener;                      // Used to accept other socket connections
    private InetAddress ip;                             // Your IP Address
    private int portNumber;                             // Your Port Number
    private ArrayList<Socket> slaveList;
    private long startTime, endtime;
    private String dir = "/home/jphan/IdeaProjects/DistributedCrawler/src/resources/";
    private String filePath = dir + "crawl_list.txt";
    private ArrayList<Pair<String, Integer>> crawlList;
    private String[] slaveCrawlList;

    public Master() {
        slaveList = new ArrayList<>();
        crawlList = new ArrayList<>();
        createOutputDirectory();
        getCrawlList();
        createServerSocket();
        getIPAddress();
        displayIPAddress();
        createSocketListenerThread();
        waitToCrawl();
        startTime = System.currentTimeMillis();
        assignCrawlLinks();
        startCrawling();
        broadcastSlavesToStartCrawling();
        while (true) {
        }
    }

    private void createOutputDirectory(){
        File f = new File(dir);
        f.mkdir();
    }

    private void getCrawlList() {
        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] split;
            while ((line = bufferedReader.readLine()) != null) {
                split = line.split(":");
                crawlList.add(new Pair<>(split[0], Integer.parseInt(split[1])));
                System.out.println(crawlList.get(crawlList.size() - 1).getKey());
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a server socket. This will keep attempting random ports until it finds a successful one
     */
    private void createServerSocket() {
        for (int port = START; port <= END; port++) {
            try {
                port = ThreadLocalRandom.current().nextInt(START, END);
                listener = new ServerSocket(port);
                portNumber = port;                  // will only reach here on a successful Port
                break;
            } catch (IOException e) {
            }
        }
    }

    /**
     * Keeps a thread running to check whether other hosts want to add this one. On accept(), it will
     * create a new socket channel and save the socket information
     */
    private void createSocketListenerThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = listener.accept();      //blocking code - will wait until another host wants to
                        slaveList.add(socket);
                        System.out.println("\n    Slave Just Added\n    # Slaves = " + Integer.toString(slaveList.size()));
                        System.out.print("Type in \"start\" To begin Crawling: ");
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("Successfully Added, Wait to begin Crawling...");
                        createSocketInputStreamHandlerThread(socket); // connect with this host's server socket
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        t.start();
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
                        readServerInputStream(streamString, socket);
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
    private void readServerInputStream(String s, Socket socket) {
        String[] split = s.split(":");
        if (split[0].equals("finished")) {
            slaveList.remove(socket);
            closeSocket(socket);
            if (slaveList.size() == 0) {
                //todo Send completion Time too ?to log it
                endtime = System.currentTimeMillis();
                System.out.println("Total Time (ms): " + (endtime - startTime));
                System.out.println("Finished Crawling : Exiting Program");
                exit(0);
            }

        } else {                                        // Got some other garbage - could be null or something else
            System.out.println(s);
        }
    }

    /**
     * Gets and saves the IPAddress of the current host
     */
    private void getIPAddress() {
        try {
            ip = InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayIPAddress() {
        System.out.println(ip.getHostAddress() + " at port number: " + Integer.toString(portNumber));
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

    private void waitToCrawl() {
        Scanner scanner = new Scanner(System.in);
        String inputString = "";
        while (!inputString.equals("start")) {
            System.out.print("Type in \"Start\" To begin Crawling: ");
            inputString = scanner.nextLine();
            if (inputString.equals("exit")) {
                exit(0);
            } else if (!inputString.equals("start")) {
                System.out.println("Incorrect Argument, Try Again");
            }
        }
    }

    private void broadcastSlavesToStartCrawling() {
        for (int i = 0; i < slaveList.size(); i++) {
            try {
                Socket socket = slaveList.get(i);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("start:" + slaveCrawlList[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startCrawling() {
        // todo Should distribute links

        // should process # of sublinks?
    }

    private void assignCrawlLinks() {
        slaveCrawlList = new String[slaveList.size()];
        int counter = 0;
        Pair<String, Integer> p;
        for (int i = 0; i < slaveCrawlList.length; i++) {
            slaveCrawlList[i] = "";
        }
        for (int i = 0; i < crawlList.size(); i++) {
            p = crawlList.get(i);
            slaveCrawlList[counter] += p.getKey();
            slaveCrawlList[counter++] += ",";
            counter %= slaveList.size();
        }
        for (int i = 0; i < slaveCrawlList.length; i++) {
            if (slaveCrawlList[i].charAt(slaveCrawlList[i].length() - 1) == ',') {
                slaveCrawlList[i] = slaveCrawlList[i].substring(0, slaveCrawlList[i].length() - 1);
            }
            System.out.println(slaveCrawlList[i]);
        }
    }

    public static void main(String[] args) {
        new Master();
    }

}
