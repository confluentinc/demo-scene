package io.confluent.developer;

import io.confluent.developer.avro.Pizza;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface PizzaMapper {
    PizzaDto toDto(Pizza pizza);
    
    Pizza toAvro(PizzaDto pizzaDto);
}
