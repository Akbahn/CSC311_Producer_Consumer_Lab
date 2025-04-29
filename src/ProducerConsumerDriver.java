import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProducerConsumerDriver {
    private static final int MAX_QUEUE_CAPACITY = 5;
    private static final int PRODUCER_COUNT = 5;
    private static final int CONSUMER_COUNT = 5;

    public static void demoSingleProducerAndSingleConsumer() {
        DataQueue dataQueue = new DataQueue(MAX_QUEUE_CAPACITY);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Producer producer = new Producer(dataQueue);
        Consumer consumer = new Consumer(dataQueue);

        // We can use an executor to run both the producer and consumer and manage their lifecycle
        executor.submit(producer);
        executor.submit(consumer);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        producer.stop();
        consumer.stop();
        shutdownExecutor(executor);
    }

    public static void demoMultipleProducersAndMultipleConsumers() {
        DataQueue dataQueue = new DataQueue(MAX_QUEUE_CAPACITY);
        ExecutorService executor = Executors.newFixedThreadPool(PRODUCER_COUNT + CONSUMER_COUNT);

        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();

        for (int i = 0; i < PRODUCER_COUNT; i++) {
            Producer producer = new Producer(dataQueue);
            producers.add(producer);
            executor.submit(producer);
        }

        for (int i = 0; i < CONSUMER_COUNT; i++) {
            Consumer consumer = new Consumer(dataQueue);
            consumers.add(consumer);
            executor.submit(consumer);
        }

        try {
            Thread.sleep(10000); // Let them run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        producers.forEach(Producer::stop);
        consumers.forEach(Consumer::stop);
        shutdownExecutor(executor);
    }

    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // This will force shutdown if its still running
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        demoSingleProducerAndSingleConsumer();
        demoMultipleProducersAndMultipleConsumers();
    }
}
