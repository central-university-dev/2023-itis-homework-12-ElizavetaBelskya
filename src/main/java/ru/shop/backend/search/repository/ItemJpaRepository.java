package ru.shop.backend.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.shop.backend.search.model.ItemEntity;

import java.util.List;
import java.util.stream.Stream;

public interface ItemJpaRepository extends JpaRepository<ItemEntity, Long> {

    //TODO: так, допустим, что предыдущие поколения не знали какие сущности в базе

    //TODO: i as image? image видимо строка-ссылка
    @Query(value = "SELECT i.item_id, i.name, r.price, i.itemurl AS url, i.image AS image, i.type " +
            "FROM item i " +
            "JOIN remain r ON r.item_id = i.item_id AND r.region_id = :regionId " +
            "WHERE i.item_id IN (:ids)", nativeQuery = true)
    List<Object[]> findByIds(@Param("regionId") Integer regionId, @Param("ids") List<Long> ids);


    @Query(value = "SELECT item_id FROM item_sku WHERE sku = :sku", nativeQuery = true)
    List<Integer> findBySku(@Param("sku") String sku);


    @Query(value = "SELECT i.* FROM item i", nativeQuery = true)
    Stream<ItemEntity> findAllInStream();


    @Query(value = "SELECT DISTINCT c.name, cp.name AS parent_name, c.realcatname AS url, cp.realcatname AS parent_url, c.image " +
            "FROM item i " +
            "JOIN catalogue c ON c.catalogue_id = i.catalogue_id " +
            "JOIN catalogue cp ON cp.catalogue_id = c.parent_id " +
            "WHERE i.item_id IN (:ids)", nativeQuery = true)
    List<Object[]> findCatsByIds(@Param("ids") List<Integer> ids);
    //находит все каталоги данных item, в том числе родительские

}
