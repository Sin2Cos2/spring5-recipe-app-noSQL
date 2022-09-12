package guru.springframework.services;

import guru.springframework.commands.UnitOfMeasureCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface UnitOfMeasureService {

    Flux<UnitOfMeasureCommand> findAll();
    Mono<UnitOfMeasureCommand> findById(String id);
}
