package com.osozznanie.service.impl;

import com.osozznanie.domain.PaginationUtility;
import com.osozznanie.domain.User;
import com.osozznanie.entity.Role;
import com.osozznanie.entity.UserEntity;
import com.osozznanie.exception.UserNotFoundException;
import com.osozznanie.exception.ValidationException;
import com.osozznanie.mapper.UserMapper;
import com.osozznanie.repository.UserRepository;
import com.osozznanie.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UserServiceImpl implements UserService {
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private UserMapper userMapper;

	@Override
	public UserDetails loadUserByUsername(String username) {
		final Optional<UserEntity> userEntity = userRepository.findByEmail(username);
		return userEntity
				.map(userMapper::mapEntityToDomain)
				.orElseThrow(() -> new UsernameNotFoundException("User with email '" + username + "' is not found."));
	}

	@Transactional
	@Override
	public User register(User user) {
		if (!user.getPassword().equals(user.getPassword2())) {
			throw new ValidationException("Passwords don't match");
		}
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			throw new ValidationException("User with this email was registered already");
		}
		final String encryptedPass = passwordEncoder.encode(user.getPassword());

		UserEntity newUserEntity = UserEntity.builder()
				.email(user.getEmail())
				.password(encryptedPass)
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.role(Role.valueOf(user.getRole().name()))
				.build();

		UserEntity userEntity = userRepository.save(newUserEntity);
		log.info(String.format("User '%s' (ID %d) was registered", userEntity.getEmail(), userEntity.getId()));
		return userMapper.mapEntityToDomain(userEntity);
	}

	@Override
	public User findById(Integer id) {
		return userRepository.findById(id)
				.map(userMapper::mapEntityToDomainWithLists)
				.orElseThrow(() -> new UserNotFoundException("User with id [" + id + "] was not found"));
	}

	@Override
	public Page<User> findAll(int pageIndex, int pageSize) {
		pageIndex = PaginationUtility.limitPageIndex(userRepository.count(), pageIndex, pageSize);
		return userRepository.findAll(PageRequest.of(pageIndex, pageSize))
				.map(userMapper::mapEntityToDomain);
	}

	@Override
	public Long count() {
		return userRepository.count();
	}

	@Override
	@Transactional
	public User updateProfile(User currentUser, User userUpdate) {
		if (!userUpdate.getEmail().equals(currentUser.getEmail()) &&
				userRepository.findByEmail(userUpdate.getEmail()).isPresent()) {
			throw new ValidationException("User with this email was registered already");
		}

		String encryptedPass = currentUser.getPassword();
		if (!userUpdate.getPassword().isEmpty() && !userUpdate.getPassword2().isEmpty()) {
			if (!userUpdate.getPassword().equals(userUpdate.getPassword2())) {
				throw new ValidationException("Passwords don't match");
			}
			encryptedPass = passwordEncoder.encode(userUpdate.getPassword());
		}
		userUpdate.setPassword(encryptedPass);

		userUpdate.setId(currentUser.getId());
		userUpdate.setRole(currentUser.getRole());

		userRepository.save(userMapper.mapDomainToEntity(userUpdate));
		log.trace(String.format("User '%s' (ID %d) updated profile", userUpdate.getEmail(), userUpdate.getId()));
		return userUpdate;
	}

	@Override
	@Transactional
	public User saveStudentWithLists(User student) {
		UserEntity userEntity = userRepository.save(userMapper.mapDomainToEntityWithLists(student));
		log.debug(String.format("User '%s' (ID %d) was saved with lists", userEntity.getEmail(), userEntity.getId()));
		return userMapper.mapEntityToDomain(userEntity);
	}
}
