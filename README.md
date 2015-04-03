Web Crawler

The coding of the project is done in JAVA.
The project implements HTTP protocol Constructed HTTP request and response messages from scratch and emulating browsers
requests required to fetch a page.
Parsed HTTP request, handled cookies receives in response messages.
Handled authentication using HTTP Post method parsed the HTML page using Jsoup Library.

Program Summary Approach:

/*
 * Web Crawler is implemented to crawl on web pages of fakebook and return secret flags 
 * There are in all five secret flags and as soon as those flags are obtained crawling 
 * is ended. I have used Queue of link list which is like frontier and hashset that ensures that
 * duplicate urls are not visited.
 */

Challenges Faced:
> imitate the HTTP POST request messages from client and parse login credentials to the server successfully.
>Handling Cookies: extracting csrf token and sessionid values from received response header and parsing the same on new 
  request messages to server.
>run time of the program, initially I had run time of around 25min. we tried many possibilities and even
  mutithreading to reduce the run-time. A close analysis of the Header concluded that Connection header had the value 
  keep alive. Removing that from the loop headers paced up the program exponentially. and the run-time was reduced to 6min.
>Error handling:HTTP status codes handled 301, 302, 403/404, 500   


Code tested:
I manually disabled and then re-enabled it : This allowed me to check server internal 500 error: the program continued to 
run untill the connection was reestablished and displayed the five secret flags at the output authentication:
User gives invalid credentials at the input of command line. the server 
responds with "Please enter a correct username and password" same message is redirected to the user.
