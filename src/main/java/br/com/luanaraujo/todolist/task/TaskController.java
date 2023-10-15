package br.com.luanaraujo.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.luanaraujo.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  
    /**
     * http://localhost:8080/tasks/
     * 
     * "description":"Tarefa para grvar aula de tasks do Curso de spring boot",
     * "title":"Gravação de aula",
     * "priority":"ALTA",
     * "startAt":"2023-10-06T12:30:00",
     * "endAt":"2023-10-06T15:35:00",
     * "idUser":""
     */

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now(); // Retorna a data atual
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {  // verifica se a data é menor que a do getStartAt()
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início / término deve ser superior a data atual");
        } 

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) { // Verifica se a data de inicio é supeerior a data de termino
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("A data de início deve ser inferior a data de término");
        } 

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return tasks;
    }

    // http://localhost:8080/tasks/892347823-cdfgcvb-832748234
    @PutMapping("{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id , HttpServletRequest request) {
        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não existe");
        }

        var idUser = request.getAttribute("idUser");

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }
        
        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }
}
