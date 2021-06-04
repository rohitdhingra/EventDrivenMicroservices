package com.example.twitter.to.kafka.service.runner.impl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.config.TwitterToKafkaServiceConfigData;
import com.example.twitter.to.kafka.service.exception.TwitterToKafkaServiceException;
import com.example.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import com.example.twitter.to.kafka.service.runner.StreamRunner;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets",havingValue = "true")
public class MockKafkaStreamRunner implements StreamRunner{

	private final static Logger LOG = LoggerFactory.getLogger(MockKafkaStreamRunner.class);
	
	private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;
	private final TwitterKafkaStatusListener twitterKafkaStatusListener;
	
	private static final Random RANDOM = new Random();
	private static final String[] WORDS = new String[] {
			"Lorem",
			"Ipsum",
			"sads",
			"weqwew",
			"wewgdfr",
			"hhewrrw"
	};
	
	private static final String tweetAsRawJSON="{\r\n" + 
			"  \"created_at\":\"{0}\",\r\n" + 
			"  \"id\":\"{1}\",\r\n" + 
			"  \"text\":\"{2}\",\r\n" + 
			"  \"user\":{\r\n" + 
			"    \"id\":\"{3}\"\r\n" + 
			"  }\r\n" + 
			"}";
	
	
	private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
		
	public MockKafkaStreamRunner(TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData,
			TwitterKafkaStatusListener twitterKafkaStatusListener) {
		super();
		this.twitterToKafkaServiceConfigData = twitterToKafkaServiceConfigData;
		this.twitterKafkaStatusListener = twitterKafkaStatusListener;
	}



	@Override
	public void start() throws TwitterException {
		// TODO Auto-generated method stub
	String[] keywords  = 	twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
	int maxTweetLength = twitterToKafkaServiceConfigData.getMockMaxTweetLength();
	int minTweetLength = twitterToKafkaServiceConfigData.getMockMinTweetLength();
	long sleepTimeMs = twitterToKafkaServiceConfigData.getMockSleepMs();
	LOG.info("Starting mock filtering twitter streams for keywords {}",Arrays.toString(keywords));
	
	simulateTwitterStream(keywords, maxTweetLength, minTweetLength, sleepTimeMs);
	}



	private void simulateTwitterStream(String[] keywords, int maxTweetLength, int minTweetLength, long sleepTimeMs)
	{
		Executors.newSingleThreadExecutor().submit(()-> {
			try {
				while(true)
				{
					String formattedTweetAsJSON = getFormattedTweet(keywords,minTweetLength,maxTweetLength);
					Status status = TwitterObjectFactory.createStatus(formattedTweetAsJSON);
					twitterKafkaStatusListener.onStatus(status);
					sleep(sleepTimeMs);
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				LOG.error("Error Creating Twitter Status",e);
			}
		});
		
	}



	private void sleep(long sleepTimeMs) {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(sleepTimeMs);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			throw new TwitterToKafkaServiceException("Error while sleeping for waiting status to create!!");
		}
	}



	private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
		String[] params = new String[] {
			ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT,Locale.ENGLISH)),
			String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
			getRandomTweetContent(keywords,minTweetLength,maxTweetLength),
			String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
		};
		
		return formatTweetAsJsonWithParams(params);
	}



	private String formatTweetAsJsonWithParams(String[] params) {
		String tweet = tweetAsRawJSON;
		for(int i=0;i<params.length;i++)
		{
			tweet = tweet.replace("{"+i+"}", params[i]);
		}
		return tweet;
	}



	private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {
		StringBuilder tweet = new StringBuilder();
		
		int tweetLength = RANDOM.nextInt(maxTweetLength-minTweetLength+1)+minTweetLength;
		return constructRandomTweets(keywords, tweet, tweetLength);
	}



	private String constructRandomTweets(String[] keywords, StringBuilder tweet, int tweetLength) {
		for(int i=0;i<tweetLength;i++)
		{
			tweet.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
			if(i == tweetLength/2)
			{
				tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
			}
		}
		return tweet.toString().trim();
	}

}
