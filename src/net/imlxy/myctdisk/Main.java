package net.imlxy.myctdisk;

import java.net.URL;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public class Main {
	public static void main(String[] args) {
		System.setProperty("http.agent",
				"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2725.0 Mobile Safari/537.36");
		Scanner scann = new Scanner(System.in);
		System.out.print("下载地址:");
		String downuri = scann.nextLine();
		scann.close();
		String downurl_final = null;
		String filename = null;
		try {
			URL url = new URL(downuri);
			HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
			urlconn.setRequestMethod("GET");
			urlconn.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlconn.getInputStream(),"utf-8"));
			String line;
			String result = in.readLine();
			while ((line = in.readLine()) != null) {
				result += line + "\n";
			}
			Document doc = Jsoup.parse(result);
			filename = doc.select("div.span10").first().child(0).text();
			String fileinfo = doc.select("div.header_content").first().text();
			String filesize = fileinfo.substring(fileinfo.indexOf("大小：") + 6, fileinfo.indexOf("B") + 1);
			String randcode = doc.select("#randcode").first().toString();
			randcode = randcode.substring(randcode.indexOf("value=") + 7, randcode.indexOf(">") - 1);
			String fileid = doc.select("#file_id").first().toString();
			fileid = fileid.substring(fileid.indexOf("value=") + 7, fileid.indexOf(">") - 1);
			System.out.println("文件名:\t\t"+filename);
			System.out.println("文件大小:\t"+filesize);
			System.out.println("文件ID:\t\t"+fileid);
			System.out.println("随机码:\t\t"+randcode);
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
			downurl_final = fileinfo1.substring(0, fileinfo1.indexOf(",") - 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File(filename);
		System.out.println(file.getAbsolutePath());
		try {
			URL url = new URL(downurl_final);
			HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
			if (file.exists()){
				System.out.println("已下载："+file.length());
				urlconn.setRequestProperty("RANGE","bytes="+file.length()+"-");
			}else{
				file.createNewFile();
			}
			urlconn.connect();
			InputStream in = urlconn.getInputStream();
			RandomAccessFile oSavedFile = new RandomAccessFile(filename,"rw"); 
			// 定位文件指针到 nPos 位置 
			oSavedFile.seek(file.length()); 
			byte[] b = new byte[524288]; 
			int nRead; 
			// 从输入流中读入字节流，然后写到文件中 
			while((nRead=in.read(b,0,524288)) > 0) 
			{ 
			oSavedFile.write(b,0,nRead); 
			}
			oSavedFile.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
