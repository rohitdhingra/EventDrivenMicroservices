package com.example.twitter.to.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.example.config.TwitterToKafkaServiceConfigData;
import com.example.twitter.to.kafka.service.init.StreamInitializer;
import com.example.twitter.to.kafka.service.runner.StreamRunner;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class); 
	
//	private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;
	
	private final StreamRunner streamRunner;
	
	private final StreamInitializer streamInitializer;

	public TwitterToKafkaServiceApplication(StreamInitializer streamInitializer,StreamRunner streamRunner) {
//		this.twitterToKafkaServiceConfigData = twitterToKafkaServiceConfigData;
		this.streamRunner = streamRunner;
		this.streamInitializer = streamInitializer;
	}

	public static void main(String[] args) {
		SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("App Starts");
//		LOG.info(twitterToKafkaServiceConfigData.getWelcomeMessage());
//		LOG.info(Arrays.toString(twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[] {})));
		streamInitializer.init();
		streamRunner.start();
	}

}
