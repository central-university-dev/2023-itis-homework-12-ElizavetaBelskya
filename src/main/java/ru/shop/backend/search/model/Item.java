package ru.shop.backend.search.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
public class Item {
    @Column
    private Integer price;
    @Column
    private String name;
    @Column
    private String url;
    @Column
    private String image;
    @Id
    @Column
    private Integer itemId;

    private String cat;

    public Item(Object[] arr) {
        this.price = ((BigInteger) arr[2]).intValue();
        this.name = arr[1].toString();
        this.url = arr[3].toString();
        this.image = arr[4].toString();
        this.itemId = ((BigInteger) arr[0]).intValue();
        this.cat = arr[5].toString();
    }
}
