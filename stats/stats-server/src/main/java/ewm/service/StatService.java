package ewm.service;

import ewm.dto.EndpointHitDto;
import ewm.dto.RequestParamDto;
import ewm.dto.ViewStatsDto;

import java.util.List;

public interface StatService {
    void hit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> stats(RequestParamDto params);
}
