package legacy.util;
/**
 * download a file 
 * 
 * @author admin
 *
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

/*
 * Command line program to download data from URLs and save
 * it to local files. Run like this:
 * java FileDownload http://schmidt.devlib.org/java/file-download.html
 * @author Marco Schmidt
 */
public class Download 
{
	static Logger lg = Logger.getLogger(Download.class);
	public static void download(String address, String localFileName) 
	{
		if(! new File(localFileName).exists())
			System.err.println(" Couldnt find parent dir for " + localFileName + " downloadingn now...");
		OutputStream out = null;
		URLConnection conn = null;
		InputStream  in = null;
		System.out.println("Download beggining..."+address + " / " +localFileName);
		try 
		{
			URL url = new URL(address);
			out = new BufferedOutputStream(
				new FileOutputStream(localFileName));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			System.out.println(localFileName + "\t" + numWritten);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	public static String download(String address) 
	{
		StringBuffer b = new StringBuffer();
		URLConnection conn = null;
		InputStream  in = null;
		try 
		{
			URL url = new URL(address);
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				b.append(buffer);
				numWritten += numRead;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ioe) {
			}
		}
		return b.toString();
	}

	public static void main(String[] args)
	{
		Download.download("http://www.rcsb.org/pdb/files/1TRO.pdb");
	}
}
