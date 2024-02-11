package dev.rushee.userservicetestfinal.services;

import dev.rushee.userservicetestfinal.dtos.ValidateTokenResponseDto;
import dev.rushee.userservicetestfinal.repositories.SessionRepository;
import dev.rushee.userservicetestfinal.dtos.UserDto;
import dev.rushee.userservicetestfinal.models.Session;
import dev.rushee.userservicetestfinal.models.SessionStatus;
import dev.rushee.userservicetestfinal.models.User;
import dev.rushee.userservicetestfinal.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;

    private BCryptPasswordEncoder _bCryptPasswordEncoder;

    private SecretKey _secretKey;

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
        //String token = RandomStringUtils.randomAlphanumeric(30);

        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        //c.add(Calendar.DATE, 1);
        c.add(Calendar.MINUTE,1);
        dt = c.getTime();

        Map<String, Object> jsonForJwt = new HashMap<>();
        jsonForJwt.put("userId", user.getId());
        jsonForJwt.put("email", user.getEmail());
        jsonForJwt.put("roles", user.getRoles());
        jsonForJwt.put("expirationDate", dt);
        //if(xx =!null)
        jsonForJwt.put("createdAt" , new Date());

        MacAlgorithm alg = Jwts.SIG.HS256;
        SecretKey key = getSecretKey();

        String token = Jwts.builder().claims(jsonForJwt).signWith(key, alg).compact();

        //Create a session
        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        sessionRepository.save(session);

        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());

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

    public ValidateTokenResponseDto validate(String token, Long userId)
    {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session sessionObj = sessionOptional.get();

        if(sessionObj.getSessionStatus() == SessionStatus.ENDED){
            return null;
        }

        SecretKey key = getSecretKey();

        Claims claims =
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        Date expirationDate = new Date((long)claims.get("expirationDate"));

        if(expirationDate.before(new Date())){
            sessionObj.setSessionStatus(SessionStatus.ENDED);
            sessionRepository.save(sessionObj);
            return null;
        }

        return ValidateTokenResponseDto.from(claims);
    }

    private SecretKey getSecretKey(){

        synchronized (this.getClass()){
            if(_secretKey == null){
                synchronized (this.getClass()){
                    if(_secretKey == null){
                        MacAlgorithm alg = Jwts.SIG.HS256;
                        _secretKey = alg.key().build();
                        return _secretKey;
                    }
                }
            }
            return _secretKey;
        }

    }

}
