package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.response.ApiResponse;
import it4341.HeTroGiup.dto.request.LoginRequest;
import it4341.HeTroGiup.dto.request.RegisterRequest;
import it4341.HeTroGiup.entity.Landlord;
import it4341.HeTroGiup.repository.LandlordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final LandlordRepository landlordRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse register(RegisterRequest request) {
        if (landlordRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse("exception", "Email đã được sử dụng", null);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Landlord landlord = Landlord.builder()
                .email(request.getEmail()) // Lưu thẳng vào cột email
                .passwordHash(encodedPassword)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .isDeleted(false)
                .build();

        landlordRepository.save(landlord);

        return new ApiResponse("00", null, "Đăng ký thành công");
    }

    public ApiResponse login(LoginRequest request) {
        var userOptional = landlordRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return new ApiResponse("exception", "Tài khoản (email) không tồn tại", null);
        }

        Landlord user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new ApiResponse("exception", "Sai mật khẩu", null);
        }

        return new ApiResponse("00", null, "successful");
    }
}