package guru.springframework.services;

import guru.springframework.commands.UnitOfMeasureCommand;
import guru.springframework.converters.UnitOfMeasureToUnitOfMeasureCommand;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureReactiveRepository repository;
    private final UnitOfMeasureToUnitOfMeasureCommand toUnitOfMeasureCommand;

    @Override
    public Flux<UnitOfMeasureCommand> findAll() {
        return repository
                .findAll()
                .map(toUnitOfMeasureCommand::convert);
    }

    @Override
    public Mono<UnitOfMeasureCommand> findById(String id) {
        return repository
                .findById(id)
                .map(toUnitOfMeasureCommand::convert);
    }
}
