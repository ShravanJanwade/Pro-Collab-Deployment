package pl.rengreen.taskmanager.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.rengreen.taskmanager.model.Project;
import pl.rengreen.taskmanager.model.Teams;
import pl.rengreen.taskmanager.model.User;
import pl.rengreen.taskmanager.repository.TeamRepository;
import pl.rengreen.taskmanager.service.ProjectService;
import pl.rengreen.taskmanager.service.TeamService;
import pl.rengreen.taskmanager.service.UserService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/project")
public class TeamsController {
    private final ProjectService projectService;
    private final UserService userService;
    private final TeamService teamService;

    @Autowired
    private TeamRepository repo;

    @Autowired
    public TeamsController(ProjectService projectService, UserService userService, TeamService teamService) {
        this.projectService = projectService;
        this.userService = userService;
        this.teamService = teamService;
    }

    @GetMapping("/createTeam/{projectId}")
    public String createTeam(@PathVariable("projectId") long projectId, Model model) {
        Project project = projectService.getProjectById(projectId);
        model.addAttribute("project", project);
        return "forms/createTeam";
    }

    @PostMapping("/{id}/createTeam")
    public String createTeam(@PathVariable Long id, @RequestParam("name") String name) {
        Project project = projectService.getProjectById(id);
        Teams team = new Teams();
        team.setName(name);
        team.setProject(project);
        teamService.createTeam(team);
        return "redirect:/project/" + id + "/teams";
    }

    @GetMapping("/{id}/teams")
    public String getTeams(@PathVariable("id") Long id, Model model) {
        List<Teams> teams = repo.findAllByProjectId(id);
        model.addAttribute("teams", teams);
        return "forms/teams";
    }

    @GetMapping("/teams/{projectId}/addUser/{teamId}")
    public String addUserToTeam(@PathVariable("projectId") long projectId, @PathVariable("teamId") long teamId,
            Model model) {
        List<User> projectUsers = projectService.getAllProjectEmployees(projectId);
        List<User> teamUsers = teamService.getTeamById(teamId).get().getUsers();
        List<Teams> allTeams = repo.findAllByProjectId(projectId);

        Set<User> allTeamUsers = new HashSet<>();
        for (Teams team : allTeams) {
            allTeamUsers.addAll(team.getUsers());
        }
        projectUsers.removeAll(allTeamUsers);
        for (User user : teamUsers) {
            projectUsers.remove(user);
        }
        Project project = projectService.getProjectById(projectId);
        User user = project.getCreator();
        projectUsers.remove(user);
        model.addAttribute("availableUsers", projectUsers);
        model.addAttribute("projectId", projectId);
        model.addAttribute("teamId", teamId);
        model.addAttribute("teamUsers", teamUsers);
        return "views/addTeamMembers";
    }

    @GetMapping("/assignment/delete/project/{projectId}/team/{teamId}/user/{userId}")
    public String deleteFromTeam(@PathVariable long projectId, @PathVariable long teamId, @PathVariable long userId) {
        Project project = projectService.getProjectById(projectId);
        Teams team = teamService.getTeamById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        User user = userService.getUserById(userId);
        List<User> users = team.getUsers();
        users.remove(user);
        team.setUsers(users);
        teamService.createTeam(team);
        return "redirect:/project/teams/" + projectId + "/addUser/" + teamId;
    }

    @PostMapping("/{projectId}/teams/{teamId}/addUsers/{userId}")
    public ResponseEntity<Void> addUsersToTeam(@PathVariable Long projectId, @PathVariable Long teamId,
            @PathVariable Long userId) {
        Teams team = teamService.getTeamById(teamId).get();
        User user = userService.getUserById(userId);
        team.getUsers().add(user);
        teamService.saveTeam(team);
        return ResponseEntity.ok().build();
    }

    // Add other endpoints as needed
}
