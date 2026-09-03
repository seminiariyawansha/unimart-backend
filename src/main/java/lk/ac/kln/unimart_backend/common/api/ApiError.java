package lk.ac.kln.unimart_backend.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        String code,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> fieldErrors
) {}