package com.example.kafka.producer.service.impl;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.example.avro.model.TwitterAvroModel;
import com.example.kafka.producer.service.KafkaProducer;
import javax.annotation.PreDestroy;

@Service
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaProducer.class);
	
	private KafkaTemplate<Long,TwitterAvroModel> kafkaTemplate;
	
	
	public TwitterKafkaProducer(KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}


	@Override
	public void send(String topicName, Long key, TwitterAvroModel message) {
		LOG.info("Sending message ='{}' to topic '{}'",message,topicName);
		ListenableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);
		addCallBack(topicName, message, kafkaResultFuture);
	}
	@PreDestroy
	public void close()
	{
		if(kafkaTemplate!=null)
		{
			LOG.info("Closing Kafka Producer");
			kafkaTemplate.destroy();
		}
	}

	private void addCallBack(String topicName, TwitterAvroModel message,
			ListenableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture) {
		kafkaResultFuture.addCallback(new ListenableFutureCallback<>() {

			@Override
			public void onSuccess(SendResult<Long, TwitterAvroModel> result) {
				// TODO Auto-generated method stub
				RecordMetadata metadata = result.getRecordMetadata();
				LOG.debug("Received new metadata.Topic: {}; Partition {} ;Offset {} ; Timestamp {} at time {}",
						metadata.topic(),
						metadata.partition(),
						metadata.offset(),
						metadata.timestamp(),
						System.nanoTime());
			}

			@Override
			public void onFailure(Throwable ex) {
				LOG.error("Error while sending message {} to topic {}",message.toString(),topicName,ex);
			}
			
		});
	}


}
