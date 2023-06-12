package ru.practicum.shareit.booking.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                              LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndItemIdAndEndIsBeforeAndStatusOrderById(Long bookerId, Long itemId, LocalDateTime now,
                                                                          BookingStatus bookingStatus);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner")
    List<Booking> findByOwnerId(@Param("owner") User owner, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND b.status = :status")
    List<Booking> findByOwnerIdAndStatus(@Param("owner") User owner, @Param("status") BookingStatus status,
                                         Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.start > CURRENT_TIMESTAMP")
    List<Booking> findByOwnerIdInFuture(@Param("owner") User owner, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.end < CURRENT_TIMESTAMP")
    List<Booking> findByOwnerIdInPast(@Param("owner") User owner, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP")
    List<Booking> findByOwnerIdInCurrent(@Param("owner") User owner, Pageable pageable);

    @Query(value = "SELECT * FROM Booking WHERE item_id = :itemId AND start_date < CURRENT_TIMESTAMP AND " +
            "status = 'APPROVED' ORDER BY start_date DESC LIMIT 1", nativeQuery = true)
    Optional<Booking> findLastBookingByItemId(@Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM Booking WHERE item_id = :itemId AND start_date > CURRENT_TIMESTAMP AND " +
            "status = 'APPROVED' ORDER BY start_date LIMIT 1", nativeQuery = true)
    Optional<Booking> findNextBookingByItemId(@Param("itemId") Long itemId);
}
