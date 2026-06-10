package com.badminton.mapper;

import com.badminton.dto.response.BookingResponse;
import com.badminton.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "courtId", source = "court.id")
    @Mapping(target = "courtName", source = "court.name")
    @Mapping(target = "timeSlotId", source = "timeSlot.id")
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "endTime", source = "timeSlot.endTime")
    @Mapping(target = "amount", source = "payment.amount")
    BookingResponse toResponse(Booking booking);
}
