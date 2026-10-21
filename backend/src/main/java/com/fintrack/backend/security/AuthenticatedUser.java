package com.fintrack.backend.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String email) {}