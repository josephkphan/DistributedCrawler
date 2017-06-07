import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;


public class Master {
    private final static int START = 1025;              // Constant - Minimum port number allowed
    private final static int END = 65525;               // Constant - Maximum Port Number allowed
    private ServerSocket listener;                      // Used to accept other socket connections
    private InetAddress ip;                             // Your IP Address
    private int portNumber;                             // Your Port Number
    private ArrayList<Socket> slaveList;                // Slaves are added here
    private long startTime, endtime;                    // used to save output times
    private String crawlListPath = Path.srcFolder + "resources/crawl_list.txt"; //file path to crawl list
    private String outputDir = Path.srcFolder+ "crawler_results";
    private String resultFile = outputDir + "/results.txt";
    private ArrayList<Pair<String, Integer>> crawlList; // Contains a List of Links with # sublinks (to be balanced)
    private String[] slaveCrawlList;                    // Balanced links for slaves.
    private String[] slaveTimeResults;
    private int slaveFinishedCounter;

    public Master() {
        // Initialize ArrayLists
        slaveList = new ArrayList<>();
        crawlList = new ArrayList<>();

        //Start up Methods
        slaveFinishedCounter = 0;
        createOutputDirectory();
        getCrawlList();
        createServerSocket();
        getIPAddress();
        displayIPAddress();
        createSocketListenerThread();
        waitToCrawl();

        // Start Balancing and Crawling
        startTime = System.currentTimeMillis();
//        assignCrawlLinksRoundRobin();
        assignCrawlLinksWeightedRoundRobin();           // Todo Currently On Weighted Round Robin
        displaySlaveCrawlList();
        broadcastSlavesToStartCrawling();

        // Will wait until all slaves are done before exiting
        while (true) { }
    }

    ////////////////////////////////////  Start Up Methods  ///////////////////////////////////////
    /**
     * Creates the Output Directory inside src folder.
     */
    private void createOutputDirectory(){   //todo Doesnt work!!!
        File f = new File(outputDir);
        f.mkdir();
    }

