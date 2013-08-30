# Program to extract news from urban legends and display keywords. The NLTK (Natural Language Toolkit) library of Python is used to tokenize text data, tag words based on their parts of speech and identify the keywords. For now, I have chosen the category horror in Urban Legends and extracted all the news articles from it. Then, the user can choose a particular news item and keywords for that item are returned. 

import urllib
import nltk, re, pprint
from BeautifulSoup import BeautifulSoup 

# Function to extract keywords from text in paragraph tag
def get_keywords(myd):
	sentences = nltk.sent_tokenize(myd)  
	sentences = [nltk.word_tokenize(sent) for sent in sentences]
	sentences = [nltk.pos_tag(sent) for sent in sentences]
	grammar = "NP: {<NNP>+}"
	print "\nKeywords:"
	for sent in sentences:
		for word in sent:
			#Only words that are significant parts of speech are chosen as keywords
			if word[1] in ('JJ','NN','NNP', 'NNS', 'NNPS', 'VB', 'VBD', 'VBG', 'WRB'):
				print word[0]
			
sock = urllib.urlopen("http://urbanlegends.about.com/od/horrors/Horrors.htm")
wsource = sock.read()
sock.close()
soup = BeautifulSoup(wsource)
count=0

#News items are tagged with a particular value of the attribute zt. These are extracted.
for mylist in soup.findAll("a",{"zt" : "18/1R4/Wa"}):
	count+=1
	print "\n" + str(count) + ": " + mylist.text +"\n"
	mydata = mylist.findParent("p").text
	print mydata

print "\nFound " + str(count) + " items in category horror"
print "\nChoose a news item number to extract keywords: "
number = int(raw_input())
count=0
for mylist in soup.findAll("a",{"zt" : "18/1R4/Wa"}):
	count+=1
	if count == number:
		print "\n" + str(count) + ": " + mylist.text +"\n"

		#Getting keywords from title
		get_keywords(mylist.text)
		mydata = mylist.findParent("p").text

		#Getting keywords from brief description
		get_keywords(mydata)
		print mydata
		break




	


