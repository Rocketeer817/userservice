package dev.rushee.userservicetestfinal.services;

import dev.rushee.userservicetestfinal.repositories.SessionRepository;
import dev.rushee.userservicetestfinal.dtos.UserDto;
import dev.rushee.userservicetestfinal.models.Session;
import dev.rushee.userservicetestfinal.models.SessionStatus;
import dev.rushee.userservicetestfinal.models.User;
import dev.rushee.userservicetestfinal.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;

    private BCryptPasswordEncoder _bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        _bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        //validate the password
        if(!_bCryptPasswordEncoder.matches(password, user.getPassword())){
            return null;
        }
//        if (!user.getPassword().equals(password)) {
//            return null;
//        }

        //Generate a token
        String token = RandomStringUtils.randomAlphanumeric(30);

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        sessionRepository.save(session);

        UserDto userDto = new UserDto();

//        Map<String, String> headers = new HashMap<>();
//        headers.put(HttpHeaders.SET_COOKIE, token);

        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + token);



        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);
//        response.getHeaders().add(HttpHeaders.SET_COOKIE, token);

        return response;
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signUp(String email, String password) {
        //encrypt the password
        String encryptedPassword = _bCryptPasswordEncoder.encode(password);
        User user = new User();
        user.setEmail(email);
        user.setPassword(encryptedPassword);
        
        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        if(sessionOptional.get().getSessionStatus() == SessionStatus.ENDED){
            return SessionStatus.ENDED;
        }

        return SessionStatus.ACTIVE;
    }

}
