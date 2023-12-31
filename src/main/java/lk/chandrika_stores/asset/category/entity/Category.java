package lk.chandrika_stores.asset.category.entity;

import com.fasterxml.jackson.annotation.JsonFilter;
import lk.chandrika_stores.asset.brand.entity.Brand;
import lk.chandrika_stores.asset.common_asset.model.enums.LiveDead;
import lk.chandrika_stores.asset.item.entity.Item;
import lk.chandrika_stores.asset.item.entity.enums.MainCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonFilter( "Category" )
public class Category {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Integer id;

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;

    @Enumerated(EnumType.STRING)
    private LiveDead liveDead;

    @Size( min = 3, message = "Your name cannot be accepted" )
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Brand> brands;

    @OneToMany(mappedBy = "category")
    private List<Item> items;
}
