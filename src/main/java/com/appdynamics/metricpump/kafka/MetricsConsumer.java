package com.appdynamics.metricpump.kafka;

import com.appdynamics.metricpump.MetricUploadRequest;
import org.apache.kafka.clients.consumer.*;


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

public class MetricsConsumer implements Runnable {
    private final Properties props = new Properties();
    private static final Logger logger = Logger.getLogger(MetricsConsumer.class.getName());

    public MetricsConsumer() {
        props.put("bootstrap.servers", "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,"test");
// ... other consumer properties ...
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", GsonDeserializer.class.getName());
        props.put(GsonDeserializer.CONFIG_VALUE_CLASS, MetricUploadRequest.class.getName());


    }

    @Override
    public void run() {
        KafkaConsumer<String, MetricUploadRequest> consumer = new KafkaConsumer<>(props);//        List<String> topics = {""}.

        consumer.subscribe(Arrays.asList("metrics"));
        while (true) {
            ConsumerRecords <String,MetricUploadRequest> records  = consumer.poll(100);
            Iterator<ConsumerRecord<String,MetricUploadRequest>> it = records.iterator();
            while (it.hasNext()) {
                ConsumerRecord<String, MetricUploadRequest> record = it.next();
                System.out.println("key is :" + record.key() + " value is " + record.value().getApplication().getName());

            }
        }
    }
}
