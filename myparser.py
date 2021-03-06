# Program to extract news from urban legends and display keywords. The NLTK (Natural Language Toolkit) library of Python is used to tokenize text data, tag words based on their parts of speech and identify the keywords. For now, I have chosen the new hoaxes page and extracted all the news articles from it. Then, the user can choose a particular news item and keywords for that item are returned. 

import urllib
import nltk, re, pprint
from BeautifulSoup import BeautifulSoup 
import ConfigParser
import sqlite3
import hashlib
import subprocess

config = ConfigParser.RawConfigParser()
config.read('dataset.cfg')

m = hashlib.md5()

# Function to extract keywords from text in paragraph tag
def get_keywords(myd):
	keywords = ""
	sentences = nltk.sent_tokenize(myd)  
	sentences = [nltk.wordpunct_tokenize(sent) for sent in sentences]
	sentences = [nltk.pos_tag(sent) for sent in sentences]
	#for sent in sentences:
	#	for word in sent:
	#		print word[1]+ ":" + word[0]
	for sent in sentences:
		for word in sent:
			#Only words that are significant parts of speech are chosen as keywords
			if word[1] in ('JJ','NNP', 'NNS', 'NNPS', 'VB', 'VBD', 'VBG', 'WRB', 'NN'):
				print word[0] + ":" + word[1];
				keywords = keywords + word[0] + ":" + word[1] + "\n";
	
	return keywords
url = config.get('Section1', 'url')		
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
conn = sqlite3.connect('RumorTool.db')
c = conn.cursor()
for node in L:
	print str(i)+":\n"
	m = hashlib.md5()
	m.update(node.text.encode('utf-8'))
	News_id = m.hexdigest()
	c.execute("SELECT * FROM NEWS_ITEMS WHERE News_id = '%s'" % News_id)
	exist = c.fetchone()
	if exist is None:
		date_text = node.find('i').text
		Added_date = date_text[7:-1]
		title =node.find('a')
		#print title.text + "\n"
		url = config.get('Section1', 'domain') + str(title.get('href'))
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
			status = "Status: Unknown"
			print status
		flag = 0
		real_text = node.find('i').previous
		print real_text + "\n"
		for char in '(?.!/;:\)':
			real_text = real_text.replace(char,' ')
	 	news_items = [(News_id, real_text, get_keywords(real_text), Added_date ,status)]
		c.executemany('INSERT INTO NEWS_ITEMS VALUES (?,?,?,?,?)', news_items)
	else:
		print "exists"
		print exist[1]+ "\n" + "\nKeywords: " + exist[2] + "\nAdded: " + exist[3] + "\n\n" + exist[4]
	i+=1
conn.commit()
print "\nChoose a news item number to extract keywords: "
number = int(raw_input())
final_id_passed = ""
keywords1=""
count=0
for mylist in L:
	count+=1
	if count == number:
		m1 = hashlib.md5()
		m1.update(mylist.text.encode('utf-8'))
		final_id_passed = m1.hexdigest()
		real_text = mylist.find('i').previous
		for char in '(?.!/;:\)"':
			real_text = real_text.replace(char,' ')
		#Getting keywords from brief description
		print "Keywords: \n"
		keywords1 = get_keywords(real_text)
		print keywords1
		break
conn.close()
#subprocess.call(['java', '-jar', 'CollectTweets.jar', keywords1+final_id_passed])







	


