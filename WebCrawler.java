/*
 * Web Crawler is implemented to crawl on web pages of fakebook and return secret flags
 * There are in all five secret flags and as soon as those flags are obtained crawling 
 * is ended. We have used Queue of link list which is like frontier and hashset that ensures that
 * duplicate urls are not visited.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {

	final static String hostName = "cs5700sp15.ccs.neu.edu";
	final static int portNumber = 80;

	public static void main(String[] args) {

		
		String userName= args[0];
		String password= args[1];
		
		
		String hostName = "cs5700sp15.ccs.neu.edu";
		int portNumber = 80;
		// create a socket and make http get request to land on that location
		try {
			InetAddress address = InetAddress.getByName(hostName);
			Socket echoSocket = new Socket(address, portNumber);
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(),
					true);
			boolean rerun = true;
			BufferedReader recieve;
			String t;
			String readline = null;
			String shtml = null;

			String location = null;
			String session = null;
			do {
				String userInput = "GET /accounts/login/?next=/fakebook/ HTTP/1.0\r\nHOST: cs5700sp15.ccs.neu.edu\r\n\r\n";
				out.print(userInput);
				out.flush();
				recieve = new BufferedReader(new InputStreamReader(
						echoSocket.getInputStream()));
				readline = recieve.readLine();
				// check for status code
				if (statusCodeCheck(readline) == 200) {
					rerun = false;
				}
				if (statusCodeCheck(readline) == 500) {
					rerun = true;
				}
				if (statusCodeCheck(readline) == 400) {
					System.out
							.println("This Page is either Forbidden or Not Found");
					echoSocket.close();
					return;
					
				}
				

			} while (rerun);
			rerun = true;
			// extract csrf token and session id from cookie after reading the
			// response header of the url
			String sessionId = null;
			String Csrf = null;
			String csrfValue = null;
			int counter = 0;
			for (int i = 0; i < 100; i++) {
				String read = recieve.readLine();
				if (read.contains("csrftoken")) {
					String a[] = read.split(":");
					Csrf = a[1];
					String value[] = Csrf.split("\\W");
					for (int h = 0; h < value.length - 1; h++) {
						if (value[h].equalsIgnoreCase("csrftoken")) {
							csrfValue = value[h + 1];
						}
					}

					counter++;
				}
				if (read.contains("sessionid")) {
					String a[] = read.split(":");
					sessionId = a[1];
					counter++;
				}
				if (counter == 2)
					break;
			}
			// construct a cookie
			String cookie = "Cookie: " + Csrf + "; " + sessionId;
			recieve.close();
			echoSocket.close();
			// socket connection to make post request
			Socket echoSocket1 = new Socket(address, portNumber);
			PrintWriter out1 = new PrintWriter(echoSocket1.getOutputStream(),
					true);

			do {
				String userInputLogin = "POST /accounts/login/ HTTP/1.0\r\nHost: cs5700sp15.ccs.neu.edu\r\nContent-Length: 109\n\rAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\nOrigin: http://cs5700f14.ccs.neu.edu\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.53 Safari/537.36\r\nContent-Type: application/x-www-form-urlencoded\r\n"
						+ cookie
						+ "\r\nAccept-Language: en-US,en;q=0.8\r\nReferer: http://cs5700f14.ccs.neu.edu/accounts/login/?next=/fakebook/\r\nAccept-Encoding: deflate\r\n\r\nusername="+userName+"&password="+password+"&csrfmiddlewaretoken="
						+ csrfValue + "&next=%2Ffakebook%2F";
				out1.print(userInputLogin);
				out1.flush();
				BufferedReader recieveAgain = new BufferedReader(
						new InputStreamReader(echoSocket1.getInputStream()));
				readline = recieveAgain.readLine();
				//read the status of the response header
				if (statusCodeCheck(readline) == 200) {
					shtml = stripHeader(recieveAgain);
					rerun = false;

					if (!shtml.contains("<h1>Welcome to Fakebook</h1>")) {
						System.out.println("Invalid login Credentials");
						return;
					}

				}
				if (statusCodeCheck(readline) == 500) {
					rerun = true;
				}
				if (statusCodeCheck(readline) == 400) {
					System.out
							.println("This Page is either Forbidden or Not Found");
					return;
				}
				// read the status code and handle 301 ,302
				if (statusCodeCheck(readline) == 300) {

					// check for 302
					if (readline.contains("302") || readline.contains("301")) {
						while ((t = recieveAgain.readLine()) != null) {
							if (t.contains("Location")) {
								String k[] = t.split("\\s+");
								for (int b = 0; b < k.length; b++)
									location = k[1];
								// System.out.println(location);
							}
							if (t.contains("sessionid")) {

								String a[] = t.split(":");
								String temp = a[1];
								String value[] = temp.split("\\W");
								for (int h = 0; h < value.length - 1; h++) {
									if (value[h].equalsIgnoreCase("sessionid")) {
										session = value[h + 1];

									}
								}
							}
						}
					}
					echoSocket1.close();
					do {
						// another socket connection to make Get request to fetch contents of redirected page for 302
						Socket echoSocket2 = new Socket(address, portNumber);
						String redirectRequest = "GET "
								+ location
								+ " HTTP/1.0\r\nHost: cs5700sp15.ccs.neu.edu\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36\r\nReferer: http://cs5700sp15.ccs.neu.edu/\r\nAccept-Encoding: deflate, sdch\r\nAccept-Language: en-US,en;q=0.8\r\nCookie: sessionid="
								+ session + "\r\n\r\n";
						PrintWriter outAgain1 = new PrintWriter(
								echoSocket2.getOutputStream(), true);
						BufferedReader recieveAgain1 = new BufferedReader(
								new InputStreamReader(
										echoSocket2.getInputStream()));
						outAgain1.println(redirectRequest);
						outAgain1.flush();

						readline = recieveAgain1.readLine();
						if (statusCodeCheck(readline) == 500) {
							System.out.println("status 500");
							rerun = true;
						}
						if (statusCodeCheck(readline) == 400) {
							System.out
									.println("This Page is either Forbidden or Not Found");
							echoSocket2.close();
							return;
						}
						

						shtml = stripHeader(recieveAgain1);
						rerun = false;
						echoSocket2.close();
					} while (rerun);
					rerun = false;
					
				}
				recieveAgain.close();
			
			} while (rerun);
			rerun = true;

			
			 
			// parse the content to extract html code
			Document d = Jsoup.parse(shtml);
			Elements links = d.select("a[href]");
			Elements flags;
			HashSet<String> front = new HashSet<String>();
			HashSet<String> visited = new HashSet<String>();
			Pattern regex = Pattern.compile(".*?(/fakebook/).*?"); // Pattern
																	// regex =
																	// Pattern.compile(".*?(c).*?");
			Matcher m;
			String url1, url2;
			Queue<String> queue = new LinkedList<String>();
			// queue.add("/fakebook/553949076/");
			ArrayList<String> flags1 = new ArrayList<String>();

			front.add("http://cs5700.ccs.neu.edu/fakebook/");

			for (Element link : links) {
				m = regex.matcher(link.toString());
				if (m.matches() == true) {
					url1 = link.attr("href");
					if (front.add(url1)) {
						queue.add(url1);

						while (!queue.isEmpty()) {
							try {
								String shtmlLoop = null;
								do {
									InetAddress address1 = InetAddress
											.getByName(hostName);
									Socket loopSocket = new Socket(address1,
											portNumber);
									String redirectRequestLoop = "GET "
											+ queue.element()
											+ " HTTP/1.0\r\nHost: cs5700sp15.ccs.neu.edu\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36\r\nReferer: http://cs5700sp15.ccs.neu.edu/\r\nAccept-Encoding: deflate, sdch\r\nAccept-Language: en-US,en;q=0.8\r\nCookie: sessionid="
											+ session + "\r\n\r\n";
									PrintWriter outAgain2 = new PrintWriter(
											loopSocket.getOutputStream(), true);
									BufferedReader recieveAgainLoop = new BufferedReader(
											new InputStreamReader(loopSocket
													.getInputStream()));
								//	BufferedReader sendAgainLoop = new BufferedReader(
									//		new InputStreamReader(System.in));
									outAgain2.println(redirectRequestLoop);
									outAgain2.flush();
									readline = recieveAgainLoop.readLine();
									if (statusCodeCheck(readline) == 200) {
										shtmlLoop = stripHeader(recieveAgainLoop);
										rerun = false;
									}
									if (statusCodeCheck(readline) == 500) {
										rerun = true;
									}
									if (statusCodeCheck(readline) == 400) {
										queue.remove();
										rerun = true;
									}
									;
									if (statusCodeCheck(readline) == 300) {

										if (recieveAgainLoop.readLine()
												.contains("302")) {
											while ((t = recieveAgainLoop
													.readLine()) != null) {
												if (t.contains("Location")) {
													String k[] = t
															.split("\\s+");
													for (int b = 0; b < k.length; b++)
														location = k[1];
												}
												if (t.contains("sessionid")) {

													String a[] = t.split(":");
													String temp = a[1];
													String value[] = temp
															.split("\\W");
													for (int h = 0; h < value.length - 1; h++) {
														if (value[h]
																.equalsIgnoreCase("sessionid")) {
															session = value[h + 1];
														}
													}
												}
											}
										}

										do {
											Socket echoSocket3 = new Socket(
													address, portNumber);
											String redirectRequest = "GET "
													+ location
													+ " HTTP/1.0\r\nHost: cs5700sp15.ccs.neu.edu\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36\r\nReferer: http://cs5700sp15.ccs.neu.edu/\r\nAccept-Encoding: deflate, sdch\r\nAccept-Language: en-US,en;q=0.8\r\nCookie: sessionid="
													+ session + "\r\n\r\n";
											PrintWriter outAgain3 = new PrintWriter(
													echoSocket3
															.getOutputStream(),
													true);
											BufferedReader recieveAgain3 = new BufferedReader(
													new InputStreamReader(
															echoSocket3
																	.getInputStream()));
											outAgain3.println(redirectRequest);
											outAgain3.flush();

											readline = recieveAgain3.readLine();
											if (statusCodeCheck(readline) == 500) {
												rerun = true;
											}
										
											if (statusCodeCheck(readline) == 200) {

												shtmlLoop = stripHeader(recieveAgain3);
												rerun = false;
											}
											echoSocket3.close();
										} while (rerun);
										rerun = false;
									}
									loopSocket.close();
								} while (rerun);
								rerun = true;
								Document docLoop = Jsoup.parse(shtmlLoop);

								flags = docLoop.select("h2.secret_flag");
								if (flags.size() > 0) {
									for (int i = 0; i < flags.size(); i++) {
										flags1.add(flags.get(i).toString());
									}

									if (flags1.size() == 5) {
										Iterator<String> it = flags1.iterator();
										while (it.hasNext()) {
											String fetch = (it.next()
													.toString());
											String flag = fetch.replaceAll(
													"\\<[^>]*>", "");
											String a[] = flag.split("\\s+");
											System.out.println(a[1]);
										}
										return;
									}
								}
								// System.out.println("parsing url : "+queue.element()
								// + "count of urls parsed = "+ count++);
								front.add(queue.element());
								queue.remove();
								Elements linkLoop = docLoop.select("a[href]");

								for (Element link2 : linkLoop) {
									m = regex.matcher(link2.attr("href"));// System.out.println(link2);
									if (m.matches() == true) {
										url2 = link2.attr("href");
										if (visited.add(url2)) {
											queue.add(url2); // System.out.println(url2);
											// System.out.println("addd in queue : "+url2
											// +"  lenth of visited links :"+visited.size());
										}

									}
								}

							} catch (UnknownHostException e) {
								System.out.println("Unknown Host");
							} catch (ConnectException e) {
								System.out.println("Connection Timed out");
							} catch (SocketException e) {
								System.out.println("Socket Connection Issue");
							} catch (IOException e) {
								System.out.println("Input Exception");
							}
						}

					}
				}
			}
		}

		catch (UnknownHostException e) {
			System.out.println("Unknown Host");
		} catch (ConnectException e) {
			System.out.println("Connection Timed out");
		} catch (SocketException e) {
			System.out.println("Connection reset, possible debug: please check your login credentials");
		} catch (IOException e) {
			System.out.println("Input Exception");
		}
	}

	private static String stripHeader(BufferedReader recieveAgain1) {
		// TODO Auto-generated method stub
		StringBuffer htmlString = new StringBuffer();
		String t3;
		int counters = 0;
		try {
			while ((t3 = recieveAgain1.readLine()) != null) {

				if (t3.contains("<html") || (t3.contains("<HTML"))) {
					counters = 3;
				}
				if (counters == 3)
					htmlString.append(t3);

				if (t3.contains("</HTML>") || (t3.contains("</html>"))) {
					htmlString.append(t3);
					counters = 0;
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return htmlString.toString();

	}

	// checks the status codes and reports them as integer
	private static int statusCodeCheck(String readline) {
		if (readline.contains("302") || readline.contains("301")) {
			return 300;
		} else if (readline.contains("500"))
			return 500;
		else if (readline.contains("404") || readline.contains("403")) {
			return 400;
		} else {
			return 200;
		}

	}
}
