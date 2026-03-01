package org.example.care.service;

import org.example.care.dto.auth.LoginRequest;
import org.example.care.dto.auth.PatientRegistrationRequest;
import org.example.care.dto.auth.RegisterRequest;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Doctor;
import org.example.care.model.Patient;
import org.example.care.model.enumeration.Role;
import org.example.care.model.User;
import org.example.care.repository.DoctorRepository;
import org.example.care.repository.PatientRepository;
import org.example.care.repository.UserRepository;
import org.example.care.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class AuthService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;


    @Transactional
    public User register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = Doctor.builder()
                    .user(savedUser)
                    .name(request.getName())
                    .specialization(request.getSpecialization())
                    .licenseNumber(request.getLicenseNumber())
                    .hospitalName(request.getHospitalName())
                    .contactInfo(request.getContactInfo())
                    .build();
            doctorRepository.save(doctor);
        } else {
            Patient patient = Patient.builder()
                    .user(savedUser)
                    .name(request.getName())
                    .age(request.getAge())
                    .dateOfBirth(request.getDateOfBirth())
                    .address(request.getAddress())
                    .contactNumber(request.getContactNumber())
                    .gender(request.getGender())
                    .bloodGroup(request.getBloodGroup())
                    .build();
            patientRepository.save(patient);
        }
        return savedUser;
    }

    public AuthResult login(LoginRequest request) {
        // authenticationManager.authenticate() automatically throws
        // BadCredentialsException if the password or username is wrong.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Use your custom ResourceNotFoundException if the user doesn't exist
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String token = jwtService.generateToken(user);
        return new AuthResult(user, token);
    }

    @Transactional
    public void extendDoctorToPatient(Long userId, PatientRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (patientRepository.existsById(userId)) {
            throw new IllegalArgumentException("User already has a patient profile");
        }

        Patient patient = Patient.builder()
                .user(user)
                .name(user.getDoctor().getName())
                .age(request.getAge())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .build();

        patientRepository.save(patient);
    }

    /**
     * Extends a Patient's account to also have a Doctor profile.
     */
    @Transactional
    public void extendPatientToDoctor(Long userId, String specialization, String license, String hospital, String contact) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (doctorRepository.existsById(userId)) {
            throw new IllegalArgumentException("User already has a doctor profile");
        }

        Doctor doctor = Doctor.builder()
                .user(user)
                .name(user.getPatient().getName())
                .specialization(specialization)
                .licenseNumber(license)
                .hospitalName(hospital)
                .contactInfo(contact)
                .build();

        doctorRepository.save(doctor);
    }

    public record AuthResult(User user, String token) {
    }
}