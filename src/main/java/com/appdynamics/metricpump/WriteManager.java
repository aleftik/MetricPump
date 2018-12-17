package com.appdynamics.metricpump;

import com.appdynamics.metricpump.api.MetricWriter;
import com.appdynamics.metricpump.kafka.GsonSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteManager implements Runnable {
    private static final Logger logger = Logger.getLogger(WriteManager.class.getName());
    private BlockingQueue<MetricUploadRequest> queue = null;
    private ExecutorService service = null;

    private List<MetricWriter> writers = null;


    public WriteManager(BlockingQueue<MetricUploadRequest> queue, List<MetricWriter> writers, int writerThreadCount) {
        this.queue = queue;
        this.writers = writers;
        this.service = Executors.newFixedThreadPool(writerThreadCount);
    }


    @Override
    public void run() {
        while (true) {
            try {
                MetricUploadRequest request = queue.take();
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        produceMessage(request);
                        for (MetricWriter writer : writers) {
                            writer.write(request);
                        }
                    }
                });
            } catch (InterruptedException ie) {
                logger.log(Level.SEVERE, "Interrupted", ie);
            }
        }
    }

    private void produceMessage(MetricUploadRequest request) {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "");

// ... other producer properties ...
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", GsonSerializer.class.getName());

            Producer<String, MetricUploadRequest> producer = new KafkaProducer<>(props);
            producer.send(new ProducerRecord<>("metrics", request));
            producer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
