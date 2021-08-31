package academy.devdojo.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
class MonoTest {

    @BeforeAll
    static void setUp() {
        BlockHound.install();
    }

    @Test
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            Assertions.assertTrue(ex.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    void monoSubscriber() {
        String name = "jeff5m";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("----------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumer() {
        String name = "jeff5m";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(str -> log.info("Value {}", str));
        log.info("----------------------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerError() {
        String name = "jeff5m";
        Mono<String> mono = Mono.just(name)
                .map(str -> {
                    throw new RuntimeException("Testing mono with error");
                });

        mono.subscribe(str -> log.info("Name {}", str), str -> log.error("Something bad happened"));
        mono.subscribe(str -> log.info("Name {}", str), Throwable::printStackTrace);

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void monoSubscriberConsumerComplete() {
        String name = "jeff5m";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                str -> log.info("Value {}", str),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"));

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerSubscription() {
        String name = "jeff5m";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                str -> log.info("Value {}", str),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription -> subscription.request(5));

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoDoOnMethods() {
        String name = "jeff5m";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request received, start doing something..."))
                .flatMap(str -> Mono.empty())
                .doOnNext(str -> log.info("Value is here. Executing doOnNext {}", str)) // will not be executed
                .doOnSuccess(str -> log.info("doOnSuccess executed {}", str));

        mono.subscribe(
                str -> log.info("Value {}", str),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"));

        log.info("----------------------");

//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception error"))
                .doOnError(err -> MonoTest.log.error("Error message: {}", err.getMessage()))
                .doOnNext(str -> log.info("Executing this doOnNext")) // will not be executed
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void monoDoOnErrorResume() {
        String name = "jeff5m";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception error"))
                .onErrorResume(str -> { // continue executions besides the error
                    log.info("Inside onErrorResume");
                    return Mono.just(name);
                })
                .doOnError(err -> MonoTest.log.error("Error message: {}", err.getMessage())) // will not be executed
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    void monoDoOnErrorReturn() {
        String name = "jeff5m";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception error"))
                .onErrorReturn("EMPTY") // fallback object to be returned
                .onErrorResume(str -> { // will not be executed
                    log.info("Inside onErrorResume");
                    return Mono.just(name);
                })
                .doOnError(err -> MonoTest.log.error("Error message: {}", err.getMessage())) // will not be executed
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
