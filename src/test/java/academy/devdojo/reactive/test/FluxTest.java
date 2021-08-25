package academy.devdojo.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
class FluxTest {

    @Test
    void fluxSubscriber() {
        Flux<String> stringFlux = Flux.just("jeff5m", "Github", "Developer")
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("jeff5m", "Github", "Developer")
                .verifyComplete();
    }

    @Test
    void fluxSubscriberNumbers() {
        Flux<Integer> integerFlux = Flux.range(1, 5)
                .log();

        integerFlux.subscribe(i -> log.info("Number {}", i));

        log.info("------------------------------------------");

        StepVerifier.create(integerFlux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

}