    /**
     * Reads Crawl List File and saves data to ArrayList "crawlList"
     */
    private void getCrawlList() {
        try {
            String line;
            String[] split;
            // Open File
            File file = new File(crawlListPath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // Read Line by Line from the file
            while ((line = bufferedReader.readLine()) != null) {
                split = line.split(":");  // Link on Left side of ':' and # sublinks on Right side
                // i.e. "dailyprogrammer:100" is the reddit sublink "dailyprogrammer" with "100" subpages
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
        try {
            listener = new ServerSocket(8010);
            portNumber = 8010; // will only reach here on a successful Port
        } catch (IOException e) {
            System.out.println("Failed to make connection with Port 8010");
            exit(0);
            // will retry to create a server socket
        }
        //For Random Port
//        for (int port = START; port <= END; port++) {
//            try {
//                port = ThreadLocalRandom.current().nextInt(START, END);
//                listener = new ServerSocket(port);
//                portNumber = port; // will only reach here on a successful Port
//                break;
//            } catch (IOException e) {
//                // will retry to create a server socket
//            }
//        }
    }

    /**
     * Gets and saves the IPAddress of the current Server
     */
    private void getIPAddress() {
        try {
            ip = InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wait until master types in "start" to begin crawling
     */
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

    //////////////////////////////////// Helper / Print Methods /////////////////////////////////////////

    /**
     * Display IP Address to Screen
     */
    private void displayIPAddress() {
        System.out.println(ip.getHostAddress() + " at port number: " + Integer.toString(portNumber));
    }

    /**
     * will close the socket connection
     */
    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints out Final Results. called when all crawling is done
     */
    private void displayResults(){
        displaySlaveTimeResults();
        System.out.println("Total Time (ms): " + (endtime - startTime));
    }

    /**
     * Prints out the load balanced slave-crawl assignments onto terminal
     */
    private void displaySlaveCrawlList(){
        System.out.println("\n--------------Slave Crawl Assignment ---------------");
        for(int i=0; i<slaveCrawlList.length; i++){
            System.out.println("Slave " + Integer.toString(i) + ": " + slaveCrawlList[i] );
        }

        System.out.println("\n............. Waiting For Slaves............");
    }

    /**
     * Prints out Time results onto terminal
     */
    private void displaySlaveTimeResults(){
        System.out.println("\n--------------Slave Time Results ---------------");
        for(int i=0; i<slaveTimeResults.length; i++){
            System.out.println("Slave " + Integer.toString(i) + ": " + slaveTimeResults[i] );
        }
    }

    private void saveResults(){
        try {
            PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
            for(int i=0; i<slaveTimeResults.length;i++) {
                writer.println("Slave" + Integer.toString(i) + ":" + slaveCrawlList[i] + ":" + slaveTimeResults[i]);
            }
            writer.println("Total Time:" + Long.toString(endtime-startTime));
            writer.close();
        } catch (IOException e) {
            System.out.println("Error Writing to Input File");
        }
    }

    private void resultsToExcel(){
        System.out.println("Saving Data To Excel File...");
        String cmd = "python resultsToExcel.py";
        Runtime run = Runtime.getRuntime();
        try {
            Process pr = run.exec(cmd);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////// Server Methods /////////////////////////////////////////

    /**
     * Send a List to every slave containing what they should crawl
     */
    private void broadcastSlavesToStartCrawling() {
        for (int i = 0; i < slaveList.size(); i++) {
            try {
                Socket socket = slaveList.get(i);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("start:" + slaveCrawlList[i]);
                // "start" is a keyword for the slaves to know to start. ':' is a separator used to parse the
                // keyword from the list of reddit sublinks.
            } catch (Exception e) {
                e.printStackTrace();
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
                        // Listener.accept() is a blocking code, waiting or a socket to connect
                        Socket socket = listener.accept();      //blocking code - will wait until another host wants to

                        // A Slave just formed a connection. Print out status on terminal
                        slaveList.add(socket);
                        System.out.println("\n    Slave Just Added\n    # Slaves = " + Integer.toString(slaveList.size()));
                        System.out.print("Type in \"start\" To begin Crawling: ");

                        // Reply back to Slave that they were successfully added
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("Successfully Added, Wait to begin Crawling...");

                        // Creates a thread that listens on the socket stream with the newly added Slave
                        createSocketInputStreamHandlerThread(socket);
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
     * This parses messages received by the socket streams and respond accordingly
     */
    private void readServerInputStream(String s, Socket socket) {
        String[] split = s.split(":");
        if (split[0].equals("finished")) {
            slaveTimeResults[slaveList.indexOf(socket)] = split[1];
            System.out.println("Slave " + Integer.toString(slaveList.indexOf(socket)) +" Finished -- " + split[1]);
//            slaveList.remove(socket);
            closeSocket(socket);
            slaveFinishedCounter++;
            if (slaveList.size() == slaveFinishedCounter) {
                // All Slaves Finished! Print out execution time
                endtime = System.currentTimeMillis();
                displayResults();
                saveResults();
                resultsToExcel();
                System.out.println("Finished Crawling : Exiting Program");
                exit(0);
            }

        } else {                                        // Got some other garbage - could be null or something else
            System.out.println(s);
        }
    }

    ////////////////////////////////////////  Load Balancing //////////////////////////////////

    /**
     * This is the load balancing part of the program. Currently on Round Robin
     */
    private void assignCrawlLinksRoundRobin() {
        // Initialize Array and variables
        slaveTimeResults = new String[slaveList.size()];
        slaveCrawlList = new String[slaveList.size()];
        int counter = 0;
        Pair<String, Integer> p;

        // Initialize blank Strings
        for (int i = 0; i < slaveCrawlList.length; i++) {
            slaveCrawlList[i] = "";
        }

        // Round robin style assignment from links to slaves
        for (int i = 0; i < crawlList.size(); i++) {
            p = crawlList.get(i);
            slaveCrawlList[counter] += p.getKey();
            slaveCrawlList[counter++] += ",";
            counter %= slaveList.size();
        }

        // Remove the last ',' for every entry of the array
        for (int i = 0; i < slaveCrawlList.length; i++) {
            if (slaveCrawlList[i].charAt(slaveCrawlList[i].length() - 1) == ',') {
                slaveCrawlList[i] = slaveCrawlList[i].substring(0, slaveCrawlList[i].length() - 1);
            }
            System.out.println(slaveCrawlList[i]);
        }
    }

    /**
     * This is the load balancing part of the program. Currently on Round Robin
     */
    private void assignCrawlLinksWeightedRoundRobin() {
        // Initialize Array and variables
        slaveTimeResults = new String[slaveList.size()];
        slaveCrawlList = new String[slaveList.size()];
        int[] weights = new int[slaveList.size()];

        int counter = 0;
        Pair<String, Integer> p;

        // Initialize blank Strings
        for (int i = 0; i < slaveCrawlList.length; i++) {
            slaveCrawlList[i] = "";
        }

        // Round robin style assignment from links to slaves
        for (int i = 0; i < crawlList.size(); i++) {
            p = crawlList.get(i);
            System.out.println(Arrays.toString(weights));
            int index = getMinIndex(weights);
            System.out.println("index = " + index);
            slaveCrawlList[index] += p.getKey();
            slaveCrawlList[index] += ",";
            weights[index]+=p.getValue();
        }
        System.out.println(Arrays.toString(weights));

        // Remove the last ',' for every entry of the array
        for (int i = 0; i < slaveCrawlList.length; i++) {
            if (slaveCrawlList[i].charAt(slaveCrawlList[i].length() - 1) == ',') {
                slaveCrawlList[i] = slaveCrawlList[i].substring(0, slaveCrawlList[i].length() - 1);
            }
            System.out.println(slaveCrawlList[i]);
        }
    }

    private int getMinIndex(int[] array){
        int minIndex = 0;
        for(int i=1; i<array.length; i++){
            if (array[minIndex] > array[i]){
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static void main(String[] args) {
        new Master();
    }

}
