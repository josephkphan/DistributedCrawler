# -*- coding: utf-8 -*-
from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
import scrapy

# This spider will grab all the questions on daily programmer and add them to a databse
class ProgrammerSpider(CrawlSpider):
    name = "spider"  # Spider Name - must be unique per spider
    with open('/home/jphan/IdeaProjects/DistributedCrawler/src/resources/scrapy_input.txt') as f:
        reddit_subpage = str(f.readlines()[0])
    allowed_domains = ["www.reddit.com"]  # Domain - set the scope of the crawler
    start_urls = ['https://www.reddit.com/r/'+reddit_subpage+'/']
    # Start URLS is where the crawler will start and perform the rules
    # If there is more than 1 start URL, it will go in order
    rules = [
        # Rule #1
        Rule(LinkExtractor(
            allow=['/r/'+reddit_subpage+'/comments/']),  # will look for links with thie format
            callback='parse_problem',  # calls this method whenever it gets a response from that url^
            follow=False),  # This will Go into the found website! but will not go any deeper
        # Rule #2
        Rule(LinkExtractor(
        allow=['/r/'+reddit_subpage+'/\?count=\d*&after=\w*']),
            # \d is some number of digits, \w alpha characters and underscores
            # ^ Finds the pagination of the website
            callback='pagination',  # calls this method whenever it gets a response from that url^
            follow=True)  # will infinitely go into "depth" of the website
        # This will go through even pagination button "next page"
    ]

    # Callback method for a found link
    # This will retrieve the information from the specific site from Rule #1
    def parse_problem(self, response):
        # with open('/home/jphan/IdeaProjects/DistributedCrawler/src/resources/scrapy_input.txt') as f:
        #     reddit_subpage = str(f.readlines()[0])
        with open('/home/jphan/IdeaProjects/DistributedCrawler/src/crawler_results/'+reddit_subpage+'.txt', 'a') as myfile:
            myfile.write(response.url)


    # Callback method for a pagination link
    def pagination(self, response):
        print("------------------------next-------------------------")
