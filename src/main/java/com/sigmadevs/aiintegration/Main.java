package com.sigmadevs.aiintegration;

import com.sigmadevs.aiintegration.service.GitHubService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        GitHubService gitHubService = new GitHubService();

        gitHubService.createRepositoryWithProject("projectab5ef367-1177-4164-bfa6-f5e447c222ab","testQ4",null,null);
//        gitHubService.deleteGitHubRepository("testQ","Acheron1232");

//        System.out.println(gitHubService.getUserRepos());
    }
}
