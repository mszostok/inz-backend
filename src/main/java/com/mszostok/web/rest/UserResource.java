package com.mszostok.web.rest;

import com.mszostok.domain.User;
import com.mszostok.service.UserService;
import com.mszostok.utils.SecurityUtils;
import com.mszostok.web.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
public class UserResource {

  @Autowired
  private UserService userService;


  @RequestMapping(value = "/profile", method = PUT, produces = {APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE})
  public ResponseEntity<?> updateProfile(@Valid @RequestBody final UserDto userDto) {

    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);


    return SecurityUtils.getCurrentUserLogin().map(email -> userService.getActiveUserByEmail(email)
      .map(user -> {
        if (userService.isUsernameInUse(user, userDto)) {
          return new ResponseEntity<>(Collections.singletonMap("message", "Username already in use"), jsonHeaders, HttpStatus.BAD_REQUEST);
        }
        if (userService.isEmailInUse(user, userDto)) {
          return new ResponseEntity<>(Collections.singletonMap("message", "E-mail already in use"), jsonHeaders, HttpStatus.BAD_REQUEST);
        }
        User updated = userService.updateProfile(userDto);
        return new ResponseEntity<>(new UserDto(updated), HttpStatus.OK);
      })
      .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR))
    ).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @RequestMapping(value = "/profile", method = GET, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDto> getProfile() {

    return SecurityUtils.getCurrentUserLogin().map(email -> userService.getActiveUserByEmail(email)
      .map(user -> new ResponseEntity<>(new UserDto(user), HttpStatus.OK))
      .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR))
    ).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
  }

}
