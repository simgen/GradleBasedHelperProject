package org.gradle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

//http://10.148.155.245:15672/api/queues/%2F/prepaid-sms-data-varsha
//@Service
@Service(value="reporting")
public class SampleRabbitReportingCollectionManager {
	private static final Logger log = LogManager.getLogger(SampleRabbitReportingCollectionManager.class);
	

	@Value("${rabbit.username}")
	private String username;

	@Value("${rabbit.password}")
	private String password;

	@Value("${rabbit.reporting.uri}")
	private String rabbitUri;

	@Value("${rabbit.reporting.queues}")
	private String reportingQueues;

	@Value("${rabbit.queue1}")
	private String data_queue1;

	@Value("${rabbit.queue2}")
	private String data_queue2;

	@Value("${rabbit.http.timeout}")
	private String timeout;

	@Value("${rabbit.reporting.dir.path}")
	private String reportingDirPath;

	@Value("${rabbit.queue.age.parameters}")
	private String ageLimit;

	private static final String UnAuthorized = "UNAUTHORIZED_EXCEPTION";
	private static final String pageNotFound = "PAGE_NOT_FOUND";
	private static final String internalServerError = "INTERNAL_SERVER_ERROR";
	private static final String reportingFileHeader = "Source|Destination|MessagesReceived";
	private static final String smsSrcDest = "SMSSrc|MyService|";
	private static final String subscriberSrcDest = "DATASrc|MyService|";

	// Bean blows up if you specify a constructor and not a default constructor
	public SampleRabbitReportingCollectionManager() {
	}

	public void generateReportingServiceData() throws Exception {

		String str = reportingQueues;
		String[] reportingQueueList = str.split(",");

		for (String reportingQueue : reportingQueueList) {
			System.out.println("Fetching getMessageCount for q:" + reportingQueue);
			String response = getMessageCount(reportingQueue);

			if (response != null) {
				JSONParser parser = new JSONParser();

				try {
					JSONObject json = (JSONObject) parser.parse(response);
					String ackMessageCount = "0";
					if (json != null) {
						// get message_stats object from main json
						JSONObject messageStats = (JSONObject) json.get("message_stats");
						if (messageStats != null) {
							ackMessageCount = (String) messageStats.get("ack").toString();
							writeToFile(ackMessageCount, reportingQueue);
						} else {
							log.error("MessageStats Json is invalid - Check Rabbit api");
						}
					} else {
						log.error("Rabbit reporting Json is invalid - Check Rabbit api");
					}

				} catch (ParseException e) {
					throw new Exception("Invalid JSON.", e);
				}
			}
		}
	}

	private String getMessageCount(String queueName) throws Exception {
		String url = rabbitUri + queueName + ageLimit;
		
		System.out.println("url" + url);

		HttpURLConnection c = null;
		try {
			String userpass = username + ":" + password;
			String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

			URL u = new URL(url);
			c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(2000);
			c.setReadTimeout(2000);
			c.setRequestProperty("Authorization", basicAuth);
			c.connect();
			int status = c.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				return sb.toString();
			case 401:
				log.error("Unauthorized to query rabbit api");
				throw new Exception(UnAuthorized);
			case 404:
				// Do not throw exception so that we can continue fetching next
				// queue in the list
				// Come here when queuenName is not found
				log.error("Exception:pageNotFound:" + url);
				return null;
			case 500:
				throw new Exception(internalServerError);
			}

		} catch (MalformedURLException e) {

			throw new Exception("MalformedURLException");

		} catch (ConnectException e) {

			throw new Exception("ConnectException");

		} catch (IOException e) {

			throw new Exception("IOException");

		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception e) {
					throw new Exception("IOException");
				}
			}
		}
		return null;
	}

	private void writeToFile(String messageCount, String queueName) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();

		String fileName = queueName + "_" + dateFormat.format(date) + ".txt";
		String absoluteFilePath = reportingDirPath + fileName;
		System.out.println("File:" +absoluteFilePath );
		String srcDestStr = "";

		if (queueName.equals(data_queue1)) {
			srcDestStr = smsSrcDest;
		} else if (queueName.equals(data_queue2)) {
			srcDestStr = subscriberSrcDest;
		}

		log.debug("Filepath : " + absoluteFilePath);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(absoluteFilePath, "UTF-8");
			writer.println(reportingFileHeader);
			writer.println(srcDestStr + messageCount);
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (UnsupportedEncodingException e) {
			log.error(e);

		} finally {
			if (writer != null) {
				writer.close();
			}
		}

	}
}
