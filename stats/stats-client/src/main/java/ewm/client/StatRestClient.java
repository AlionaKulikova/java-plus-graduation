package ewm.client;

import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRestClient {
    void addHit(EndpointHitDto hitDto);

    List<ViewStatsDto> stats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}