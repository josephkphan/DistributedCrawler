# -*- coding: utf-8 -*-
from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
import scrapy


class ProgrammerSpider(CrawlSpider):
    name = "spider"  
    with open('/home/jphan/IdeaProjects/DistributedCrawler/src/resources/scrapy_input.txt') as f:
        reddit_subpage = str(f.readlines()[0])
    allowed_domains = ["www.reddit.com"]  
    start_urls = ['https://www.reddit.com/r/'+reddit_subpage+'/']
    rules = [
        # Rule #1
        Rule(LinkExtractor(
            allow=['/r/'+reddit_subpage+'/comments/']),  # will look for links with thie format
            callback='parse_problem',  # calls this method whenever it gets a response from that url^
            follow=False),  # This will Go into the found website! but will not go any deeper
        # Rule #2
        Rule(LinkExtractor(
        allow=['/r/'+reddit_subpage+'/\?count=\d*&after=\w*']),
            callback='pagination', 
            follow=True)  # will infinitely go into "depth" of the website
        # This will go through even pagination button "next page"
    ]

    # Callback method for a found link
    # This will retrieve the information from the specific site from Rule #1
    def parse_problem(self, response):
        with open('/home/jphan/IdeaProjects/DistributedCrawler/src/resources/scrapy_input.txt') as f:
            reddit_subpage = str(f.readlines()[0])
            f.close()
        with open('/home/jphan/IdeaProjects/DistributedCrawler/src/crawler_results/'+reddit_subpage+'.txt', 'a') as myfile:
            myfile.write(str(response.url) + '\n')
            myfile.close()
        # print response.url

    # Callback method for a pagination link
    def pagination(self, response):
        print("------------------------next-------------------------")
