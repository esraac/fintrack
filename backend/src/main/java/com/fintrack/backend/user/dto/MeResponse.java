package com.fintrack.backend.auth.dto;

import java.util.UUID;

public record MeResponse(UUID id, String email) {}