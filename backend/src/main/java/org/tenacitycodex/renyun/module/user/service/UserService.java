package org.tenacitycodex.renyun.module.user.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.DoctorListDTO;
import org.tenacitycodex.renyun.common.dto.PendingPatientDTO;
import org.tenacitycodex.renyun.common.dto.request.PatientRegisterRequest;
import org.tenacitycodex.renyun.common.dto.request.RegisterRequest;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.common.exceptions.CacheException;
import org.tenacitycodex.renyun.common.exceptions.CacheMissedException;
import org.tenacitycodex.renyun.common.exceptions.PasswordIncorrectException;
import org.tenacitycodex.renyun.common.exceptions.UsernameNotFoundException;
import org.tenacitycodex.renyun.common.util.Snowflake;
import org.tenacitycodex.renyun.component.abstracts.IUserService;
import org.tenacitycodex.renyun.component.caching.UserCache;
import org.tenacitycodex.renyun.module.training.repository.TaskRepository;
import org.tenacitycodex.renyun.module.user.entity.DoctorProfile;
import org.tenacitycodex.renyun.module.user.entity.PatientProfile;
import org.tenacitycodex.renyun.module.user.entity.Task;
import org.tenacitycodex.renyun.module.user.entity.User;
import org.tenacitycodex.renyun.module.user.repository.DoctorProfileRepository;
import org.tenacitycodex.renyun.module.user.repository.PatientProfileRepository;
import org.tenacitycodex.renyun.module.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserCache userCacheEngine;
    private final PasswordEncoder passwordEncoder;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserService(UserRepository userRepository, UserCache userCacheEngine, PasswordEncoder passwordEncoder,
                       PatientProfileRepository patientProfileRepository,
                       DoctorProfileRepository doctorProfileRepository,
                       TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.userCacheEngine = userCacheEngine;
        this.passwordEncoder = passwordEncoder;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public void saveUser(@NonNull User user) {
        try {
            userCacheEngine.cache(user);
        } catch (CacheException e){
            User savedUser = userRepository.save(user);
            log.info("User saved to database: {}", savedUser.getId());
        }
    }
    @Override
    public User getUserById(@NonNull Long userId) {
        try {
            return userCacheEngine.getCachedById(userId);
        } catch (CacheMissedException e) {
            log.debug("User not in cache, fetching from database: {}", userId);
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userCacheEngine.cache(user);
                return user;
            }
        }
        return null;
    }

    @Override
    public User getUserByUsername(@NonNull String username) {
        try {
            return userCacheEngine.getUserByUsername(username);
        } catch (Exception e) {
            Optional<User> userOpt = userRepository.findUserByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userCacheEngine.setUserUsernameKey(user);
                return user;
            }
        }
        return null;
    }

    @Override
    public Page<User> getAllUsers(Pageable page) {
        return this.userRepository.findAll(page);
    }

    @Override
    public User loginViaUsernamePwd(String username, String password) throws UsernameNotFoundException, PasswordIncorrectException {
        User user = getUserByUsername(username);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                userCacheEngine.cache(user);
                return user;
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
    }

    @Override
    public User loginViaUsernameValidation(String username, String code) {
        return null;
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public Map<String, Object> buildFullUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("role", user.getRole());
        info.put("name", user.getName() != null ? user.getName() : user.getUsername());
        info.put("gender", user.getGender());
        info.put("status", user.getStatus());
        if ("patient".equals(user.getRole())) {
            PatientProfile pp = patientProfileRepository.findByUserId(user.getId()).orElse(null);
            if (pp != null) {
                info.put("age", pp.getAge());
                info.put("diagnosis", pp.getDiagnosis());
                info.put("hospital", pp.getHospital());
                info.put("doctorId", pp.getDoctorId());
                info.put("doctorName", pp.getDoctorName());
                info.put("surgeryDate", pp.getSurgeryDate());
            }
        } else if ("doctor".equals(user.getRole())) {
            DoctorProfile dp = doctorProfileRepository.findByUserId(user.getId()).orElse(null);
            if (dp != null) {
                info.put("hospital", dp.getHospital());
                info.put("department", dp.getDepartment());
                info.put("title", dp.getTitle());
                info.put("speciality", dp.getSpeciality());
            }
        }
        return info;
    }

    @Override
    public @Nullable User register(RegisterRequest request, String password) throws IllegalArgumentException{
        String hashedPassword = (passwordEncoder.encode(password));
        if (getUserByUsername(request.getUsername()) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "用户已注册");
        }
        User user = User
                .builder()
                .id(Snowflake.nextId())
                .createdAt(LocalDateTime.now())
                .username(request.getUsername())
                .passwordHash(hashedPassword)
                .role("patient")
                .name(request.getName())
                .status("active")
                .build();
        try {
            userRepository.save(user);
            userCacheEngine.setUserUsernameKey(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
        return user;
    }

    public List<DoctorListDTO> listDoctors() {
        List<User> doctors = userRepository.findByRoleAndStatusOrderByCreatedAtAsc("doctor", "active");
        List<DoctorListDTO> result = new ArrayList<>();
        for (User u : doctors) {
            DoctorProfile dp = doctorProfileRepository.findByUserId(u.getId()).orElse(null);
            result.add(DoctorListDTO.builder()
                    .id(u.getId())
                    .name(u.getName() != null ? u.getName() : u.getUsername())
                    .hospital(dp != null ? dp.getHospital() : null)
                    .department(dp != null ? dp.getDepartment() : null)
                    .title(dp != null ? dp.getTitle() : null)
                    .build());
        }
        return result;
    }

    public List<PendingPatientDTO> getPendingPatients() {
        List<User> patients = userRepository.findByRoleAndStatusOrderByCreatedAtAsc("patient", "pending");
        List<PendingPatientDTO> result = new ArrayList<>();
        for (User u : patients) {
            PatientProfile pp = patientProfileRepository.findByUserId(u.getId()).orElse(null);
            result.add(PendingPatientDTO.builder()
                    .id(u.getId())
                    .name(u.getName() != null ? u.getName() : u.getUsername())
                    .gender(u.getGender())
                    .status(u.getStatus())
                    .age(pp != null ? pp.getAge() : null)
                    .diagnosis(pp != null ? pp.getDiagnosis() : null)
                    .hospital(pp != null ? pp.getHospital() : null)
                    .doctorName(pp != null ? pp.getDoctorName() : null)
                    .doctorId(pp != null ? pp.getDoctorId() : null)
                    .createdAt(u.getCreatedAt())
                    .build());
        }
        return result;
    }

    @Transactional
    public void approvePatient(Long patientId, Long doctorId, String doctorName) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "患者不存在"));
        patient.setStatus("approved");
        userRepository.save(patient);

        PatientProfile pp = patientProfileRepository.findByUserId(patientId)
                .orElse(PatientProfile.builder().userId(patientId).build());
        pp.setDoctorId(doctorId);
        pp.setDoctorName(doctorName != null ? doctorName : "");
        patientProfileRepository.save(pp);

        boolean hasTasks = !taskRepository.findByPatientIdOrderByCreatedAtDesc(patientId).isEmpty();
        if (!hasTasks) {
            List<Task> defaults = new ArrayList<>();
            defaults.add(Task.builder()
                    .id(Snowflake.nextId())
                    .patientId(patientId)
                    .name("直腿抬高")
                    .count(10)
                    .unit("次")
                    .description("保持膝关节伸直\n仰卧位，腿抬高45°保持5秒")
                    .status("pending")
                    .source("doctor")
                    .build());
            defaults.add(Task.builder()
                    .id(Snowflake.nextId())
                    .patientId(patientId)
                    .name("靠墙静蹲")
                    .count(30)
                    .unit("秒")
                    .description("膝关节不超过脚尖\n背靠墙，屈膝90°")
                    .status("pending")
                    .source("doctor")
                    .build());
            taskRepository.saveAll(defaults);
        }
    }

    @Transactional
    public void rejectPatient(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "患者不存在"));
        patient.setStatus("rejected");
        userRepository.save(patient);
    }

    @Transactional
    public User registerWithRole(PatientRegisterRequest req) {
        if (req.getUsername() == null || req.getPassword() == null || req.getName() == null) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "请填写必填项");
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new ApiException(org.springframework.http.HttpStatus.CONFLICT, "用户名已存在，请更换");
        }
        String role = "patient".equals(req.getRole()) ? "patient" : "doctor";
        Long newId = Snowflake.nextId();
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        User user = User.builder()
                .id(newId)
                .username(req.getUsername())
                .passwordHash(hashedPassword)
                .role(role)
                .name(req.getName())
                .gender(req.getGender() != null ? req.getGender() : "男")
                .status("doctor".equals(role) ? "active" : "pending")
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        if ("patient".equals(role)) {
            String doctorName = "";
            if (req.getDoctorId() != null) {
                User doctor = userRepository.findById(req.getDoctorId()).orElse(null);
                doctorName = doctor != null && doctor.getName() != null ? doctor.getName()
                        : (req.getDoctorFreeText() != null ? req.getDoctorFreeText() : "");
            } else if (req.getDoctorFreeText() != null) {
                doctorName = req.getDoctorFreeText();
            }
            PatientProfile pp = PatientProfile.builder()
                    .userId(newId)
                    .age(req.getAge())
                    .diagnosis(req.getDiagnosis() != null ? req.getDiagnosis() : "")
                    .hospital(req.getHospital() != null ? req.getHospital() : "")
                    .doctorId(req.getDoctorId())
                    .doctorName(doctorName)
                    .build();
            patientProfileRepository.save(pp);
        } else {
            DoctorProfile dp = DoctorProfile.builder()
                    .userId(newId)
                    .hospital(req.getHospital() != null ? req.getHospital() : "")
                    .department(req.getDepartment() != null ? req.getDepartment() : "")
                    .title(req.getTitle() != null ? req.getTitle() : "")
                    .speciality(req.getSpeciality() != null ? req.getSpeciality() : "")
                    .build();
            doctorProfileRepository.save(dp);
        }
        return user;
    }
}
