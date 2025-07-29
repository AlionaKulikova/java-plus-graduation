package ewm.request.service.interfaces;

import java.util.List;
import java.util.Map;

public interface RequestService {
    Map<Long, Long> getConfirmedRequests(List<Long> eventIds);

    Long countAllByEventIdAndStatus(Long eventId, String requestStatus);

    boolean isRequestExist(Long userId, Long eventId);
}