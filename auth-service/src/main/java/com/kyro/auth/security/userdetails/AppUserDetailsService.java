package com.kyro.auth.security.userdetails;

import com.kyro.auth.User;
import com.kyro.auth.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public AppUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException, DisabledException {
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }

    if (user.isBanned()) {
      throw new DisabledException("your account has been banned");
    }

    if (!user.isActive()) {
      throw new DisabledException("Account is not activated for email: " + email);
    }

    return AppUserDetails.buildUserDetails(user);
  }
}
