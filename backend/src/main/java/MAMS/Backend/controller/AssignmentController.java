package MAMS.Backend.controller;

import MAMS.Backend.entity.Assignment;
import MAMS.Backend.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<Assignment> getAll() {
        return assignmentService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Assignment> create(
            @RequestBody Assignment assignment) {

        return ResponseEntity.ok(assignmentService.create(assignment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> update(
            @PathVariable Long id,
            @RequestBody Assignment assignment) {

        return ResponseEntity.ok(
                assignmentService.update(id, assignment)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/base/{baseId}")
    public List<Assignment> getByBase(@PathVariable Long baseId) {
        return assignmentService.getByBase(baseId);
    }

    @GetMapping("/personnel/{personnelName}")
    public List<Assignment> getByPersonnel(
            @PathVariable String personnelName) {

        return assignmentService.getByPersonnel(personnelName);
    }
}