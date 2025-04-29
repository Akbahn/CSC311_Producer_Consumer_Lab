package csc311_producer_consumer_lab;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class SimpleProducerConsumerDriver {
    private static final Logger log = Logger.getLogger(SimpleProducerConsumerDriver.class.getCanonicalName());
    private static final int QUEUE_CAPACITY = 5;
    private static final int PRODUCER_COUNT = 2;
    private static final int CONSUMER_COUNT = 3;
    private static final int RUN_DURATION_SECONDS = 5;

    private final BlockingQueue<Double> blockingQueue = new LinkedBlockingDeque<>(QUEUE_CAPACITY);
    private volatile boolean running = true;

    private Runnable producerTask() {
        return () -> {
            while (running) {
                double value = generateValue();
                try {
                    blockingQueue.put(value);
                    log.info(String.format("[%s] Value produced: %f", Thread.currentThread().getName(), value));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };
    }

    private Runnable consumerTask() {
        return () -> {
            while (running) {
                try {
                    Double value = blockingQueue.take();
                    log.info(String.format("[%s] Value consumed: %f", Thread.currentThread().getName(), value));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };
    }

    private double generateValue() {
        return Math.random();
    }

    private void runProducerConsumer() {
        ExecutorService executor = Executors.newFixedThreadPool(PRODUCER_COUNT + CONSUMER_COUNT);

        for (int i = 0; i < PRODUCER_COUNT; i++) {
            executor.submit(producerTask());
        }

        for (int i = 0; i < CONSUMER_COUNT; i++) {
            executor.submit(consumerTask());
        }

        try {
            Thread.sleep(RUN_DURATION_SECONDS * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stop tasks
        running = false;
        executor.shutdownNow();  // Interrupt all running threads
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate in time.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SimpleProducerConsumerDriver driver = new SimpleProducerConsumerDriver();
        driver.runProducerConsumer();
    }
}
