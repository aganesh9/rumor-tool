# Program to extract news from urban legends and display keywords. The NLTK (Natural Language Toolkit) library of Python is used to tokenize text data, tag words based on their parts of speech and identify the keywords. For now, I have chosen the new hoaxes page and extracted all the news articles from it. Then, the user can choose a particular news item and keywords for that item are returned. 

import urllib
import nltk, re, pprint
from BeautifulSoup import BeautifulSoup 

# Function to extract keywords from text in paragraph tag
def get_keywords(myd):
	sentences = nltk.sent_tokenize(myd)  
	sentences = [nltk.wordpunct_tokenize(sent) for sent in sentences]
	sentences = [nltk.pos_tag(sent) for sent in sentences]
	#for sent in sentences:
	#	for word in sent:
	#		print word[1]+ ":" + word[0]
	print "\nKeywords:"
	for sent in sentences:
		for word in sent:
			#Only words that are significant parts of speech are chosen as keywords
			if word[1] in ('JJ','NNP', 'NNS', 'NNPS', 'VB', 'VBD', 'VBG', 'WRB'):
				print word[0]
url = "http://urbanlegends.about.com/od/reference/a/new_uls.htm"		
sock = urllib.urlopen(url)
wsource = sock.read()
sock.close()
soup = BeautifulSoup(wsource)
count=0

#News items are tagged with a particular value of the attribute zt. These are extracted.
count = 0
L = []
for section in soup.find("div", {"id": "articlebody"}).findAll('p'):
	nextNode = section
	count+=1
	if (count >= 3):
		L.append(nextNode)
count-=4
print "Found " + str(count) + " news items\n"			
i = 1
L = L[1:-1]
flag = 0
for node in L:
	print str(i)+":\n"
	print node.find('a').text + "\n" + node.text
	print "\n"
	title =node.find('a')
	url = "http://www.urbanlegends.about.com" + str(title.get('href'))
	sock = urllib.urlopen(url)
	wsource = sock.read()
	sock.close()
	soup = BeautifulSoup(wsource)
	stat = soup.find("div", {"id": "articlebody"}).find('p').findAll('b')
	if (stat):
		status = stat[-1].text
		flag = 1
		print status
	if flag == 0:
		print "Status: Unknown"
	flag = 0
	i+=1

print "\nChoose a news item number to extract keywords: "
number = int(raw_input())

count=0
for mylist in L:
	count+=1
	if count == number:
		title = mylist.find('a')
		print title.text
		#Getting keywords from title
		get_keywords(title.text)

		print mylist.text
		#Getting keywords from brief description
		get_keywords(mylist.text)
		break





	


