package org.reliability.dto;

import java.util.List;

public record ComponentDetails(ComponentType componentType, List<Alpha> alphas) {
}
