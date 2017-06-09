# This script is used to take in a list of comma separated subreddits which the
# crawler has been assigned, and returns information about each of these
# subreddit's weight.

import os

# ---------------------- Pre-processing of crawllist.txt -----------------------
file = open("resources/crawl_list.txt","r")
crawl_list = file.readlines()

crawl_dictionary = {}
for crawl_item in crawl_list:
    crawl_dictionary[crawl_item.split(":")[0]] = crawl_item.split(":")[1].replace("\n", "")

# ------------------------------- User Input -----------------------------------
subreddits = raw_input('Enter the list of subreddits:')
print ""

list_of_subreddits = subreddits.split(",")

total_weight = 0
for subreddit in list_of_subreddits:
    total_weight += int(crawl_dictionary[subreddit])
    print subreddit + ": " +crawl_dictionary[subreddit]

print "Total Weight: " + str(total_weight) + "\n"

# Copies string to clipboard for fast pasting
clipboard_string = total_weight
os.system("echo '%s' | pbcopy" % clipboard_string)
