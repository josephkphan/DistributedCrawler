# DistributedCrawler

## Master/Slave model
 - Master will load balance all of the links and tell the slaves which links they need to crawl. 
 - Slaves will crawl the links given to them

## Crawler
 - Used scrapy and its CrawlSpider.
 - Code for my spider can be found in reddit_scraper/spiders/spider.py 
 - Rules to discover links on reddit as well as rules to discover pagination buttons
 - The spider will save its results (response url) in a .txt file in crawler_results

## PreReqs
 - Need: Anaconda, Scrapy, Python-xlwt
 - Instructions (for Ubuntu at least)
 - Install Anaconda -- https://www.continuum.io/downloads
 - Install Scrappy : "conda install -c conda-forge scrapy"
 - Note* If you still cannot run scrapy you may need to fix your path "export PATH=~/anaconda2/bin:$PATH"
 - Install xlwt (used to save results in excel file) "sudo apt-get update" and then "sudo apt-get install python-xlwt"
 - If that didn't work then try "pip install xlwt" (if you have pip)

## Setup
 - Edit Filepaths in spider.py and Path.java to the src folder
 - Include your List of Starting URLs in resources with name "crawl_list.txt"

## Running The Distributed Program
 - Compile the code with "make"
 - Need at least 2 terminals/machines
 - Have one be the master by running "java Master"
 - The rest are slaves by running "java Slave"
 - Slaves will need to add their master in <master ip>,<master port number> i.e. "127.0.1.1,8000"
 - After all slaves are added, on the master type in the command "start"
 - Wait until all slaves are completed, the master will print out the time logs and exit the program
 - Clean up files with "make clean"

## Running the Solo Program
 - Compile Code with "make"
 - Run command "java Solo"