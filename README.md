# DistributedCrawler

## Used a Master Slave model.
 - Master will load balance all of the links and tell the slaves which links they need to crawl. 
 - Slaves will crawl the links given to them

## Crawler
 - Used scrapy and its CrawlSpider.
 - Code for my spider can be found in reddit_scraper/spiders/spider.py 
 - Rules to discover links on reddit as well as rules to discover pagination buttons
 - my spider will save its results (response url) in a .txt file in crawler_results
 
## Running The Program
 - Compile the code with "make"
 - Need at least 2 terminals/machines
 - Have one be the master by running "java Master"
 - The rest are slaves by running "java Slave"
 - Slaves will need to add their master in <master ip>,<master port number> i.e. "127.0.1.1,8000"
 - After all slaves are added, on the master type in the command "start"
 - wait til all slaves are completed, the master will print out the time logs and exit the program
 - clean up files with "make clean"
