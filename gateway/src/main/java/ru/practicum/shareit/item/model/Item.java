//package ru.practicum.shareit.item.model;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import ru.practicum.shareit.user.model.User;
//
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;
//import javax.persistence.Transient;
//import java.util.ArrayList;
//import java.util.List;
//
//@Getter
//@Setter
//@Builder
//
//public class Item {
//    private Long id;
//    private String name;
//    private String description;
//    private Boolean available;
//
//    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
//    private User owner;
//
//    @Transient
//    private List<Comment> comments = new ArrayList<>();
//
//    @Transient
//    private Long requestId;
//}
