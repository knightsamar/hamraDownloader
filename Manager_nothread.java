/*
 * hamraDownloader -- website dumper 
 * 
 * Vision: Dump websites for offline dumping
 * Inspiration: Our searches for online ebooks and attempts for offline viewing at our netless homes.
 * 
 * Developers and Hackers: Divine Dragon(060321013) and Knight Samar(060321011)
 * Name inspired by : Deepak Dell, MBA-IT (07-09) batch
 * 
 * If you think comments shouldn't be funny, you ought to code more at 5.30 AM in morning :p
 * 
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.RuntimeException.*;
import java.util.regex.*;

/* Everything is a Link ;)
* This class is instantiated for each link found in a webpage and is multi-threaded.
* Once a Link comes into this world, it is fetched, looked for more links if it is a html file(LIVE WAY) and dumped to disk
* If it is a binary file or one which contains no further links, it is simply dumped.
* The thread like all good things in this world, comes to an end when things are done. 
*/

//class Link implements Runnable
class Link
{	public static String DOWNLOAD_FOLDER;	//Address of the folder where the files will be downloaded
	//Status Indicators for the links
	public static final int PREPARED = 1;
	public static final int DOWNLOADING = 2;
	public static final int DOWNLOADED = 3;
	public static final int ERROR = 4;
	public static final boolean YES = true;
	public static final boolean NO = false;
	public static final String DEADLINK = "DEADLINK";	//this is Dead Link -- no other file is referred using this file. -- don't process
	public static final String LIVEWAY = "LIVEWAY";	//this is Live Way -- other files are referred usign this file -- process it.
	
	private String MyURL;			// URL of the link
	private int Status;				// one of the above defined constants
	private boolean Selected;		//Whether to store the link to disk or not
	private String LocalFileName;	//Location of the file on disk
	private String LinkType; 		//whether this is Dead link or Live link

	Link()
	{
		//do nothing
		System.out.println("The blank constructor was called");
	}

	//instantiator or constructor as the Java people like to call it.
	//parameterized constructor for non-iterators
	Link(String url_str,String localfile,String contenttype)
	{	MyURL = new String(url_str);
		Status = Link.PREPARED;
		Selected = Link.YES;
		LocalFileName = localfile;
		//whether this is a Live way ro dead link
		if (contenttype.indexOf("text/html") != -1)
		{
			LinkType = Link.LIVEWAY;
		}
		else
		{
			LinkType = Link.DEADLINK;
		}
		
		//Thread thread  = new Thread(this);
		run();
	}

	public String getURL()
	{	return this.MyURL;
	}
	
	public int getStatus()
	{	return this.Status;
	}
	
	public boolean getSelected()
	{	return this.Selected;
	}
	
	public String getLocalFileName()
	{	
		return this.LocalFileName;
	}
	
	public boolean setStatus(int new_Status)
	{	try
		{	this.Status = new_Status;
			return true;
		}
		catch (Exception e)
		{	return false;
		}
	}
	
	public boolean setSelected(int new_Status)
	{	try
		{	this.Status = new_Status;
			return true;
		}
		catch (Exception e)
		{	return false;
		}
	}
	
	public boolean setLocalFileName(String lfn)
	{	this.LocalFileName = lfn;
		return true;
	}
	
	/* tells whether this is Live Way or Dead Link */
	public String getLinkType()
	{
		return this.LinkType;
	}

	/* Fetch the file line by line -- processing it similary for available links and files and then saving it.*/
	public String fetcher()
	{		String newlinks = new String();					//for storing the links obtained from this file
			try
			{	URL myUrl = new URL(this.getURL());				//make the string an URL!
				InputStream file = myUrl.openStream();			//Open the URL's stream for Input 
						
				//does the output path exist ?
				File fl = new File((this.LocalFileName).substring(0,(this.LocalFileName).lastIndexOf(File.separator)));
				if (!(fl.exists()))
				{
					//no, hence create the directory
					fl.mkdirs();
				}
				System.out.println(this);
				/* is this a Dead Link or Live Way ? 
				/ Dead Links = files which refer to no more files
				/ Live way == files which link */
				if (LinkType == LIVEWAY)
				{
					//Buffer-process only non-binary files.
					BufferedReader br = new BufferedReader(new InputStreamReader(file));//this is a Buffered input stream and we are doing line by line.
					String line;	//the line we talked about.
					String currentFolder = MyURL.substring(0,MyURL.lastIndexOf("/"));//where are we in the whole sea ?
					
					
					//this is the buffered writer
					BufferedWriter bw = new BufferedWriter(new FileWriter(this.LocalFileName));
					
					System.out.println("Downloading Started for file ==> " + this);
					while((line = br.readLine()) != null)
					{	
						
						bw.write(line,0,line.length()); //BufferedWriter.write(String s,int off,int len)
						bw.newLine(); //for printing new-line char -- platform specific
						
						
						//check whether there is a link and if yes add it.
						newlinks = newlinks + LinkPattern.match(line,currentFolder) + ",";
						
					}
					bw.close(); //close the stream
					setStatus(Link.DOWNLOADED);
					newlinks = newlinks.substring(0,newlinks.length()-1);
				 }	
				 //when the file is a dead link
				 else if (LinkType == DEADLINK)
				 {
					//download the binary file completely
					System.out.println("Binary files are only enqueued in this version!");	
				 }
				System.out.println("Download Complete for file ==> " + this);
				Status = DOWNLOADED;
				Manager_nothread.showStatus();
				
			}
			catch (MalformedURLException e)
			{	System.out.println("URL specified is not correct");
				System.out.println("Tell the developers : \n" + e.toString());
				Status = ERROR;
			}
			catch (IOException e)
			{	
				System.out.println("Unable to get the file from the server. Check connection!");
				System.out.println("Tell the developers :");
				e.printStackTrace();
				Status = ERROR;
			}
			catch (ConcurrentModificationException e)
			{
				System.out.println("Concurrent modification exception occured!");
				System.out.println("Tell the developers : \n");
				e.printStackTrace();
				Status = ERROR;
			}
			catch (Exception e)
			{	System.out.println("Internal server error! hamraDownloader ko donut nahi milega");
				System.out.println("Tell the developers : \n" + e.toString());
				Status = ERROR;
			}
			return newlinks;
	}
	
