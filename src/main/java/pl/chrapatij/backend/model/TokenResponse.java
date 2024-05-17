package pl.chrapatij.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(@JsonProperty(value = "auth-token") String token) {
}