package system.integration.videostorage.service;

import system.integration.videostorage.dto.VideoStorageUploadResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class KinescopeIds {
    private static final Map<UUID, UUID> kinescopeIds = new ConcurrentHashMap<>();
    private static final Map<UUID, VideoStorageUploadResponse> dto = new ConcurrentHashMap();

    public static void addId(UUID id, UUID taskId, VideoStorageUploadResponse videoStorageUploadResponse) {
        kinescopeIds.put(id, taskId);
        dto.put(taskId, videoStorageUploadResponse);
    }
    public static void deleteId(UUID id) {
        dto.remove(kinescopeIds.get(id));
        kinescopeIds.remove(id);
    }
    public static Map<UUID, UUID> getAll() {
        return kinescopeIds;
    }
    public static VideoStorageUploadResponse getData(UUID taskId) {
        return dto.get(taskId);
    }
}