	public void run()
	{	String FoundLinks = new String();
		//do we too much of threads ?
		
		//if yes then wait
		
		//if no then run!
		if ((Status == Link.DOWNLOADED) || (Status == Link.ERROR))
		{	return;
		}
		else if (Status == PREPARED)
		{			
			FoundLinks = this.fetcher();
			
		}
		System.out.println("Done, exiting now for " + this);
		
		//Check if new links are found
		if (FoundLinks.length() > 0)
		{	String links[] = FoundLinks.split(",");
			for(int i=0; i<links.length; i++)
			{	if (links[i].length()>0)
				{	//System.out.println("Enquing link ==> "+links[i]);
					Manager_nothread.addLink(links[i]);
				}
			}
		}
		return ;
	}
	
	//What are you ? -- the representation
	public String toString()
	{
		return (this.getURL() + " " + this.getStatus() + " " + this.getLocalFileName() + " " + this.getLinkType());
	}
}


class Manager_nothread
{	
	//this is our Link Table
	private static ArrayList<Link> LinkTable = new ArrayList();
	//this is our string collection of all the URLs -- used for iteration.
	private static ArrayList<String> LinkTableURLs = new ArrayList();
	
	//this is the start address
	private static String Start_Address = new String();
	//start downloading!
	Manager_nothread(String start_address)
	{	

		this.Start_Address = start_address;
		
		//add this Link to the link table
		Manager_nothread.addLink(start_address);
		
	}
	
