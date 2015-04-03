all:
	@javac -cp /home/dubeyaj/Documents/Proj2/jsoup-1.8.1.jar -Xlint:unchecked WebCrawler.java

clean :
	rm -f *.class
