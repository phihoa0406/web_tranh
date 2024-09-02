package com.example.web_tranh.service.User;

import com.example.web_tranh.dao.RoleRepository;
import com.example.web_tranh.dao.UserRepository;
import com.example.web_tranh.entity.Notification;
import com.example.web_tranh.entity.Role;
import com.example.web_tranh.entity.User;
import com.example.web_tranh.service.Base64ToMultipartFileConverter;
import com.example.web_tranh.service.Email.EmailService;
import com.example.web_tranh.service.UploadImage.UploadImageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.sql.Date;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImp implements UserService{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UploadImageService uploadImageService;
    private final ObjectMapper objectMapper;

    public UserServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<?> register(User user)
    {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(new Notification("Username đã tồn tại."));
        }

        // Kiểm tra email
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(new Notification("Email đã tồn tại."));
        }

        // Mã hoá mật khẩu
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);

        user.setAvatar("");

        // Tạo mã kích hoạt cho người dùng
        user.setActivationCode(generateActivationCode());
        user.setEnabled(false);

        // Cho role mặc định
        List<Role> roleList = new ArrayList<>();
        roleList.add(roleRepository.findByNameRole("CUSTOMER"));
        user.setListRoles(roleList);

        // Lưu vào database
        userRepository.save(user);

        // Gửi email cho người dùng để kích hoạt
        sendEmailActivation(user.getEmail(),user.getActivationCode());

        return ResponseEntity.ok("Đăng ký thành công!");

    }

    @Override
    public ResponseEntity<?> save(JsonNode userJson, String option)
    {
        try {
            User user = objectMapper.treeToValue(userJson, User.class);

            // Kiểm tra username đã tồn tại chưa
            if (!option.equals("update")) {
                // kiem tra username
                if (userRepository.existsByUsername(user.getUsername())) {
                    return ResponseEntity.badRequest().body(new Notification("Username đã tồn tại."));
                }

                // Kiểm tra email
                if (userRepository.existsByEmail(user.getEmail())) {
                    return ResponseEntity.badRequest().body(new Notification("Email đã tồn tại."));
                }
            }

            // Set ngày sinh cho user
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Instant instant = Instant.from(formatter.parse(formatStringByJson(String.valueOf(userJson.get("dateOfBirth")))) );
            java.sql.Date dateOfBirth = new java.sql.Date(Date.from(instant).getTime());
            user.setDateOfBirth(dateOfBirth);

            // Set role cho user
            int idRoleRequest = Integer.parseInt(String.valueOf(userJson.get("role")));
            Optional<Role> role = roleRepository.findById(idRoleRequest);
            List<Role> roles = new ArrayList<>();
            roles.add(role.get());
            user.setListRoles(roles);


            // Mã hoá mật khẩu
            if (!(user.getPassword() == null)) { // Trường hợp là thêm hoặc thay đổi password
                String encodePassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(encodePassword);
            } else {
                // Trường hợp cho update không thay đổi password
                Optional<User> userTemp = userRepository.findById(user.getIdUser());
                user.setPassword(userTemp.get().getPassword());
            }

            // Set avatar
            String avatar = (formatStringByJson(String.valueOf((userJson.get("avatar")))));
            if (avatar.length() > 500) {
                MultipartFile avatarFile = Base64ToMultipartFileConverter.convert(avatar);
                String avatarURL = uploadImageService.uploadImage(avatarFile, "User_" + user.getIdUser());
                user.setAvatar(avatarURL);
            }

            userRepository.save(user);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("thành công");
    }


    @Override
    public ResponseEntity<?> delete(int id)
    {
        try {
            Optional<User> user = userRepository.findById(id);

            if (user.isPresent()) {
                String imageUrl = user.get().getAvatar();

                if (imageUrl != null) {
                    uploadImageService.deleteImage(imageUrl);
                }

                userRepository.deleteById(id);
            }
        }
            catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok("thành công");

    }

    @Override
    public ResponseEntity<?> changePassword(JsonNode userJson)
    {
        try {
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(userJson.get("idUser"))));
            String newPassword = formatStringByJson(String.valueOf(userJson.get("newPassword")));
            Optional<User> user = userRepository.findById(idUser);
            user.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user.get());
        }
        catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }


    @Override
    @Transactional
    public ResponseEntity<?> changeAvatar(JsonNode userJson) {
        try {
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(userJson.get("idUser"))));
            String dataAvatar = formatStringByJson(String.valueOf(userJson.get("avatar")));

            Optional<User> userOpt = userRepository.findById(idUser);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User không tồn tại");
            }

            User user = userOpt.get();
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                uploadImageService.deleteImage(user.getAvatar());
            }

            if (Base64ToMultipartFileConverter.isBase64(dataAvatar)) {
                MultipartFile avatarFile = Base64ToMultipartFileConverter.convert(dataAvatar);
                String avatarUrl = uploadImageService.uploadImage(avatarFile, "User_" + idUser);
                user.setAvatar(avatarUrl);
            }

            User savedUser = userRepository.save(user);
            ///String jwtToken = jwtService.generateToken(savedUser.getUsername());
          ///  return ResponseEntity.ok(new JwtResponse(jwtToken));
            return ResponseEntity.ok("Cập nhật ảnh đại diện thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật ảnh đại diện");
        }
    }

    @Override
    public ResponseEntity<?> updateProfile(JsonNode userJson) {
        try {
            // Convert JsonNode thành một đối tượng chứa thông tin cần cập nhật
            User userRequest = objectMapper.treeToValue(userJson, User.class);

            Optional<User> userOpt = userRepository.findById(userRequest.getIdUser());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy người dùng");
            }

            User user = userOpt.get();
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setPhoneNumber(userRequest.getPhoneNumber());
            user.setDeliveryAddress(userRequest.getDeliveryAddress());
            user.setGender(userRequest.getGender());

            // Xử lý ngày sinh
            String rawDob = formatStringByJson(String.valueOf(userJson.get("dateOfBirth")));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Instant instant = Instant.from(formatter.parse(rawDob));
            java.sql.Date dateOfBirth = new java.sql.Date(Date.from(instant).getTime());
            user.setDateOfBirth(dateOfBirth);

            userRepository.save(user);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật hồ sơ");
        }
    }
    @Override
    @Transactional
    public ResponseEntity<?> forgotPassword(JsonNode jsonNode) {
        try {
            User user = userRepository.findByEmail(formatStringByJson(jsonNode.get("email").toString()));

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Đổi mật khẩu
            String passwordTemp = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(passwordTemp));
            userRepository.save(user);

            // Gửi email
            sendEmailForgotPassword(user.getEmail(), passwordTemp);

        } catch (Exception e) {
            e.printStackTrace();
            // Khi có @Transactional thì nếu sendEmail bị lỗi, mọi thay đổi sẽ rollback
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
    private void sendEmailForgotPassword(String email, String password) {
        String subject = "Reset mật khẩu";
        String message = "Mật khẩu tạm thời của bạn là: <strong>" + password + "</strong>";
        message += "<br/> <span>Vui lòng đăng nhập và đổi lại mật khẩu của bạn</span>";
        try {
            emailService.sendMessage("dophi04062002@gmail.com", email, subject, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateTemporaryPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }


    public ResponseEntity<?> activeAccount(String email, String activationCode) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(new Notification("Người dùng không tồn tại!"));
        }
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(new Notification("Tài khoản đã được kích hoạt"));
        }
        if (user.getActivationCode().equals(activationCode)) {
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            return ResponseEntity.badRequest().body(new Notification("Mã kích hoạt không chính xác!"));
        }
        return ResponseEntity.ok("Kích hoạt thành công");
    }


    @Override
    public User getUserById(int id) {
        return userRepository.findUserById(id);
    }

    private String generateActivationCode() {
        return UUID.randomUUID().toString();
    }

    private void sendEmailActivation(String email, String activationCode) {
        String endpointFE = "http://localhost:3000";
        String url = endpointFE + "/active/" + email + "/" + activationCode;
        String subject = "Kích hoạt tài khoản";
        String message = "Cảm ơn bạn đã là thành viên của chúng tôi. Vui lòng kích hoạt tài khoản!: <br/> Mã kích hoạt: <strong>"+ activationCode +"</strong>";
        message += "<br/> Click vào đây để <a href="+ url +">kích hoạt</a>";
        try {
            emailService.sendMessage("dophi04062002@gmail.com", email, subject, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}