	//checks whether this URL already exists in the Link Table
	public static boolean checkDuplicate(String url)
	{	boolean exists = false;
		//Check for Duplicates before adding them to our list
		Iterator LinkTableURLsItr = Manager_nothread.LinkTableURLs.listIterator();
		
		while (LinkTableURLsItr.hasNext())
		{	String u = (String) LinkTableURLsItr.next();
			if (u.indexOf(url) != -1) //whether this url is same as the one in table
			{	
				//Link has already been listed for download. Do not add again
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	/* add a Link to the Link table */
	public static void addLink(String url)
	{	
		boolean UnNamedFile = false;	 //for handling index outputs by web servers
		String localFile;				//for storing the local file name
		String contentType;				//for storing the content type
		
		//Check if the link is has a file link at the end or it is abstracted like http://localhost/
		if (!(url.lastIndexOf(".") > url.lastIndexOf("/")))
		{	//This link does not contain a . extionsion file
			UnNamedFile = true;
			if (!(url.lastIndexOf("/") == url.length()-1))
			{	// The link does not have / at the end of it. Terminate the string with /
				url = url.concat("/");
			}
		}
		
		//Check for http:// - indicates that the link is already prepared
		//Otherwise we have to explicitly prepare it
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++==="+(url.indexOf(Start_Address) == -1));
		
		if (url.indexOf(Start_Address) == -1)
		{	return ;
		}

		//get local file name and mime type for the link
		try
		{
			URL myURL = new URL(url);
			//offline location = /path/filename
			localFile = Link.DOWNLOAD_FOLDER + "/" + myURL.getPath() + "/" + myURL.getFile();
			
			//just for getting the mime-type
			HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
			contentType = connection.getContentType();			

			//whether this is an implicit output by the webserver or just another fancy Django/Ruby on Rails website ?
			if (UnNamedFile)
			{	
				localFile = (Link.DOWNLOAD_FOLDER.concat(url.substring(Manager_nothread.Start_Address.length(),url.length()))).concat("index.html");
			}
			else
			{	localFile = Link.DOWNLOAD_FOLDER.concat(url.substring(Manager_nothread.Start_Address.length(),url.length()));
			}

			localFile = localFile.replace("/",File.separator);
			
			//Finally! create the link in the Link table
			Link ln = new Link(url,localFile,contentType);
			LinkTable.add(ln);	//add to the link table
			LinkTableURLs.add(url); //add the url to the link table url
		
		}
		catch(MalformedURLException e)
		{
			System.out.println ("Malformed URL : "+ url);
			System.out.println ("Tell the developers: "+ e.toString());
			return;
		}
		catch (IOException e)
		{
			System.out.println ("Tell the developers: "+ e.toString());
			e.printStackTrace();
		}
	}
	
	
	public static void main(String args[])
	{	
		Link.DOWNLOAD_FOLDER = "c:\\temp\\";
		
		//whether this is a valid download folder ?
		File download_folder = new File(Link.DOWNLOAD_FOLDER);
		
		try
		{
			//does it exist ?
			if(download_folder.exists())
			{
				//can you write to it ?
				if(!download_folder.canWrite())
				{
					//try setting permission u+w ie. writable to owner only
					if (!download_folder.setWritable(true,true))
					{
						//anything can't be done. throw up!
						System.out.println("Write Permission denied to " + Link.DOWNLOAD_FOLDER);
						return ;
						//throw SecurityException;
					}
				}
			}
			else
			{
				//try creating the folder -- since it can be created by us, we can obviously write to OR the filesystem people have gone crazy
				if (download_folder.mkdirs())
				{
					//can't create the folder. throw up
					System.out.println("Write Permission denied to " + Link.DOWNLOAD_FOLDER);
					return ;
					//throw SecurityException;
					
				}
			}
		}
		catch(SecurityException e)
		{
			System.out.println("Security Violation! Problem accessing the download folder");
			System.out.println("Tell the developers : \n" + e.toString());
			return ;
		}
		LinkPattern.starter();
		try
		{	Manager_nothread dn = new Manager_nothread(args[0]);
		}
		catch(IndexOutOfBoundsException e)
		{	e.printStackTrace();
		}

			
		//Manager_nothread dn = new Manager_nothread("http://sagittarius/docs/html-doc/");
	}
	
	//Show the LinkTable for more information
	public static void showStatus()
	{	System.out.println("/********************************************/");
		System.out.println("/*******    HERE IS YOUR LINK TABLE   *******/");
		System.out.println("/********************************************/");
		Iterator LinkTableItr = Manager_nothread.LinkTable.listIterator();
		
		while (LinkTableItr.hasNext())
		{	Link temp = (Link) LinkTableItr.next();
			System.out.println(temp);
		}
		System.out.println("/********************************************/");
	}
}

//for determining the links to other files

final class LinkPattern
{   
	private static ArrayList LinkPatterns = new ArrayList();
	
	public static void starter()
	{
		//adding each type of compiled link pattern
		
		/*RANT: notice how we have two slashes before s -- that is how you specifiy a metacharacter in Java regexp
		* this took us from 4 am to 5 am -- because no book(Complete reference, Core Java) nor the API talked or gave 
		* examples for whitespace characters or other predefined character matches match
		* 
		* The Art of Java by Herbert Schildt/James Holmes, McGraw Hill is a superb with great examples.
		* 
		* Most of the code below in match() is inspired(no, not lifted off) by pg.no.225-230 in the above book.
		*/
		//LinkPatterns.add(Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",Pattern.CASE_INSENSITIVE)); //for <a href=
		LinkPatterns.add(Pattern.compile("href\\s*=\\s*\"?(.*?)[\"|>]",Pattern.CASE_INSENSITIVE)); //for <a href=
	}
	
	//checks for links in the line and adds them to the Link table
	
	public static String match(String line,String currentFolder)
	{
		//System.out.println("Printing started...");
		//iterate through the link patterns and search for a match!
		String newlinks = new String();
		Iterator LinkPatternsItr = LinkPatterns.listIterator();
		
		while (LinkPatternsItr.hasNext())
		{
			Pattern p = (Pattern) LinkPatternsItr.next();
			Matcher m = p.matcher(line);
			while (m.find())
			{
				String link = m.group(1).trim(); //remove spaces from the first matching group

				// Skip empty links.
				if (link.length() < 1)
				{	continue;
				}

				// Skip links that are just page anchors.
				if (link.charAt(0) == '#')
				{	 continue;
				}

				//Ignore mailto: link
				if (link.indexOf("mailto:") != -1)
				{	 continue;
				}

				// ignore javascript links
				if (link.toLowerCase().indexOf("javascript") != -1)
				{	continue;
				}
				
				//Check for some page anchors in the links
				if (link.indexOf("#") != -1)
				{	//url contains some page anchor. Remove the anchor
					link = link.substring(0,link.indexOf("#"));
				}

				//System.out.println("Found Link ==> " + link);
				if (!(Manager_nothread.checkDuplicate(link)))
				{	//System.out.println("Not found in LinkTable ==> Adding  => "+link);
					//Manager_nothread.addLink(link); //add this link now
					newlinks = newlinks+","+currentFolder+"/"+link;
				}
			}
		}
		if (newlinks.length()>0)
		{	newlinks = newlinks.substring(1,newlinks.length());
			//System.out.println(newlinks);
		}
		
		return newlinks;
	}
}
