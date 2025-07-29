package ewm.request.service;

import java.util.List;
import java.util.Map;

public interface RequestService {
    Map<Long, Long> getConfirmedRequestMap(List<Long> eventIds);

    Long countAllByEventIdAndStatusIs(Long eventId,
                                      String requestStatus);

    boolean isRequestExist(long userId, long eventId);
}
