package org.example;

import java.math.BigDecimal;
import java.util.List;

public record ComponentDetails(ComponentType componentType, List<Alpha> alphas) {
}
