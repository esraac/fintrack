package com.fintrack.backend.user.dto;

import java.util.UUID;

public record MeResponse(UUID id, String email) {}