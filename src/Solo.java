import java.io.*;
import java.util.ArrayList;

/**
 * Created by jphan on 5/10/17.
 */
public class Solo {

    private long startTime, endtime;                    // used to save output times
    private String crawlListPath = Path.srcFolder + "resources/crawl_list.txt"; //file path to crawl list
    private String outputDir = Path.srcFolder + "crawler_results";
    private String resultFile = outputDir + "/results.txt";
    private ArrayList<Pair<String, Integer>> crawlList; // Contains a List of Links with # sublinks (to be balanced)
    private final String filePath = Path.srcFolder + "resources/scrapy_input.txt";
    private ArrayList<String> resultTimes;

    public Solo() {
        // Initialize ArrayLists
        crawlList = new ArrayList<>();
        resultTimes = new ArrayList<>();

        //Start up Methods
        createOutputDirectory();
        getCrawlList();

        // Start Balancing and Crawling
        startTime = System.currentTimeMillis();
        startCrawling();
        endtime = System.currentTimeMillis();
        displayResults();
        System.out.println("Total Time: " + Long.toString(endtime - startTime));
        saveResults();
        resultsToExcel();
    }

    /**
     * Creates the Output Directory inside src folder.
     */
    private void createOutputDirectory() {   //todo Doesnt work!!!
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
            System.out.println("Crawl List: ");
            while ((line = bufferedReader.readLine()) != null) {
                split = line.split(":");  // Link on Left side of ':' and # sublinks on Right side
                // i.e. "dailyprogrammer:100" is the reddit sublink "dailyprogrammer" with "100" subpages
                crawlList.add(new Pair<>(split[0], Integer.parseInt(split[1])));
                System.out.println(crawlList.get(crawlList.size() - 1).getKey());
            }
            System.out.println();
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run Scrappy on a given starting link (subreddit)
     */
    private void crawlPage(String page) {
        long startTime = System.currentTimeMillis();
        System.out.println("Crawling: " + page);
        saveScrapyInputToFile(filePath, page);
        String cmd = "scrapy crawl --nolog spider";
        Runtime run = Runtime.getRuntime();
        try {
            Process pr = run.exec(cmd);
            pr.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
        long endtime = System.currentTimeMillis();
        resultTimes.add(Long.toString(endtime - startTime));
    }

    /**
     * Saves a String to to given File path
     */
    public void saveScrapyInputToFile(String filePath, String string) {
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.print(string);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error Writing to Input File");
        }
    }

    private void startCrawling() {
        for (Pair<String, Integer> p : crawlList) {
            crawlPage(p.getKey());
        }
    }

    private void displayResults() {
        System.out.println("\n------------ Result Times --------------");
        for (int i = 0; i < crawlList.size(); i++) {
            System.out.println(crawlList.get(i).getKey() + ":" + Integer.toString(
                    crawlList.get(i).getValue()) +":" + resultTimes.get(i));
        }
    }

    private void saveResults() {
        try {
            PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
            for (int i = 0; i < crawlList.size(); i++) {
                writer.println(crawlList.get(i).getKey() + ":" + Integer.toString(
                        crawlList.get(i).getValue()) +":" + resultTimes.get(i));
            }
            writer.println("Total Time:" + Long.toString(endtime - startTime));
            writer.close();
        } catch (IOException e) {
            System.out.println("Error Writing to Input File");
        }
    }

    private void resultsToExcel() {
        System.out.println("Saving Data To Excel File...");
        String cmd = "python soloResultsToExcel.py";
        Runtime run = Runtime.getRuntime();
        try {
            Process pr = run.exec(cmd);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Solo();
    }

}
