package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.repository.ItemRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReindexSearchService {

    private final ItemJpaRepository dbRepository;
    private final ItemRepository searchRepository;
    @Scheduled(fixedDelay = 43200000)
    @Transactional
    public void reindex(){
        log.info("генерация индексов по товарам запущена");
        searchRepository.deleteAll();
        //все таки стоит удалить данные из эластика, чтобы дубликаты не копились
        dbRepository.findAllInStream().parallel()
                .map(ItemElastic::new)
                .forEach(searchRepository::save);
        log.info("генерация индексов по товарам закончилась");
    }
    //так, то есть тут каждые 12 часов сохраняется в эластик все товары, которые лежат в jpa репозитории
    //и при этом не происходит удаления изначальных данных или проверки на уже добавленные
    //тем самым накапливаются дубликаты товаров

}
