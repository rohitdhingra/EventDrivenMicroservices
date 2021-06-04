package com.example.twitter.to.kafka.service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.avro.model.TwitterAvroModel;
import com.example.config.KafkaConfigData;
import com.example.kafka.producer.service.KafkaProducer;
import com.example.twitter.to.kafka.service.transformer.TwitterStatusToAvroTransformer;

import twitter4j.Status;
import twitter4j.StatusAdapter;
@Component
public class TwitterKafkaStatusListener extends StatusAdapter{
	
	private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaStatusListener.class);
	
	private final KafkaConfigData kafkaConfigData;
	
	private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
	
	private final TwitterStatusToAvroTransformer twitterStatusToAvroTransformer;
	
	

	public TwitterKafkaStatusListener(KafkaConfigData kafkaConfigData,
			KafkaProducer<Long, TwitterAvroModel> kafkaProducer,
			TwitterStatusToAvroTransformer twitterStatusToAvroTransformer) {
		super();
		this.kafkaConfigData = kafkaConfigData;
		this.kafkaProducer = kafkaProducer;
		this.twitterStatusToAvroTransformer = twitterStatusToAvroTransformer;
	}



	@Override
	public void onStatus(Status status) {
		LOG.info("Twitter Status text {} sending to kafka topic {}",status.getText(),kafkaConfigData.getTopicName());
		TwitterAvroModel twitterAvroModel = twitterStatusToAvroTransformer.getTwitterAvroModelFromStatus(status);
		kafkaProducer.send(kafkaConfigData.getTopicName(), twitterAvroModel.getUserId(), twitterAvroModel);
	}

}
