package com.example.ops.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 灰度发布服务
 */
@Service
public class GrayReleaseService {

    private final Map<String, GrayRelease> releases = new ConcurrentHashMap<>();

    /**
     * 发布状态
     */
    public enum ReleaseStatus {
        PENDING, RUNNING, COMPLETED, ROLLED_BACK
    }

    /**
     * 灰度发布
     */
    public static class GrayRelease {
        private final String releaseId;
        private final String serviceName;
        private final String version;
        private ReleaseStatus status = ReleaseStatus.PENDING;
        private int trafficRatio = 0;
        private final long createdAt;
        private long updatedAt;

        public GrayRelease(String serviceName, String version) {
            this.releaseId = "release-" + System.currentTimeMillis();
            this.serviceName = serviceName;
            this.version = version;
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;
        }

        public String getReleaseId() { return releaseId; }
        public String getServiceName() { return serviceName; }
        public String getVersion() { return version; }
        public ReleaseStatus getStatus() { return status; }
        public int getTrafficRatio() { return trafficRatio; }
        public long getCreatedAt() { return createdAt; }
        public long getUpdatedAt() { return updatedAt; }

        public void setStatus(ReleaseStatus status) { this.status = status; this.updatedAt = System.currentTimeMillis(); }
        public void setTrafficRatio(int ratio) { this.trafficRatio = ratio; this.updatedAt = System.currentTimeMillis(); }
    }

    /**
     * 创建灰度发布
     */
    public GrayRelease createRelease(String serviceName, String version) {
        GrayRelease release = new GrayRelease(serviceName, version);
        release.setStatus(ReleaseStatus.RUNNING);
        releases.put(release.getReleaseId(), release);
        return release;
    }

    /**
     * 获取所有发布
     */
    public List<GrayRelease> getAllReleases() {
        return new ArrayList<>(releases.values());
    }

    /**
     * 获取发布
     */
    public GrayRelease getRelease(String releaseId) {
        return releases.get(releaseId);
    }

    /**
     * 调整流量
     */
    public boolean adjustTraffic(String releaseId, int ratio) {
        GrayRelease release = releases.get(releaseId);
        if (release != null && release.getStatus() == ReleaseStatus.RUNNING) {
            release.setTrafficRatio(Math.max(0, Math.min(100, ratio)));
            return true;
        }
        return false;
    }

    /**
     * 完成灰度发布
     */
    public boolean completeRelease(String releaseId) {
        GrayRelease release = releases.get(releaseId);
        if (release != null) {
            release.setTrafficRatio(100);
            release.setStatus(ReleaseStatus.COMPLETED);
            return true;
        }
        return false;
    }

    /**
     * 回滚
     */
    public boolean rollback(String releaseId) {
        GrayRelease release = releases.get(releaseId);
        if (release != null) {
            release.setTrafficRatio(0);
            release.setStatus(ReleaseStatus.ROLLED_BACK);
            return true;
        }
        return false;
    }

    /**
     * 删除发布
     */
    public boolean deleteRelease(String releaseId) {
        return releases.remove(releaseId) != null;
    }
}
