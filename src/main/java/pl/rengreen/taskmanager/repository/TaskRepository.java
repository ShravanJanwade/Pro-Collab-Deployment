package pl.rengreen.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pl.rengreen.taskmanager.model.Project;
import pl.rengreen.taskmanager.model.Task;
import pl.rengreen.taskmanager.model.User;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerOrderByDateDesc(User user);

    long countByIsCompleted(boolean isCompleted);

    @Transactional
    void deleteAllByProject(Project project);
}