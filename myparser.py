import urllib
from BeautifulSoup import BeautifulSoup 

def get_keywords(myd):

sock = urllib.urlopen("http://urbanlegends.about.com/od/horrors/Horrors.htm")
wsource = sock.read()
sock.close()
soup = BeautifulSoup(wsource)
count=0
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
		get_keywords(mylist.text)
		mydata = mylist.findParent("p").text
		get_keywords(mydata)
		print mydata
		break




	


