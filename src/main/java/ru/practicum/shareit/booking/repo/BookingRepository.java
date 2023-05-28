package ru.practicum.shareit.booking.repo;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findByBookerId(Long bookerId, Sort sort);

    Collection<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Sort sort);

    Collection<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    Collection<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    Collection<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                                    LocalDateTime end, Sort sort);

    Collection<Booking> findByBookerIdAndItemIdAndEndIsBeforeAndStatus(Long bookerId, Long itemId, LocalDateTime now,
                                                                       BookingStatus bookingStatus);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner " +
            "ORDER BY b.start DESC")
    Collection<Booking> findByOwnerId(@Param("owner") User owner);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.status = :status ORDER BY b.start DESC")
    Collection<Booking> findByOwnerIdAndStatus(@Param("owner") User owner, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Collection<Booking> findByOwnerIdInFuture(@Param("owner") User owner);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Collection<Booking> findByOwnerIdInPast(@Param("owner") User owner);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.owner = :owner AND " +
            "b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Collection<Booking> findByOwnerIdInCurrent(@Param("owner") User owner);

    @Query(value = "SELECT * FROM Booking WHERE item_id = :itemId AND start_date < CURRENT_TIMESTAMP AND " +
            "status = 'APPROVED' ORDER BY start_date DESC LIMIT 1", nativeQuery = true)
    Optional<Booking> findLastBookingByItemId(@Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM Booking WHERE item_id = :itemId AND start_date > CURRENT_TIMESTAMP AND " +
            "status = 'APPROVED' ORDER BY start_date LIMIT 1", nativeQuery = true)
    Optional<Booking> findNextBookingByItemId(@Param("itemId") Long itemId);
}
