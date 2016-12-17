package net.imlxy.myctdisk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Main {
	public static void main(String[] args) {
		System.setProperty("http.agent",
				"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2725.0 Mobile Safari/537.36");
		Scanner scann = new Scanner(System.in);
		System.out.print("URL:");
		String downuri = scann.nextLine();
		scann.close();
		Thread downThread = new Thread(new downthread(parse_file(downuri)));
		downThread.start();
	}

	public static String[] parse_file(String file_url) {
		String[] info = new String[2];
		// [0] = final url
		// [1] = filename
		try {
			URL url = new URL(file_url);
			HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
			urlconn.setRequestMethod("GET");
			urlconn.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlconn.getInputStream(), "utf-8"));
			String line;
			String result = in.readLine();
			while ((line = in.readLine()) != null) {
				result += line + "\n";
			}
			Document doc = Jsoup.parse(result);
			info[1] = doc.select("div.span10").first().child(0).text();
			String fileinfo = doc.select("div.header_content").first().text();
			String filesize = fileinfo.substring(fileinfo.indexOf("大小：") + 3, fileinfo.indexOf("B") + 1);
			String randcode = doc.select("#randcode").first().toString();
			randcode = randcode.substring(randcode.indexOf("value=") + 7, randcode.indexOf(">") - 1);
			String fileid = doc.select("#file_id").first().toString();
			fileid = fileid.substring(fileid.indexOf("value=") + 7, fileid.indexOf(">") - 1);
			System.out.println("Filename: " + info[1]);
			System.out.println("Size: " + filesize);
			// System.out.println("ID: " + fileid);
			// System.out.println("Randcode:\t\t" + randcode);
			URL downurl = new URL("http://ctfile.com/guest_loginV2.php");
			HttpURLConnection urlconn1 = (HttpURLConnection) downurl.openConnection();
			urlconn1.setRequestMethod("POST");
			urlconn1.setUseCaches(false);
			urlconn1.setDoOutput(true);
			urlconn1.connect();
			DataOutputStream dos = new DataOutputStream(urlconn1.getOutputStream());
			dos.writeBytes("randcode=" + randcode + "&file_id=" + fileid);
			dos.flush();
			dos.close();
			BufferedReader in1 = new BufferedReader(new InputStreamReader(urlconn1.getInputStream()));
			String line1;
			String result1 = in1.readLine();
			while ((line1 = in1.readLine()) != null) {
				result1 += line1 + "\n";
			}
			String fileinfo1 = result1.substring(result1.indexOf("free_down_action") + 18);
			info[0] = fileinfo1.substring(0, fileinfo1.indexOf(",") - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
}

class downthread implements Runnable {
	private BlockingQueue<String[]> pool = new ArrayBlockingQueue<String[]>(256);

	public downthread(String[] file_info) {
		try {
			pool.put(file_info);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			String[] key = pool.take();
			HttpURLConnection urlconn = (HttpURLConnection) new URL(key[0]).openConnection();
			File outfile = new File(key[1]);
			if (outfile.exists()) {
				System.out.println("Downloaded：" + outfile.length());
				urlconn.setRequestProperty("RANGE", "bytes=" + outfile.length() + "-");
			} else {
				outfile.createNewFile();
			}

			urlconn.connect();
			InputStream in = urlconn.getInputStream();
			RandomAccessFile oSavedFile = new RandomAccessFile(outfile.getName(), "rw");
			oSavedFile.seek(outfile.length());
			byte[] b = new byte[524288];
			int nRead;
			long oldtime = new Date().getTime();
			long oldlength = outfile.length();
			while ((nRead = in.read(b, 0, 524288)) > 0) {
				oSavedFile.write(b, 0, nRead);
				if (new Date().getTime() - oldtime >= 1000) {
					Date dat = new Date();
					System.out.print("Speed:" + (oldlength - outfile.length()) / ((oldtime - dat.getTime())) + "kb/s\r");
					oldlength = outfile.length();
					oldtime = dat.getTime();
				}
			}
			oSavedFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
