package jpabook.jpashop;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Parent {

    @Id @GeneratedValue
    private Long id;

    private String name;
}
