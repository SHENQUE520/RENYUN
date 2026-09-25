package org.tenacitycodex.renyun.api.v1.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.DoctorListDTO;
import org.tenacitycodex.renyun.common.dto.PendingPatientDTO;
import org.tenacitycodex.renyun.common.dto.request.PatientApproveRequest;
import org.tenacitycodex.renyun.module.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PatientController {

    private final UserService userService;

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<List<DoctorListDTO>>> listDoctors() {
        List<DoctorListDTO> doctors = userService.listDoctors();
        return ResponseEntity.ok(ApiResponse.ok(doctors));
    }

    @GetMapping("/patients/pending")
    public ResponseEntity<ApiResponse<List<PendingPatientDTO>>> getPendingPatients() {
        List<PendingPatientDTO> patients = userService.getPendingPatients();
        return ResponseEntity.ok(ApiResponse.ok(patients));
    }

    @PostMapping("/patients/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approvePatient(
            @PathVariable Long id,
            @RequestBody(required = false) PatientApproveRequest request) {
        Long doctorId = request != null ? request.getDoctorId() : null;
        String doctorName = request != null ? request.getDoctorName() : null;
        userService.approvePatient(id, doctorId, doctorName);
        return ResponseEntity.ok(ApiResponse.ok("已批准"));
    }

    @PostMapping("/patients/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectPatient(@PathVariable Long id) {
        userService.rejectPatient(id);
        return ResponseEntity.ok(ApiResponse.ok("已拒绝"));
    }
}
