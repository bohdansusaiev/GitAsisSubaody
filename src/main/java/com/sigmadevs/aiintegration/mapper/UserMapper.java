package com.sigmadevs.aiintegration.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface UserMapper {

//    UserGetDto userToUserGetDto(User user);
//
//    User userGetDtoToUser(UserGetDto userGetDto);
//
//    @Mapping(target = "displayName", source = "username")
//    @Mapping(target = "image", constant = "image")
//    @Mapping(target = "isEmailVerified", constant = "false")
//    @Mapping(target = "role", constant = "USER")
//    User userRegistrationDtoToUser(UserRegistrationDto userRegistrationDto);
//
////    @Mapping(target = "displayName", source = "username")
////    @Mapping(target = "image", constant = "image")
////    @Mapping(target = "isEmailVerified", constant = "false")
////    @Mapping(target = "role", constant = "USER")
//    UserDto userToUserDto(User user);

}
