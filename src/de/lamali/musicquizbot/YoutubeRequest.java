package de.lamali.musicquizbot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeRequest {
	public static String getResult(String keyword){
		try {
			String theURL = "https://www.youtube.com/results?search_query="+keyword.replaceAll(" ", "+");

			URL url = new URL(theURL);
			InputStream is = url.openStream();
			int ptr = 0;
			StringBuffer buffer = new StringBuffer();
			while ((ptr = is.read()) != -1) {
				buffer.append((char)ptr);
			}
			String patternString1 = "watch\\?v=(\\S{11})";
			Pattern pattern1 = Pattern.compile(patternString1);
			Matcher matcher1 = pattern1.matcher(buffer);
			if (matcher1.find()) {
				return matcher1.group(1);
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
		return "dQw4w9WgXcQ";
	}
}
