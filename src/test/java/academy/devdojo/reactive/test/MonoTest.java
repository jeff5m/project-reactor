package academy.devdojo.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class MonoTest {

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
                Subscription::cancel);

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }
}
