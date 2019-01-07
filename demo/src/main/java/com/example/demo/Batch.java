package com.example.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Batch {
	private final static String KOREANAIR_URL = "https://www.koreanair.com/api/fly/revenue/from/GMP/to/HIN/on/02-04-2019-0000";
	private final static String KOREANAIR2_URL = "https://www.koreanair.com/api/fly/revenue/from/HIN/to/GMP/on/02-05-2019-0000";
	private final static String LINE_TOKEN = "";
	private final static String LINE_URL = "https://notify-api.line.me/api/notify";

	@Scheduled(cron = "0 */1 * * * *")
	public void ch() {
		System.out.println("배치 수행:"+System.currentTimeMillis());
		JSONObject responseJson = new JSONObject();
		String[] array = { KOREANAIR_URL, KOREANAIR2_URL };
		String go = "";
		String back = "";
		String checkString = "";
		for (String url : array) {
			try {

				URL obj = new URL(url);
				HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
				con.setRequestProperty("page-id", "/booking/dow.html");
				con.setRequestProperty("uidd", "83^51%8638461@384712");
				con.setRequestProperty("Referer", "https://www.koreanair.com/korea/ko/booking/dow.html");
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				con.setConnectTimeout(10000);
				con.setReadTimeout(10000);
				con.setDoOutput(true);

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer rs = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					rs.append(inputLine);
				}
				in.close();
				responseJson = new JSONObject(rs.toString());

			} catch (Exception e) {

			}
			
			if (url.equals(KOREANAIR_URL)) {
				go = "김포->진주 2월4일 오전:" + responseJson.getJSONArray("outbound").getJSONObject(0).getJSONObject("remainingSeatsByBookingClass").toString()+"\n"
						+"김포->진주 2월4일 오후:" + responseJson.getJSONArray("outbound").getJSONObject(1).getJSONObject("remainingSeatsByBookingClass").toString();
			} else {
				back = "진주->김포 2월5일 오전:" + responseJson.getJSONArray("outbound").getJSONObject(0).getJSONObject("remainingSeatsByBookingClass").toString()+"\n"
						+"진주->김포 2월5일 오후:" + responseJson.getJSONArray("outbound").getJSONObject(1).getJSONObject("remainingSeatsByBookingClass").toString();
				checkString = responseJson.getJSONArray("outbound").getJSONObject(1).getJSONObject("remainingSeatsByBookingClass").toString();
			}

		}
		if(!StringUtils.isEmpty(checkString)&& checkString.contains(":")) {
			if(Integer.parseInt(checkString.split(":")[1]) > 0) {
				try {
					URL obj = new URL(LINE_URL);
					HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Authorization", "Bearer " + LINE_TOKEN);
					con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

					con.setConnectTimeout(10000);
					con.setReadTimeout(10000);
					con.setDoInput(true);
					con.setDoOutput(true);
					Map<String, Object> params = new LinkedHashMap<>();

					params.put("message", go + "\n" + back);

					StringBuilder postData = new StringBuilder();
					for (Map.Entry<String, Object> param : params.entrySet()) {
						if (postData.length() != 0)
							postData.append('&');
						postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
						postData.append('=');
						postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
					}
					byte[] postDataBytes = postData.toString().getBytes("UTF-8");

					OutputStream os = con.getOutputStream();
					os.write(postDataBytes);
					os.close();

					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer rs = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						rs.append(inputLine);
					}
					in.close();
					responseJson = new JSONObject(rs.toString());
					System.out.println(responseJson);
				} catch (Exception e) {

				}
			}
		}		
	}
}
