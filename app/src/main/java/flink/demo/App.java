/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package flink.demo;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
    public static FlinkKafkaConsumer<String> createStringConsumerForTopic(
            String topic, String kafkaAddress, String kafkaGroup ) {

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkaAddress);
        props.setProperty("group.id",kafkaGroup);
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<String>(
                topic, new SimpleStringSchema(), props);
        return consumer;
    }
    public static void createBackup () throws Exception {
        String inputTopic = "flink_input";
        String outputTopic = "flink_output";
        String consumerGroup = "baeldung";
        String kafkaAddress = "192.168.99.100:9092";
        StreamExecutionEnvironment environment
                = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        FlinkKafkaConsumer<InputMessage> flinkKafkaConsumer
                = createInputMessageConsumer(inputTopic, kafkaAddress, consumerGroup);
        flinkKafkaConsumer.setStartFromEarliest();

        flinkKafkaConsumer.assignTimestampsAndWatermarks(
                new InputMessageTimestampAssigner());
        FlinkKafkaProducer<Backup> flinkKafkaProducer
                = createBackupProducer(outputTopic, kafkaAddress);

        DataStream<InputMessage> inputMessagesStream
                = environment.addSource(flinkKafkaConsumer);

        inputMessagesStream
                .timeWindowAll(Time.hours(24))
                .aggregate(new BackupAggregator())
                .addSink(flinkKafkaProducer);

        environment.execute();
    }
    public static FlinkKafkaProducer<String> createStringProducer(
            String topic, String kafkaAddress){

        return new FlinkKafkaProducer<>(kafkaAddress,
                topic, new SimpleStringSchema());
    }
    public static FlinkKafkaProducer<Backup> createBackupProducer(String topic, String kafkaAddress){
        return new FlinkKafkaProducer<>(kafkaAddress,
                topic, new BackupSerializationSchema());
    }
    public static FlinkKafkaConsumer<InputMessage> createInputMessageConsumer(String topic, String kafkaAddress, String kafkaGroup ){

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkaAddress);
        props.setProperty("group.id",kafkaGroup);
        FlinkKafkaConsumer<InputMessage> consumer = new FlinkKafkaConsumer<InputMessage>(
                topic, new InputMessageDeserializationSchema(), props);
        return consumer;
    }
    public static void capitalize() {
        String inputTopic = "flink_input";
        String outputTopic = "flink_output";
        String consumerGroup = "baeldung";
        String address = "localhost:9092";
        StreamExecutionEnvironment environment = StreamExecutionEnvironment
                .getExecutionEnvironment();
        FlinkKafkaConsumer<String> flinkKafkaConsumer = createStringConsumerForTopic(
                inputTopic, address, consumerGroup);
        DataStream<String> stringInputStream = environment
                .addSource(flinkKafkaConsumer);

        FlinkKafkaProducer<String> flinkKafkaProducer = createStringProducer(
                outputTopic, address);

        stringInputStream
                .map(new WordsCapitalizer())
                .addSink(flinkKafkaProducer);
    }
}
