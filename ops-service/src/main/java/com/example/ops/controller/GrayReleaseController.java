package com.example.ops.controller;

import com.example.ops.service.GrayReleaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 灰度发布 REST API
 */
@RestController
@RequestMapping("/ops/releases")
public class GrayReleaseController {

    @Autowired
    private GrayReleaseService grayReleaseService;

    /**
     * 创建灰度发布
     */
    @PostMapping
    public Map<String, Object> createRelease(@RequestBody Map<String, String> request) {
        String serviceName = request.get("serviceName");
        String version = request.get("version");

        GrayReleaseService.GrayRelease release = grayReleaseService.createRelease(serviceName, version);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("releaseId", release.getReleaseId());
        result.put("status", release.getStatus());
        return result;
    }

    /**
     * 获取所有发布
     */
    @GetMapping
    public Map<String, Object> getReleases() {
        List<GrayReleaseService.GrayRelease> releases = grayReleaseService.getAllReleases();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("releases", releases);
        return result;
    }

    /**
     * 获取发布详情
     */
    @GetMapping("/{releaseId}")
    public Map<String, Object> getRelease(@PathVariable String releaseId) {
        GrayReleaseService.GrayRelease release = grayReleaseService.getRelease(releaseId);
        Map<String, Object> result = new HashMap<>();
        if (release != null) {
            result.put("success", true);
            result.put("release", release);
        } else {
            result.put("success", false);
            result.put("message", "Release not found");
        }
        return result;
    }

    /**
     * 调整流量
     */
    @PutMapping("/{releaseId}/traffic")
    public Map<String, Object> adjustTraffic(
            @PathVariable String releaseId,
            @RequestBody Map<String, Integer> request) {

        int ratio = request.get("ratio");
        boolean success = grayReleaseService.adjustTraffic(releaseId, ratio);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (success) {
            result.put("trafficRatio", ratio);
            result.put("message", "Traffic adjusted");
        } else {
            result.put("message", "Failed to adjust traffic");
        }
        return result;
    }

    /**
     * 完成灰度发布
     */
    @PostMapping("/{releaseId}/complete")
    public Map<String, Object> completeRelease(@PathVariable String releaseId) {
        boolean success = grayReleaseService.completeRelease(releaseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Release completed" : "Failed to complete");
        return result;
    }

    /**
     * 回滚
     */
    @PostMapping("/{releaseId}/rollback")
    public Map<String, Object> rollback(@PathVariable String releaseId) {
        boolean success = grayReleaseService.rollback(releaseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Rolled back successfully" : "Failed to rollback");
        return result;
    }

    /**
     * 删除发布
     */
    @DeleteMapping("/{releaseId}")
    public Map<String, Object> deleteRelease(@PathVariable String releaseId) {
        boolean success = grayReleaseService.deleteRelease(releaseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Release deleted" : "Failed to delete");
        return result;
    }
}
