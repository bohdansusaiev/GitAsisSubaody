package com.sigmadevs.aiintegration.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.nio.file.*;
import java.io.*;
import java.util.*;


public class GitHubService {
    private String existSha = null;
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String GITHUB_TOKEN = "";
    private static final String GITHUB_USERNAME = "Acheron1232";

    public void createRepositoryWithProject(String projectPath, String repoName, String githubToken, String githubUsername) throws IOException {
        File projectDir = new ClassPathResource("/projects/" + projectPath).getFile();
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            throw new IllegalArgumentException("Невірний шлях до проекту");
        }
        createGitHubRepository(repoName);
        List<File> files = listFilesInDirectory(projectDir);

        List<Map<String, String>> tree = new ArrayList<>();
        for (File file : files) {
            String base64Content = encodeFileToBase64(file);
            String sha = createBlob(file.getName(), base64Content, repoName);
            Path filePath = file.toPath();
            Path relativePath = projectDir.toPath().relativize(filePath);

            tree.add(createTreeEntry(relativePath.toString(), sha));
//                tree.add(createTreeEntry(file.getPath(), sha));
        }
        createBranch(repoName, "main", "new-feature");
        String existTree = getTree("new-feature", repoName);
        String treeSha = createTree(tree, repoName, existTree);
        String commitSha = createCommit(treeSha, repoName);
        updateBranch(commitSha, repoName, "new-feature");
        System.out.println(tree);
    }

    public void createGitHubRepository(String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/user/repos";

        Map<String, Object> repoData = new HashMap<>();
        repoData.put("name", repoName);
        repoData.put("private", true);
        repoData.put("auto_init", true);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(repoData, headers);

        restTemplate.postForEntity(url, entity, String.class);
    }

    public void deleteGitHubRepository(String repoName,String githubUsername) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/repos/"+githubUsername+"/"+repoName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public List<Map<String, Object>> getUser() {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        return response.getBody();
    }
    public List<Map<String, Object>> getUserRepos() {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/user/repos";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody();
    }

    public String getTree(String branch, String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        String url = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/branches/" + branch;
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Map<String, Object> commit = (Map<String, Object>) body.get("commit");
        existSha = (String) commit.get("sha");
        Map<String, Object> commitDetails = (Map<String, Object>) commit.get("commit");

        Map<String, Object> treeMap = (Map<String, Object>) commitDetails.get("tree");
        String baseTreeSha = (String) treeMap.get("sha");
        return baseTreeSha;
    }

    public void createBranch(String repoName, String fromBranchName, String branchName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/git/ref/heads/" + fromBranchName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> mainRefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> mainRef = mainRefResponse.getBody();
            Map<String, Object> objectData = (Map<String, Object>) mainRef.get("object");
            String sha = (String) objectData.get("sha");

            Map<String, Object> branchData = new HashMap<>();
            branchData.put("ref", "refs/heads/" + branchName);
            branchData.put("sha", sha);

            String createBranchUrl = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/git/refs";
            HttpEntity<Map<String, Object>> createBranchEntity = new HttpEntity<>(branchData, headers);
            restTemplate.exchange(createBranchUrl, HttpMethod.POST, createBranchEntity, String.class);

            System.out.println("Branch " + branchName + " created.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating branch: " + e.getMessage());
        }
    }

    private String createBlob(String fileName, String base64Content, String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/git/blobs";

        Map<String, Object> blobData = new HashMap<>();
        blobData.put("content", base64Content);
        blobData.put("encoding", "base64");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(blobData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody().get("sha").toString();
    }

    private Map<String, String> createTreeEntry(String path, String sha) {
        path = path.replace("\\", "/");
        Map<String, String> treeEntry = new HashMap<>();
        treeEntry.put("path", path);
        treeEntry.put("mode", "100644");
        treeEntry.put("type", "blob");
        treeEntry.put("sha", sha);
        return treeEntry;
    }

    private String createTree(List<Map<String, String>> tree, String repoName, String existTree) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/git/trees";

        Map<String, Object> treeData = new HashMap<>();
        treeData.put("tree", tree);

        treeData.put("base_tree", existTree);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(treeData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody().get("sha").toString();
    }

    private String createCommit(String treeSha, String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/git/commits";

        Map<String, Object> commitData = new HashMap<>();
        commitData.put("message", "Initial commit");
        commitData.put("tree", treeSha);
        commitData.put("parents", Collections.singleton(existSha));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(commitData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody().get("sha").toString();
    }

    private void updateBranch(String commitSha, String repoName, String branchName) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        String url = GITHUB_API_URL + "/repos/" + GITHUB_USERNAME + "/" + repoName + "/git/refs/heads/" + branchName;

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("sha", commitSha);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);

        try {
            restTemplate.patchForObject(url, entity, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                System.out.println("Update is not a fast-forward. Attempting to create a new commit.");
                System.out.println(e.getResponseBodyAsString());
            }
        }
    }

    private List<File> listFilesInDirectory(File directory) {
        List<File> files = new ArrayList<>();
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    files.addAll(listFilesInDirectory(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    private String encodeFileToBase64(File file) throws IOException {
        byte[] bytes = new FileInputStream(file).readAllBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

}
