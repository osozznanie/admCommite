package com.osozznanie.service.impl;

import com.osozznanie.domain.Exam;
import com.osozznanie.domain.Major;
import com.osozznanie.domain.User;
import com.osozznanie.entity.ExamEntity;
import com.osozznanie.entity.MajorEntity;
import com.osozznanie.entity.StudentMajorEntity;
import com.osozznanie.entity.StudentMarkEntity;
import com.osozznanie.entity.UserEntity;
import com.osozznanie.exception.UserNotFoundException;
import com.osozznanie.exception.ValidationException;
import com.osozznanie.mapper.UserMapper;
import com.osozznanie.repository.UserRepository;
import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
	private static final User USER = buildUser();
	private static final User USER_UPDATE = buildUserUpdate();
	private static final UserEntity USER_ENTITY = buildUserEntity();

	private static final String EMAIL = "test@email.net";
	private static final String OTHER_EMAIL = "other@email.com";
	private static final int ID = 1;
	private static final String PASS = "pass";
	private static final String ENCRYPTED_PASS = "encrypted_pass";
	private static final String OTHER_PASS = "other_pass";
	private static final String OTHER_PASS_ENCRYPTED = "other_pass_encrypted";

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private UserMapper userMapper;
	@InjectMocks
	private UserServiceImpl userService;

	@After
	public void resetMocks() {
		reset(userRepository, passwordEncoder, userMapper);
	}

	@Test
	void loadUserByUsernameSuccess() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(USER_ENTITY));
		when(userMapper.mapEntityToDomain(USER_ENTITY)).thenReturn(USER);

		User user = (User) userService.loadUserByUsername(EMAIL);

		assertThat(user, is(USER));
		verify(userRepository).findByEmail(EMAIL);
		verify(userMapper).mapEntityToDomain(USER_ENTITY);
		verifyNoMoreInteractions(userRepository);
		verifyNoMoreInteractions(userMapper);
	}

	@Test
	void loadUserByUsernameExceptionNotFound() {
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(OTHER_EMAIL));

		verify(userRepository).findByEmail(anyString());
		verifyNoMoreInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void registerSuccess() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(PASS)).thenReturn(ENCRYPTED_PASS);
		when(userRepository.save(any(UserEntity.class))).thenReturn(USER_ENTITY);
		when(userMapper.mapEntityToDomain(USER_ENTITY)).thenReturn(USER);

		User user = userService.register(USER);
		assertThat(user, is(USER));

		verify(userRepository, times(1)).findByEmail(EMAIL);
		verify(passwordEncoder, times(1)).encode(PASS);
		verify(userRepository, times(1)).save(any(UserEntity.class));
		verify(userMapper, times(1)).mapEntityToDomain(USER_ENTITY);
		verifyNoMoreInteractions(userRepository);
		verifyNoMoreInteractions(passwordEncoder);
		verifyNoMoreInteractions(userMapper);
	}

	@Test
	void registerExceptionPasswordsDontMatch() {
		USER.setPassword2(OTHER_PASS);
		Exception exception = assertThrows(ValidationException.class, () -> userService.register(USER));
		assertEquals("Passwords don't match", exception.getMessage());
	}

	@Test
	void registerExceptionUserExists() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(USER_ENTITY));
		Exception exception = assertThrows(ValidationException.class, () -> userService.register(USER));
		assertEquals("User with this email was registered already", exception.getMessage());
	}

	@Test
	void findByIdSuccess() {
		when(userRepository.findById(ID)).thenReturn(Optional.of(USER_ENTITY));
		when(userMapper.mapEntityToDomainWithLists(USER_ENTITY)).thenReturn(USER);

		User user = userService.findById(ID);

		assertThat(user, is(USER));
		verify(userRepository).findById(ID);
		verify(userMapper).mapEntityToDomainWithLists(USER_ENTITY);
		verifyNoMoreInteractions(userRepository);
		verifyNoMoreInteractions(userMapper);
	}

	@Test
	void findByIdExceptionNotFound() {
		when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.findById(anyInt()));

		verify(userRepository).findById(anyInt());
		verifyNoMoreInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void findAll() {
		final int pageIndex = 0;
		final int pageSize = 10;
		final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
		final List<UserEntity> userEntities = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			userEntities.add(USER_ENTITY);
		}
		final Page<UserEntity> userEntityPage = new PageImpl<>(userEntities);

		when(userRepository.count()).thenReturn(userEntityPage.getTotalElements());
		when(userRepository.findAll(pageRequest)).thenReturn(userEntityPage);
		when(userMapper.mapEntityToDomain(USER_ENTITY)).thenReturn(USER);

		final Page<User> userPage = userService.findAll(pageIndex, pageSize);

		assertEquals(9, userPage.getTotalElements());
		assertEquals(9, userPage.getNumberOfElements());
		assertEquals(1, userPage.getTotalPages());
		verify(userRepository, times(1)).count();
		verify(userRepository, times(1)).findAll(pageRequest);
		verify(userMapper, times(9)).mapEntityToDomain(USER_ENTITY);
		verifyNoMoreInteractions(userRepository);
		verifyNoMoreInteractions(userMapper);
	}

	@Test
	void count() {
		when(userRepository.count()).thenReturn(1L);
		userService.count();
		verify(userRepository, times(1)).count();
	}

	@Test
	void updateProfileSuccess() {
		when(userRepository.findByEmail(USER_UPDATE.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(OTHER_PASS)).thenReturn(OTHER_PASS_ENCRYPTED);
		when(userMapper.mapDomainToEntity(USER_UPDATE)).thenReturn(USER_ENTITY);
		when(userRepository.save(USER_ENTITY)).thenReturn(USER_ENTITY);

		User userReturned = userService.updateProfile(USER, USER_UPDATE);

		assertThat(userReturned, is(USER_UPDATE));
		verify(userRepository, times(1)).findByEmail(USER_UPDATE.getEmail());
		verify(passwordEncoder, times(1)).encode(OTHER_PASS);
		verify(userMapper, times(1)).mapDomainToEntity(USER_UPDATE);
		verify(userRepository, times(1)).save(USER_ENTITY);
		verifyNoMoreInteractions(userRepository);
		verifyNoMoreInteractions(passwordEncoder);
		verifyNoMoreInteractions(userMapper);
	}

	@Test
	void updateProfileExceptionEmailExists() {
		USER_ENTITY.setEmail(OTHER_EMAIL);
		when(userRepository.findByEmail(USER_UPDATE.getEmail())).thenReturn(Optional.of(USER_ENTITY));

		Exception exception = assertThrows(ValidationException.class, () -> userService.updateProfile(USER, USER_UPDATE));

		assertEquals("User with this email was registered already", exception.getMessage());
		verify(userRepository, times(1)).findByEmail(USER_UPDATE.getEmail());
		verifyNoInteractions(passwordEncoder);
		verifyNoInteractions(userMapper);
	}

	@Test
	void updateProfileExceptionPasswordsDontMatch() {
		USER_UPDATE.setPassword2("other pass is wrong");
		when(userRepository.findByEmail(USER_UPDATE.getEmail())).thenReturn(Optional.empty());

		Exception exception = assertThrows(ValidationException.class, () -> userService.updateProfile(USER, USER_UPDATE));

		assertEquals("Passwords don't match", exception.getMessage());
		verify(userRepository, times(1)).findByEmail(USER_UPDATE.getEmail());
		verifyNoInteractions(passwordEncoder);
		verifyNoInteractions(userMapper);
	}

	@Test
	void saveStudentWithLists() {
		when(userRepository.save(USER_ENTITY)).thenReturn(USER_ENTITY);
		when(userMapper.mapDomainToEntityWithLists(USER)).thenReturn(USER_ENTITY);
		when(userMapper.mapEntityToDomain(USER_ENTITY)).thenReturn(USER);

		User user = userService.saveStudentWithLists(USER);
		assertEquals(USER, user);
		verify(userRepository, times(1)).save(USER_ENTITY);
		verify(userMapper, times(1)).mapDomainToEntityWithLists(USER);
		verify(userMapper, times(1)).mapEntityToDomain(USER_ENTITY);
		verifyNoMoreInteractions(userRepository);
		verifyNoMoreInteractions(userMapper);
		verifyNoInteractions(passwordEncoder);
	}

	private static User buildUser() {
		User user = new User();

		List<Exam> exams = new ArrayList<>();
		exams.add(Exam.builder()
				.id(21)
				.subject("ua_lang_lit")
				.location("Campus_A")
				.date(new Date(10000000))
				.isRegistered(true)
				.mark(150)
				.build());
		exams.add(Exam.builder()
				.id(22)
				.subject("ua_history")
				.location("Campus_A")
				.date(new Date(10000000))
				.isRegistered(true)
				.mark(150)
				.build());
		exams.add(Exam.builder()
				.id(23)
				.subject("math")
				.location("Campus_A")
				.date(new Date(10000000))
				.isRegistered(true)
				.mark(150)
				.build());

		List<Major> majors = new ArrayList<>();
		majors.add(Major.builder()
				.id(11)
				.title("Computer Science")
				.exams(exams)
				.capacity(10)
				.applicants(new ArrayList<>(Collections.singletonList(user)))
				.applicantsNum(1)
				.isApplied(true)
				.build());

		user.setId(ID);
		user.setEmail(EMAIL);
		user.setPassword(PASS);
		user.setPassword2(PASS);
		user.setFirstName("Ivanko");
		user.setLastName("Trololo");
		user.setRole(com.osozznanie.domain.Role.STUDENT);
		user.setExams(exams);
		user.setMajors(majors);
		user.setMajorScore(450);
		return user;
	}

	private static User buildUserUpdate() {
		return User.builder()
				.id(USER.getId())
				.email(OTHER_EMAIL)
				.password(OTHER_PASS)
				.password2(OTHER_PASS)
				.firstName("NewName")
				.lastName(USER.getLastName())
				.role(USER.getRole())
				.majors(USER.getMajors())
				.exams(USER.getExams())
				.majorScore(USER.getMajorScore())
				.build();
	}

	private static UserEntity buildUserEntity() {
		UserEntity userEntity = new UserEntity();
		List<StudentMarkEntity> studentMarks = new ArrayList<>();

		ExamEntity examEntity1 = ExamEntity.builder()
				.id(21)
				.subject("ua_lang_lit")
				.location("Campus_A")
				.date(new Date(10000000))
				.build();
		ExamEntity examEntity2 = ExamEntity.builder()
				.id(22)
				.subject("ua_history")
				.location("Campus_A")
				.date(new Date(10000000))
				.build();
		ExamEntity examEntity3 = ExamEntity.builder()
				.id(23)
				.subject("math")
				.location("Campus_A")
				.date(new Date(10000000))
				.build();
		List<ExamEntity> exams = Arrays.asList(examEntity1, examEntity2, examEntity3);

		StudentMarkEntity studMark = new StudentMarkEntity();
		studMark.setExam(examEntity1);
		studMark.setStudent(userEntity);
		studentMarks.add(studMark);

		StudentMarkEntity studMark2 = new StudentMarkEntity();
		studMark2.setExam(examEntity2);
		studMark2.setStudent(userEntity);
		studentMarks.add(studMark2);

		StudentMarkEntity studMark3 = new StudentMarkEntity();
		studMark3.setExam(examEntity3);
		studMark3.setStudent(userEntity);
		studentMarks.add(studMark3);

		StudentMajorEntity studMaj = new StudentMajorEntity();
		studMaj.setMajor(MajorEntity.builder()
				.id(11)
				.title("Computer Science")
				.examEntities(exams)
				.capacity(10)
				.applicantsNum(1)
				.build());
		studMaj.setStudent(userEntity);
		List<StudentMajorEntity> majors = new ArrayList<>();
		majors.add(studMaj);

		userEntity.setId(ID);
		userEntity.setEmail(EMAIL);
		userEntity.setPassword(ENCRYPTED_PASS);
		userEntity.setFirstName("Ivanko");
		userEntity.setLastName("Trololo");
		userEntity.setRole(com.osozznanie.entity.Role.STUDENT);
		userEntity.setStudMajors(majors);
		userEntity.setMarks(studentMarks);
		return userEntity;
	}
}